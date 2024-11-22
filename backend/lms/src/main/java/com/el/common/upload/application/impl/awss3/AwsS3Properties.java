package com.el.common.upload.application.impl.awss3;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "s3")
public record AwsS3Properties(
        String endpoint,
        String accessKey,
        String secretKey,
        String bucketName
){
}
