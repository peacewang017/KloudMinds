package com.example.k8s_springboot.controller;

import com.example.k8s_springboot.domain.po.User;
import com.example.k8s_springboot.domain.vo.FileVO;
import com.example.k8s_springboot.service.IUsersService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

/**
 * <p>
 *  前端控制器
 * </p>
 *
 * @author lrp
 * @since 2024-07-09
 */



@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
@Api(tags = "用户管理接口")
@Slf4j
public class UserController {

    @Autowired
    private final IUsersService userService;

    @ApiOperation("创建用户")
    @PostMapping("/create")
    public User createUser(@RequestParam String username,@RequestParam String password) throws Exception {
        return userService.createUser(username, password);
    }

    @ApiOperation("上传文件")
    @PostMapping("/{username}/upload")
    public String uploadFile(@PathVariable String username, @RequestParam MultipartFile file) {
        try {
            userService.uploadFile(username, file);
            return "文件上传成功";
        } catch (Exception e) {
            return "错误: " + e.getMessage();
        }
    }

    @ApiOperation("下载文件")
    @GetMapping("/{username}/download")
    public String downloadFile(@PathVariable String username, @RequestParam String objectName, @RequestParam String filePath) {
        try {
            userService.downloadFile(username, objectName, filePath);
            return "文件下载成功";
        } catch (Exception e) {
            return "错误: " + e.getMessage();
        }
    }

    @ApiOperation("获取用户当前使用量")
    @GetMapping("/{username}/usage")
    public long getUserCurrentUsage(@PathVariable String username) {
        try {
            return userService.getUserCurrentUsage(username);
        } catch (Exception e) {
            return -1;
        }
    }

    @ApiOperation("获取用户存储的文件名称和文件大小列表")
    @GetMapping("/{username}/files")
    public List<FileVO> getUserFiles(@PathVariable String username) {
        try {
            return userService.getUserFiles(username);
        } catch (Exception e) {
            return null;
        }
    }

    @ApiOperation("给指定用户扩容")
    @GetMapping("/{username}/extend")
    public String extendUserCapacity(@PathVariable String username, @RequestParam long capacity) {
        try {
            userService.extendUserCapacity(username, capacity);
            return "扩容成功";
        } catch (Exception e) {
            return "错误: " + e.getMessage();
        }
    }

    @ApiOperation("获取当前用户的最大存储容量")
    @GetMapping("/{username}/capacity")
    public long getUserCapacity(@PathVariable String username) {
        try {
            return userService.getUserCapacity(username);
        } catch (Exception e) {
            return -1;
        }
    }

    @ApiOperation("删除用户的指定文件")
    @DeleteMapping("/{username}/delete")
    public String deleteUserFile(@PathVariable String username, @RequestParam String objectName) {
        try {
            userService.deleteUserFile(username, objectName);
            return "文件删除成功";
        } catch (Exception e) {
            return "错误: " + e.getMessage();
        }
    }

    @ApiOperation("删除用户")
    @DeleteMapping("/{username}/deleteUser")
    public String deleteUser(@PathVariable String username) {
        //test
        try {
            userService.deleteUser(username);
            return "用户删除成功";
        } catch (Exception e) {
            return "错误: " + e.getMessage();
        }
    }

}

