package com.el.common.upload.application.dto;

public record UploadResponse (
        String url,
        String message
){
    public static UploadResponse success(String url) {
        return new UploadResponse(url, "File uploaded successfully.");
    }

}
