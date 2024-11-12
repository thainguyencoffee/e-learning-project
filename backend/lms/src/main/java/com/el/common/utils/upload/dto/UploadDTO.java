package com.el.common.utils.upload.dto;

import java.util.List;

public record UploadDTO(
        List<String> urls
) {

    public static UploadDTO of(String url) {
        return new UploadDTO(List.of(url));
    }

}
