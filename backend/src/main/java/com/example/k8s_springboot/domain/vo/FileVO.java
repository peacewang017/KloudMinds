package com.example.k8s_springboot.domain.vo;

import lombok.Data;

/**
 * <p>
 *  VO
 * </p>
 *
 * @since 2024-07-09
 */
@Data
public class FileVO {
    // 文件名
    private String fileName;
    // 文件大小
    private Long fileSize;
    // 文件上传时间
    private String uploadTime;
}
