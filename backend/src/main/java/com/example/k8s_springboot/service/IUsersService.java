package com.example.k8s_springboot.service;

import com.example.k8s_springboot.domain.po.User;
import com.baomidou.mybatisplus.extension.service.IService;
import com.example.k8s_springboot.domain.vo.FileVO;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

/**
 * <p>
 *  服务类
 * </p>
 *
 * @author lrp
 * @since 2024-07-09
 */
public interface IUsersService extends IService<User> {

    User createUser(String username, String password) throws Exception;

    void uploadFile(String username, MultipartFile file) throws Exception;

    void downloadFile(String username, String objectName, String filePath) throws Exception;

    long getUserCurrentUsage(String username) throws Exception;

    List<FileVO> getUserFiles(String username) throws Exception;

    void extendUserCapacity(String username, long capacity) throws Exception;

    long getUserCapacity(String username) throws Exception;

    void deleteUserFile(String username, String objectName);

    void deleteUser(String username);
}
