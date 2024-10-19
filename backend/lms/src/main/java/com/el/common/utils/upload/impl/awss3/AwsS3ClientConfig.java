package com.el.common.utils.upload.impl.awss3;

import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import software.amazon.awssdk.services.s3.S3Configuration;

import java.net.URI;

@Configuration
public class AwsS3ClientConfig {

    @Bean
    public S3Client s3Client(AwsS3Properties properties) {
        AwsBasicCredentials credentials = AwsBasicCredentials.create(
                properties.accessKey(), properties.secretKey());
        return S3Client.builder()
                .endpointOverride(URI.create("https://sgp1.digitaloceanspaces.com"))
                .region(Region.AP_SOUTHEAST_1)
                .credentialsProvider(StaticCredentialsProvider.create(credentials))
                .serviceConfiguration(S3Configuration.builder().pathStyleAccessEnabled(true).build())
                .build();
    }

}
