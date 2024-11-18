package com.el.common.utils.upload;

import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;

public interface UploadService {

    String uploadFile(MultipartFile file, boolean isPrivate) ;

    String uploadFile(byte[] fileContent, String fileName, String contentType, boolean isPrivate);

    byte[] downloadFile(String url);

    String generatePreSignedUrl(String url);

    void deleteFiles(List<String> urls);

    Map<String, String> startMultipartUpload(String fileName);

}
