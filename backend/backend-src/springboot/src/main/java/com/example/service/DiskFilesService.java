package com.example.service;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.date.DateField;
import cn.hutool.core.date.DateTime;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.io.FileUtil;
import cn.hutool.core.lang.Dict;
import cn.hutool.core.util.IdUtil;
import cn.hutool.core.util.StrUtil;
import com.example.common.Constants;
import com.example.common.enums.ResultCodeEnum;
import com.example.common.enums.RoleEnum;
import com.example.entity.*;
import com.example.exception.CustomException;
import com.example.mapper.DiskFilesMapper;
import com.example.mapper.UserMapper;
import com.example.utils.TokenUtils;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import io.minio.http.Method;
import org.apache.http.HttpEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;


import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;
import java.util.stream.Collectors;

import java.net.URI;


import io.minio.*;


/**
 * 网盘文件信息表业务处理
 **/
@Service
public class DiskFilesService {

    private static final String filePath = System.getProperty("user.dir") + "/disk/";

    private static final Logger log = LoggerFactory.getLogger(DiskFilesService.class);

    @Autowired
    private MinioClient minioClient;

    @Value("${server.port:9090}")
    private String port;

    @Value("${ip:localhost}")
    private String ip;

    @Value("${flask.server.url}")
    private String flaskServerUrl;

    @Resource
    private DiskFilesMapper diskFilesMapper;

    @Resource
    private UserMapper userMapper;
    @Resource
    TrashService trashService;

    @Resource
    ShareService shareService;

    @Autowired
    private RabbitTemplate rabbitTemplate;

    /**
     * 新增
     */
    public void add(MultipartFile file, String name, String folder, Integer folderId) {
        System.out.println("Some informative message");
        System.out.println("Entering add() method");
        System.out.println("Parameters - file: " + file.getOriginalFilename() + ", name: " + name + ", folder: " + folder + ", folderId: " + folderId);

        DiskFiles diskFiles = new DiskFiles();
        String now = DateUtil.now();
        diskFiles.setCrateTime(now);
        diskFiles.setUpdateTime(now);
        diskFiles.setFolder(folder);
        diskFiles.setName(name);
        diskFiles.setType(Constants.FILE_TYPE_FOLDER);

        Account currentUser = TokenUtils.getCurrentUser();
        System.out.println("Current user - id: " + currentUser.getId() + ", name: " + currentUser.getName());

        if (RoleEnum.USER.name().equals(currentUser.getRole())) {
            diskFiles.setUserId(currentUser.getId());
        }
        diskFiles.setFolderId(folderId);

        if (Constants.NOT_FOLDER.equals(folder)) {
            String originalFilename = file.getOriginalFilename();
            diskFiles.setName(originalFilename);
            String extName = FileUtil.extName(originalFilename);
            diskFiles.setType(extName);
            long flag = System.currentTimeMillis();
            String fileName = originalFilename;

            try {
                byte[] bytes = file.getBytes();
                BigDecimal size = BigDecimal.valueOf(bytes.length).divide(BigDecimal.valueOf(1024), 3, RoundingMode.HALF_UP);
                double theSize = size.doubleValue();
                diskFiles.setSize(theSize);
                System.out.println("File size: " + size);

                //检查用户容量是否超出限制
                User user = userMapper.selectCapacityById(currentUser.getId());
                System.out.println(user);
                BigDecimal usedCapacity = user.getUsedCapacity();
                BigDecimal maxCapacity = user.getMaxCapacity();
                if (usedCapacity.add(size).compareTo(maxCapacity) > 0) {
                    throw new CustomException(ResultCodeEnum.CAPACITY_EXCEEDED);
                }

                // 检查bucket是否存在，不存在则创建
                String bucketName = "bucket" + currentUser.getId();
                System.out.println("Bucket name: " + bucketName);

                boolean isExist = minioClient.bucketExists(BucketExistsArgs.builder().bucket(bucketName).build());
                if (!isExist) {
                    System.out.println("Bucket does not exist. Creating bucket: " + bucketName);
                    minioClient.makeBucket(MakeBucketArgs.builder().bucket(bucketName).build());
                }

                // 上传文件到MinIO时设置Content-Type
                String contentType = getContentType(originalFilename);
                InputStream inputStream = file.getInputStream();
                minioClient.putObject(
                        PutObjectArgs.builder()
                                .bucket(bucketName)
                                .object(fileName)
                                .stream(inputStream, file.getSize(), -1)
                                .contentType(contentType) // 设置Content-Type
                                .build()
                );
                inputStream.close();

                String url = "http://35.153.106.4:32000/" + bucketName + "/" + fileName;
                diskFiles.setFile(url);
                System.out.println("File uploaded. URL: " + url);

                // 发送消息到RabbitMQ
                Map<String, String> message = new HashMap<>();
                message.put("bucketName", bucketName);
                message.put("fileName", fileName);
                message.put("userId", String.valueOf(currentUser.getId()));
                rabbitTemplate.convertAndSend("file-upload-exchange", "file.uploaded", message);

                // 更新用户已使用容量
                user.setUsedCapacity(usedCapacity.add(size));
                user.setId(currentUser.getId());
                userMapper.updateById(user);

            } catch (Exception e) {
                System.out.println("File upload error");
                e.printStackTrace();
            }
        }

        diskFilesMapper.insert(diskFiles);
        System.out.println("DiskFiles inserted: " + diskFiles);

        if (folderId != null) {
            DiskFiles parentFolder = this.selectById(folderId);
            Integer rootFolderId = parentFolder.getRootFolderId();
            diskFiles.setRootFolderId(rootFolderId);
        } else {
            if (Constants.IS_FOLDER.equals(folder)) {
                Integer diskFilesId = diskFiles.getId();
                diskFiles.setRootFolderId(diskFilesId);
            }
        }

        this.updateById(diskFiles);
        System.out.println("DiskFiles updated with root folder ID: " + diskFiles.getRootFolderId());
        System.out.println("Exiting add() method");
    }



