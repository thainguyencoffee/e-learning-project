package com.el.awss3.application;

import org.springframework.web.multipart.MultipartFile;

import java.util.Set;

public interface AmazonS3Service {
    String uploadFile(MultipartFile file);
    void deleteFile(String url);
    void deleteFiles(Set<String> urls);
}
