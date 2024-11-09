package com.el.common.utils.upload.dto;

public record UploadResponse (
        String url,
        String message
){
    public static UploadResponse success(String url) {
        return new UploadResponse(url, "File uploaded successfully.");
    }

}
