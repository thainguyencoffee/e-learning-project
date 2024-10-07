package com.el.awss3.application;

import org.springframework.web.multipart.MultipartFile;

public interface AmazonS3Service {
    String uploadFile(MultipartFile file);
    void deleteFile(String url);
}
