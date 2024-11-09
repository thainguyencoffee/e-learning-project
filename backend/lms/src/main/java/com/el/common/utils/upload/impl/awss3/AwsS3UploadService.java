package com.el.common.utils.upload.impl.awss3;

import com.el.common.utils.upload.UploadService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.*;

import java.io.IOException;
import java.util.List;
import java.util.UUID;

@Service
@Slf4j
public class AwsS3UploadService implements UploadService {

    private final AwsS3Properties awsS3Properties;
    private final S3Client s3Client;

    public AwsS3UploadService(AwsS3Properties awsS3Properties, S3Client s3Client) {
        this.awsS3Properties = awsS3Properties;
        this.s3Client = s3Client;
    }

    @Override
    public String uploadFile(MultipartFile file) {
        String fileName = file.getOriginalFilename();
        if (fileName == null) {
            throw new AmazonServiceS3Exception("File name is null.");
        }
        String key = UUID.randomUUID() + "_" + file.getOriginalFilename();
        PutObjectRequest putObject = PutObjectRequest.builder()
                .bucket(awsS3Properties.bucketName())
                .key(key)
                .contentType(file.getContentType())
                .acl("public-read")
                .build();
        try {
            s3Client.putObject(putObject, RequestBody.fromInputStream(file.getInputStream(), file.getSize()));
            log.info("Upload media for {} successfully.", key);
        } catch (IOException e) {
            log.error("Upload media for {} failed: {}", key, e.getMessage());
            throw new AmazonServiceS3Exception("Upload media failed. " + e.getMessage());
        }
        return awsS3Properties.endpoint() + "/" + key;
    }


    @Override
    public void deleteFiles(List<String> urls) {
        DeleteObjectsRequest deleteObjects = DeleteObjectsRequest.builder()
                .bucket(awsS3Properties.bucketName())
                .delete(builder -> builder
                        .objects(urls.stream()
                                .map(this::cutURL)
                                .map(key -> ObjectIdentifier.builder().key(key).build())
                                .toList())
                ).build();
        try {
            s3Client.deleteObjects(deleteObjects);
        } catch (Exception e) {
            log.error("Delete media failed: {}", e.getMessage());
            throw new AmazonServiceS3Exception("Delete media failed. " + e.getMessage());
        }
    }

    private String cutURL(String url) {
        return url.substring(url.lastIndexOf("/") + 1);
    }

}
