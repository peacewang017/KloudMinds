package com.example.k8s_springboot.service.impl;

import com.example.k8s_springboot.domain.po.User;
import com.example.k8s_springboot.domain.vo.FileVO;
import com.example.k8s_springboot.mapper.UsersMapper;
import com.example.k8s_springboot.service.IUsersService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import io.minio.*;
import io.minio.messages.Item;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;


import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

/**
 * <p>
 * 服务实现类
 * </p>
 *
 * @author lrp
 * @since 2024-07-09
 */
@Service
public class UsersServiceImpl extends ServiceImpl<UsersMapper, User> implements IUsersService {
    @Autowired
    private UsersMapper usersMapper;

    @Autowired
    private MinioClient minioClient;

    /**
     * 根据用户名获得桶名称
     */
    public String getBucketName(String username) {
        return "bucket-" + username;
    }

    /**
     * 创建用户并初始化存储桶
     */
    // 创建用户
    public User createUser(String username, String password) throws Exception {
        User user = new User();
        user.setUsername(username);
        user.setBucketName(getBucketName(username));
        user.setPassword(password); // 设置用户密码
        user.setCurrentUsage(0L); // 设置当前使用量
        user.setMaxCapacity(500 * 1024 * 1024L); // 设置最大容量为500MB

        // 创建存储桶
        ensureBucketExists(user.getBucketName());

        // 保存用户到数据库
        usersMapper.insert(user);
        return user;
    }

    /**
     * 确保存储桶存在，如果不存在则创建
     */
    private void ensureBucketExists(String bucketName) throws Exception {
        boolean isExist = minioClient.bucketExists(BucketExistsArgs.builder().bucket(bucketName).build());
        if (!isExist) {
            minioClient.makeBucket(MakeBucketArgs.builder().bucket(bucketName).build());
        }
    }

    /**
     * 用户上传文件
     */
    public void uploadFile(String username, MultipartFile file) throws Exception {
        User user = usersMapper.selectByUsername(username);
        if (user == null) throw new Exception("用户未找到");

        long newUsage = user.getCurrentUsage() + file.getSize();
        if (newUsage > user.getMaxCapacity()) throw new Exception("存储空间不足");

        // 使用Java的临时文件机制
        Path tempFile = Files.createTempFile("upload_", "_" + file.getOriginalFilename());
        File convFile = tempFile.toFile();
        try (FileOutputStream fos = new FileOutputStream(convFile)) {
            fos.write(file.getBytes());
        }

        try (FileInputStream fis = new FileInputStream(convFile)) {
            // 上传文件到MinIO
            minioClient.putObject(
                    PutObjectArgs.builder()
                            .bucket(user.getBucketName())
                            .object(file.getOriginalFilename())
                            .stream(fis, file.getSize(), -1)
                            .build()
            );
        } finally {
            // 删除临时文件
            if (!convFile.delete()) {
                System.err.println("无法删除临时文件: " + convFile.getAbsolutePath());
            }
        }

        // 更新用户存储使用量
        user.setCurrentUsage(newUsage);
        usersMapper.updateById(user);
    }

    /**
     * 用户下载文件
     */
    public void downloadFile(String username, String objectName, String filePath) throws Exception {
        User user = usersMapper.selectByUsername(username);
        if (user == null) throw new Exception("用户未找到");

        // 从MinIO下载文件
        try (InputStream stream = minioClient.getObject(
                GetObjectArgs.builder().bucket(user.getBucketName()).object(objectName).build());
             FileOutputStream out = new FileOutputStream(filePath)) {
            byte[] buf = new byte[1024];
            int bytesRead;
            while ((bytesRead = stream.read(buf)) != -1) {
                out.write(buf, 0, bytesRead);
            }
        }
    }

    /**
     * 获取用户当前存储容量
     */
    public long getUserCurrentUsage(String username) throws Exception {
        User user = usersMapper.selectByUsername(username);
        if (user == null) throw new Exception("用户未找到");
        return user.getCurrentUsage();
    }

    @Override
    public List<FileVO> getUserFiles(String username) throws Exception {
        List<FileVO> userFiles = new ArrayList<>();
        User user = usersMapper.selectByUsername(username);
        if (user == null) throw new Exception("用户未找到");

        try {

            Iterable<Result<Item>> results = minioClient.listObjects(
                    ListObjectsArgs.builder().bucket(user.getBucketName()).build());

            for (Result<Item> result : results) {
                Item item = result.get();
                // 获取文件的元数据
                StatObjectResponse stat = minioClient.statObject(
                        StatObjectArgs.builder().bucket(user.getBucketName()).object(item.objectName()).build());

                // 创建并填充FileVO对象
                FileVO fileVO = new FileVO();
                fileVO.setFileName(stat.object());
                fileVO.setFileSize(stat.size());
                // 格式化时间可能需要根据实际情况调整
                fileVO.setUploadTime(stat.lastModified().toString());

                // 添加到列表中
                userFiles.add(fileVO);
            }
        } catch (Exception e) {
            e.printStackTrace();
            // 处理异常，例如返回空列表或错误信息
        }
        return userFiles;

    }

    @Override
    public void extendUserCapacity(String username, long capacity) throws Exception {
        User user = usersMapper.selectByUsername(username);
        if (user == null) throw new Exception("用户未找到");

        user.setMaxCapacity(user.getMaxCapacity() + capacity);
        usersMapper.updateById(user);
    }

    @Override
    public long getUserCapacity(String username) throws Exception {
        User user = usersMapper.selectByUsername(username);
        if (user == null) throw new Exception("用户未找到");
        ;
        return user.getMaxCapacity();
    }

    @Override
    public void deleteUserFile(String username, String objectName) {
        try {
            User user = usersMapper.selectByUsername(username);
            if (user == null) throw new Exception("用户未找到");

            // 获取文件信息以得到文件大小
            StatObjectResponse statObject = minioClient.statObject(
                    StatObjectArgs.builder().bucket(user.getBucketName()).object(objectName).build());
            long fileSize = statObject.size();

            // 删除MinIO中的文件
            minioClient.removeObject(
                    RemoveObjectArgs.builder().bucket(user.getBucketName()).object(objectName).build());

            // 更新用户存储使用量，减去文件大小
            user.setCurrentUsage(user.getCurrentUsage() - fileSize);
            usersMapper.updateById(user);
        } catch (Exception e) {
            e.printStackTrace();
            // 处理异常，例如返回错误信息
        }
    }

    @Override
    public void deleteUser(String username) {
        try {
            User user = usersMapper.selectByUsername(username);
            if (user == null) throw new Exception("用户未找到");

            // 删除对应桶中全部内容
            deleteAllObjectsInBucket(user.getBucketName());

            // 删除MinIO中的存储桶
            deleteBucket(user.getBucketName());

            // 删除数据库中的用户
            usersMapper.deleteById(user.getId());
        } catch (Exception e) {
            e.printStackTrace();
            // 处理异常，例如返回错误信息
        }
    }

    private void deleteAllObjectsInBucket(String bucketName) throws Exception {
        Iterable<Result<Item>> results = minioClient.listObjects(
                ListObjectsArgs.builder().bucket(bucketName).build());
        for (Result<Item> result : results) {
            Item item = result.get();
            minioClient.removeObject(
                    RemoveObjectArgs.builder().bucket(bucketName).object(item.objectName()).build());
        }
    }

    private void deleteBucket(String bucketName) throws Exception {
        minioClient.removeBucket(
                RemoveBucketArgs.builder().bucket(bucketName).build());
    }

}