    /**
     * 网盘文件下载
     */
    public String download(String flag, HttpServletResponse response) {
        System.out.println("Entering download() method");
        System.out.println("Parameter - flag: " + flag);

        try {
            if (StrUtil.isNotEmpty(flag)) {
                Account currentUser = TokenUtils.getCurrentUser();
                System.out.println("Current user - id: " + currentUser.getId() + ", name: " + currentUser.getName());


                System.out.println("Generating download URL for bucket: bucket" + currentUser.getId() + ", object: " + flag);

                // 设置预签名URL的有效期，例如7天
                int expiration = 7 * 24 * 60 * 60;
                String url = minioClient.getPresignedObjectUrl(
                        GetPresignedObjectUrlArgs.builder()
                                .method(Method.GET)
                                .bucket("bucket" + currentUser.getId())
                                .object(flag)
                                .expiry(expiration)
                                .build()
                );

                System.out.println("Download URL generated: " + url);
                System.out.println("Exiting download() method");

                // 返回预签名URL
                return url;
            }
        } catch (Exception e) {
            System.out.println("Error generating download URL");
            e.printStackTrace();
        }
        System.out.println("Exiting download() method");
        // 如果无法生成URL，返回null或适当的错误消息
        return null;
    }

    /**
     * 预览
     */
    public String preview(String name, HttpServletResponse response){
        // 向 MinIO 获取预览链接
        try {
            Account currentUser = TokenUtils.getCurrentUser();
            String url = minioClient.getPresignedObjectUrl(
                    GetPresignedObjectUrlArgs.builder()
                            .method(Method.GET)
                            .bucket("bucket" + currentUser.getId())
                            .object(name)
                            .expiry(7 * 24 * 60 * 60)
                            .build()
            );
            System.out.println("预览链接：" + url);
            return url;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }


/*    public String preview(String name, HttpServletResponse response) {
        // 向 MinIO 获取预览链接
        try {
            Account currentUser = TokenUtils.getCurrentUser();
            String bucketName = "bucket" + currentUser.getId();

            // 获取文件流
            InputStream inputStream = minioClient.getObject(
                    GetObjectArgs.builder()
                            .bucket(bucketName)
                            .object(name)
                            .build()
            );

            // 确定文件类型
            String contentType = getContentType(name);

            // 设置响应头
            response.setContentType(contentType);
            response.setHeader("Content-Disposition", "inline; filename=\"" + name + "\"");

            // 将文件内容写入响应
            IOUtils.copy(inputStream, response.getOutputStream());
            response.flushBuffer();

            inputStream.close();

            return name;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }*/



    public String getContentType(String fileName) {
        if (fileName.endsWith(".txt")) {
            return "text/plain";
        } else if (fileName.endsWith(".pdf")) {
            return "application/pdf";
        } else if (fileName.endsWith(".doc")) {
            return "application/msword";
        } else if (fileName.endsWith(".docx")) {
            return "application/vnd.openxmlformats-officedocument.wordprocessingml.document";
        } else {
            return "application/octet-stream"; // 默认类型
        }
    }



    /**
     * 移入垃圾箱
     */
    @Transactional
    public void trashById(Integer id) {
        DiskFiles diskFiles = this.selectById(id);
        this.deepTrash(id);  // 递归删除子节点

        // 移入文件记录到垃圾箱表
        Trash trash = new Trash();
        trash.setTime(DateUtil.now());
        trash.setFileId(id);
        trash.setName(diskFiles.getName());
        trash.setUserId(diskFiles.getUserId());
        trash.setSize(diskFiles.getSize());
        trashService.add(trash);
    }

    private void deepTrash(Integer id) {
        DiskFiles diskFiles = this.selectById(id);
        if (diskFiles == null) {
            return;
        }
        diskFilesMapper.trashById(id);  // 删除当前的文件
        if (Constants.NOT_FOLDER.equals(diskFiles.getFolder())) {  // 是文件
            return;
        }
        List<DiskFiles> children = diskFilesMapper.selectByFolderId(id);
        if (CollUtil.isEmpty(children)) {
            return;
        }
        for (DiskFiles child : children) {
            this.deepTrash(child.getId());  // 递归寻找子节点
        }
    }

    /**
     * 批量移入垃圾箱
     */
    public void trashBatch(List<Integer> ids) {
        for (Integer id : ids) {
            this.trashById(id);
        }
    }

    /**
     * 删除
     */
    public void deleteById(Integer id) {
        this.deepDelete(id);  // id 是文件的ID
        trashService.deleteByFileId(id);
    }

    private void deepDelete(Integer id) {
        DiskFiles diskFiles = this.selectById(id);
        if (diskFiles == null) {
            return;
        }
        diskFilesMapper.deleteById(id);  // 删除当前的文件记录
        if (Constants.NOT_FOLDER.equals(diskFiles.getFolder())) {  // 是文件
            // 删除文件
            String file = diskFiles.getFile();
            String path = filePath + file.substring(file.lastIndexOf("/"));
            FileUtil.del(path);
            return;
        }
        List<DiskFiles> children = diskFilesMapper.selectByFolderId(id);
        if (CollUtil.isEmpty(children)) {
            return;
        }
        for (DiskFiles child : children) {
            this.deepTrash(child.getId());  // 递归寻找子节点
        }
    }

    /**
     * 批量删除
     */
    public void deleteBatch(List<Integer> ids) {
        for (Integer id : ids) {
            this.deleteById(id);
        }
    }

    /**
     * 修改
     */
    public void updateById(DiskFiles diskFiles) {
        diskFilesMapper.updateById(diskFiles);
    }

    /**
     * 根据ID查询
     */
    public DiskFiles selectById(Integer id) {
        return diskFilesMapper.selectById(id);
    }

    /**
     * 查询所有
     */
    public List<DiskFiles> selectAll(DiskFiles diskFiles) {
        Account currentUser = TokenUtils.getCurrentUser();
        if (RoleEnum.USER.name().equals(currentUser.getRole())) {
            diskFiles.setUserId(currentUser.getId());
        }
        return diskFilesMapper.selectAll(diskFiles);
    }

    /**
     * 分页查询
     */
    public PageInfo<DiskFiles> selectPage(DiskFiles diskFiles, Integer pageNum, Integer pageSize) {
        PageHelper.startPage(pageNum, pageSize);
        List<DiskFiles> list = diskFilesMapper.selectAllData(diskFiles);
        return PageInfo.of(list);
    }


    public List<DiskFiles> selectFolderNames(Integer folderId, List<DiskFiles> list) {
        DiskFiles diskFiles = this.selectById(folderId);
        if (diskFiles == null) {
            return list;
        }
        list.add(diskFiles);  // 把当前节点的名称加到list里面去  最后一起返回
        Integer parentFolderId = diskFiles.getFolderId(); // 父级ID
        if (parentFolderId == null) {  // 当前目录的外层没有目录  就结束
            return list;
        }
        return this.selectFolderNames(parentFolderId, list);
    }

    public List<Trash> selectTrash() {
        Integer userId = null;
        Account currentUser = TokenUtils.getCurrentUser();
        if (RoleEnum.USER.name().equals(currentUser.getRole())) {
            userId = currentUser.getId();
        }
        return diskFilesMapper.selectTrash(userId);
    }

    /**
     * 还原
     */
    public void restore(Integer id) {
        this.deepRestore(id);
        trashService.deleteByFileId(id);  // 删除回收站的记录
    }

    private void deepRestore(Integer id) {
        DiskFiles diskFiles = this.selectById(id);
        if (diskFiles == null) {
            return;
        }
        diskFilesMapper.restoreById(id);  // 删除当前的文件记录
        if (Constants.NOT_FOLDER.equals(diskFiles.getFolder())) {  // 是文件
            return;
        }
        List<DiskFiles> children = diskFilesMapper.selectAllByFolderId(id);
        if (CollUtil.isEmpty(children)) {
            return;
        }
        for (DiskFiles child : children) {
            this.deepRestore(child.getId());  // 递归寻找子节点
        }
    }

    /**
     * 复制
     * folderId 表示外层的目录的Id
     */
    public void copy(Integer id, Integer folderId) {
        DiskFiles diskFiles = this.selectById(id);
        if (diskFiles == null) {
            return;
        }
        // 新建一个对象
        diskFiles.setId(null);
        String now = DateUtil.now();
        diskFiles.setCrateTime(now);
        diskFiles.setUpdateTime(now);
        if (Constants.NOT_FOLDER.equals(diskFiles.getFolder())) {
            String mainName = FileUtil.mainName(diskFiles.getName());
            String extName = FileUtil.extName(diskFiles.getName());
            diskFiles.setName(mainName + "-拷贝." + extName);  // 表示它是复制的文件
        } else {
            diskFiles.setName(diskFiles.getName() + "-拷贝");  // 表示它是复制的文件
        }
        diskFiles.setFolderId(folderId);  // 把拷贝后的文件夹下的所有子节点的folderId设置成当前的文件夹的ID
        diskFilesMapper.insert(diskFiles);
        if (diskFiles.getFolder().equals(Constants.IS_FOLDER)) {  // 是目录的  进行递归复制子文件
            List<DiskFiles> children = diskFilesMapper.selectByFolderId(id); // 排除被删除的文件或者文件夹
            if (CollUtil.isEmpty(children)) {
                return;
            }
            for (DiskFiles child : children) {
                this.copy(child.getId(), diskFiles.getId());  // 递归寻找子节点  复制
            }
        }
    }

    public Share share(DiskFiles diskFiles) {
        Share share = new Share();
        share.setName(diskFiles.getName());
        share.setShareTime(DateUtil.now());
        share.setFileId(diskFiles.getId());
        Account currentUser = TokenUtils.getCurrentUser();
        if (RoleEnum.USER.name().equals(currentUser.getRole())) {
            share.setUserId(currentUser.getId());
        }
        Integer days = diskFiles.getDays();
        DateTime dateTime = DateUtil.offsetDay(new Date(), days);
        String endTime = DateUtil.formatDateTime(dateTime);
        share.setEndTime(endTime);  // 结束时间
        share.setCode(IdUtil.getSnowflakeNextIdStr()); // 生成一个唯一的标识  作为本次分享的验证码
        share.setType(diskFiles.getType());
        shareService.add(share);
        return share;
    }

    public List<DiskFiles> selectShare(Integer shareId, Integer folderId) {
        if (folderId == null) {
            Share share = shareService.selectById(shareId);
            Integer fileId = share.getFileId();
            return CollUtil.newArrayList(this.selectById(fileId));
        } else {
            DiskFiles diskFiles = new DiskFiles();
            diskFiles.setFolderId(folderId);
            return this.selectAll(diskFiles);
        }
    }

    public List<Dict> count(Integer days) {
        List<Dict> list = new ArrayList<>();
        Date now = new Date();
        DateTime end = DateUtil.offsetDay(now, -1);// 前一天  -1  ~ -7
        DateTime start = DateUtil.offsetDay(now, -days);// 前 n天
        List<DateTime> dateTimeList = DateUtil.rangeToList(start, end, DateField.DAY_OF_YEAR);
        List<String> dateList = dateTimeList.stream().map(DateUtil::formatDate).sorted(String::compareTo).collect(Collectors.toList());
        for (String date : dateList) {
            Integer count = diskFilesMapper.selectByDate(date);
            Dict dict = Dict.create().set("date", date).set("count", count);
            list.add(dict);
        }
        return list;
    }


    @Autowired
    private RestHighLevelClient client;
    public PageInfo<DiskFiles> selectByStr(String str, Integer pageNum, Integer pageSize) {
        Account currentUser = TokenUtils.getCurrentUser();
        String bucketName = "bucket" + currentUser.getId();
        System.out.println(str);
        if (str==null){
            PageHelper.startPage(pageNum, pageSize);
            List<DiskFiles> list = new ArrayList<>();
            return PageInfo.of(list);
        }

        System.out.println("Current user bucketName: " + bucketName); // 调试输出当前用户的bucketName
        System.out.println(str);

        SearchRequest searchRequest = new SearchRequest("files");
        BoolQueryBuilder boolQuery = QueryBuilders.boolQuery()
                .must(QueryBuilders.termQuery("bucketName.keyword", bucketName))
                .must(QueryBuilders.simpleQueryStringQuery(str)
                        .field("fileName")
                        .field("content"));

        SearchSourceBuilder sourceBuilder = new SearchSourceBuilder();
        sourceBuilder.query(boolQuery);
        searchRequest.source(sourceBuilder);

        List<String> fileNameList = new ArrayList<>();
        try {
            SearchResponse searchResponse = client.search(searchRequest, RequestOptions.DEFAULT);

            System.out.println("SearchResponse: " + searchResponse); // 调试输出整个搜索响应

            for (SearchHit hit : searchResponse.getHits().getHits()) {
                String fileName = (String) hit.getSourceAsMap().get("fileName");
                fileNameList.add(fileName);
            }

        } catch (IOException e) {
            e.printStackTrace();
        }

        System.out.println("fileNameList = " + fileNameList); // 调试输出查询到的文件名列表
        if (fileNameList.isEmpty()) {
            return new PageInfo<>(new ArrayList<>());
        }

        PageHelper.startPage(pageNum, pageSize);
        List<DiskFiles> list = diskFilesMapper.selectByNameList(fileNameList);
        return PageInfo.of(list);

    }

    public PageInfo<DiskFiles> selectByStrLike(String str, Integer pageNum, Integer pageSize) {
        Account currentUser = TokenUtils.getCurrentUser();
        String bucketName = "bucket" + currentUser.getId();

        if (str == null) {
            PageHelper.startPage(pageNum, pageSize);
            return new PageInfo<>(new ArrayList<>());
        }

        try {
            // 调用Weaviate RAG搜索接口
            List<String> fileNameList = searchInWeaviate(bucketName, str);

            if (fileNameList.isEmpty()) {
                return new PageInfo<>(new ArrayList<>());
            }

            PageHelper.startPage(pageNum, pageSize);
            List<DiskFiles> list = diskFilesMapper.selectByNameList(fileNameList);
            return PageInfo.of(list);

        } catch (Exception e) {
            e.printStackTrace();
            return new PageInfo<>(new ArrayList<>());
        }
    }

    private List<String> searchInWeaviate(String bucketName, String keyword) throws IOException {
        CloseableHttpClient httpClient = HttpClients.createDefault();
        HttpPost httpPost = new HttpPost(flaskServerUrl + "/rag_search");
        httpPost.setHeader("Content-Type", "application/json");

        Map<String, String> data = new HashMap<>();
        data.put("bucketname", bucketName);
        data.put("targetcontent", keyword);
        String json = new ObjectMapper().writeValueAsString(data);

        StringEntity entity = new StringEntity(json);
        httpPost.setEntity(entity);

        CloseableHttpResponse response = httpClient.execute(httpPost);
        HttpEntity responseEntity = response.getEntity();

        if (response.getStatusLine().getStatusCode() == 200 && responseEntity != null) {
            String responseBody = EntityUtils.toString(responseEntity);
            List<Map<String, String>> articles = new ObjectMapper().readValue(responseBody, List.class);
            List<String> fileNameList = new ArrayList<>();

            for (Map<String, String> article : articles) {
                String fileName = article.get("filename");
                fileNameList.add(fileName);
            }

            return fileNameList;
        } else {
            throw new RuntimeException("Search in Weaviate failed with status code: " + response.getStatusLine().getStatusCode());
        }
    }
}