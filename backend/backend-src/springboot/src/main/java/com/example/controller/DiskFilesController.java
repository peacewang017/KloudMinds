package com.example.controller;

import cn.hutool.core.lang.Dict;
import com.example.common.Result;
import com.example.entity.DiskFiles;
import com.example.entity.Share;
import com.example.entity.Trash;
import com.example.service.DiskFilesService;
import com.github.pagehelper.PageInfo;
import io.minio.MinioClient;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * 网盘文件前端操作接口
 **/
@RestController
@Slf4j
@RequestMapping("/diskFiles")
@Api(tags = "网盘文件操作接口")
public class DiskFilesController {

    @Autowired
    private MinioClient minioClient;


    @Resource
    private DiskFilesService diskFilesService;

    /**
     * 新增
     */
    @PostMapping("/add")
    public Result add(MultipartFile file, String name, String folder, Integer folderId) {
        diskFilesService.add(file, name, folder, folderId);
        return Result.success();
    }

    /**
     * 复制
     */
    @PostMapping("/copy/{id}")
    public Result copy(@PathVariable Integer id) {
        diskFilesService.copy(id, null);
        return Result.success();
    }

    /**
     * 分享
     */
    @PostMapping("/share")
    public Result share(@RequestBody DiskFiles diskFiles) {
        Share share = diskFilesService.share(diskFiles);
        return Result.success(share);
    }

    @ApiOperation("下载文件")
    @GetMapping("/download/{flag}")
    public String download(@PathVariable String flag, HttpServletResponse response) {
        return diskFilesService.download(flag, response);
    }

    @GetMapping("/preview/{name}")
    public String preview(@PathVariable String name, HttpServletResponse response) {
        return diskFilesService.preview(name,response);
    }

    /**
     * 移入回收站
     * 递归删除
     */
    @DeleteMapping("/trash/{id}")
    public Result trash(@PathVariable Integer id) {
        diskFilesService.trashById(id);
        return Result.success();
    }

    /**
     * 批量移入回收站
     */
    @DeleteMapping("/trash/batch")
    public Result trashBatch(@RequestBody List<Integer> ids) {
        diskFilesService.trashBatch(ids);
        return Result.success();
    }

    /**
     * 删除
     */
    @ApiOperation("删除文件")
    @DeleteMapping("/delete/{id}")
    public Result deleteById(@PathVariable Integer id) {
        diskFilesService.deleteById(id);
        return Result.success();
    }

    /**
     * 批量删除
     */
    @DeleteMapping("/delete/batch")
    public Result deleteBatch(@RequestBody List<Integer> ids) {
        diskFilesService.deleteBatch(ids);
        return Result.success();
    }

    /**
     * 修改
     */
    @PutMapping("/update")
    public Result updateById(@RequestBody DiskFiles diskFiles) {
        diskFilesService.updateById(diskFiles);
        return Result.success();
    }

    /**
     * 还原文件
     */
    @PutMapping("/restore/{id}")
    public Result restore(@PathVariable Integer id) {
        diskFilesService.restore(id);
        return Result.success();
    }

    /**
     * 根据ID查询
     */
    @GetMapping("/selectById/{id}")
    public Result selectById(@PathVariable Integer id) {
        DiskFiles diskFiles = diskFilesService.selectById(id);
        return Result.success(diskFiles);
    }

    /**
     * 查询所有
     */
    @GetMapping("/selectAll")
    public Result selectAll(DiskFiles diskFiles) {
        List<DiskFiles> list = diskFilesService.selectAll(diskFiles);
        return Result.success(list);
    }

    /**
     * 根据前端返回的字符串查询相关数据
     */
    @GetMapping("/selectByStr")
    public Result selectByStr(@RequestParam(required = false) String str,
                              @RequestParam(defaultValue = "1") Integer pageNum,
                              @RequestParam(defaultValue = "10") Integer pageSize) {
        PageInfo<DiskFiles> page = diskFilesService.selectByStr(str, pageNum, pageSize);
        return Result.success(page);
    }

    /**
     * 根据前端返回的字符串模糊匹配相关数据
     */
    @GetMapping("/selectByStrLike")
    public Result selectByStrLike(@RequestParam(required = false) String str,
                                  @RequestParam(defaultValue = "1") Integer pageNum,
                                  @RequestParam(defaultValue = "10") Integer pageSize) {
        PageInfo<DiskFiles> page = diskFilesService.selectByStrLike(str, pageNum, pageSize);
        return Result.success(page);
    }


    /**
     * 分页查询
     */
    @GetMapping("/selectPage")
    public Result selectPage(DiskFiles diskFiles,
                             @RequestParam(defaultValue = "1") Integer pageNum,
                             @RequestParam(defaultValue = "10") Integer pageSize) {
        System.out.println("diskFiles = " + diskFiles);
        PageInfo<DiskFiles> page = diskFilesService.selectPage(diskFiles, pageNum, pageSize);
        return Result.success(page);
    }


    /**
     * 查询所有父级的目录名称
     */
    @GetMapping("/selectFolders")
    public Result selectFolders(Integer folderId) {
        List<DiskFiles> list = new ArrayList<>();
        if (folderId == null) {
            return Result.success(list);
        }
        diskFilesService.selectFolderNames(folderId, list);
        Collections.reverse(list);
        return Result.success(list);
    }

    /**
     * 查询回收站
     */
    @GetMapping("/selectTrash")
    public Result selectTrash() {
        List<Trash> list = diskFilesService.selectTrash();
        return Result.success(list);
    }

    /**
     * 查询分享的数据
     */
    @GetMapping("/selectShare")
    public Result selectShare(Integer shareId, Integer folderId) {
        List<DiskFiles> list = diskFilesService.selectShare(shareId, folderId);
        return Result.success(list);
    }

    @GetMapping("/count")
    public Result count(@RequestParam Integer days) {
        List<Dict> list = diskFilesService.count(days);
        return Result.success(list);
    }

}