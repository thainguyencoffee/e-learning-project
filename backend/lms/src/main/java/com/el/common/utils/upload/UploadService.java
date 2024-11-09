package com.el.common.utils.upload;

import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface UploadService {

    String uploadFile(MultipartFile file) ;

    void deleteFiles(List<String> urls);

}
