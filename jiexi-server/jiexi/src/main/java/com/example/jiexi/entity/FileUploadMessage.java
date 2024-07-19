package com.example.jiexi.entity;

import lombok.Data;

@Data
public class FileUploadMessage {
    private String bucketName;
    private String fileName;
    private String userId;

    // Getters and Setters
}
