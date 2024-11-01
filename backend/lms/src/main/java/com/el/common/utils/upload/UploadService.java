package com.el.common.utils.upload;

import org.springframework.web.multipart.MultipartFile;

import java.util.Set;

public interface UploadService {

    String uploadFile(MultipartFile file) ;

    void deleteFile(String url);

    void deleteFiles(Set<String> urls);

}
