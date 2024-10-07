package com.el.awss3.infrastructure;

import com.amazonaws.AmazonClientException;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.el.awss3.application.AmazonS3Service;
import com.el.awss3.application.AmazonServiceS3Exception;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
class AmazonS3ServiceImpl implements AmazonS3Service {

    private final AmazonS3 amazonS3;

    @Value("${digitalocean.spaces.bucket-name}")
    private @Setter String bucketName;

    public String uploadFile(MultipartFile file) {
        String fileName = file.getOriginalFilename();
        if (fileName == null) {
            throw new AmazonServiceS3Exception("File name is null.");
        }
        String uniqueFileName = UUID.randomUUID() + "_" + file.getOriginalFilename();
        var objectMetadata = new ObjectMetadata();
        objectMetadata.setContentLength(file.getSize());
        try {
            amazonS3.putObject(bucketName, uniqueFileName, file.getInputStream(), objectMetadata);
            log.info("Upload media for " + uniqueFileName + " successfully.");
        } catch (IOException | AmazonClientException e) {
            log.error("Upload media for " + uniqueFileName + " failed.");
            throw new AmazonServiceS3Exception("Upload media failed.");
        }
        return amazonS3.getUrl(bucketName, uniqueFileName).toString();
    }

    public void deleteFile(String url) {
        try {
            amazonS3.deleteObject(bucketName, cutURL(url, bucketName));
            log.info("Delete media with url {} successfully.", url);
        } catch (AmazonClientException e) {
            log.error("Error deleting media with url {}.", url);
            throw new AmazonServiceS3Exception("Delete media failed.");
        }
    }

    private String cutURL(String url, String bucketName) {
        if (url == null || bucketName == null || bucketName.isEmpty()) {
            throw new AmazonServiceS3Exception("URL and bucket name must not be null or empty.");
        }

        String keyword = "/" + bucketName + "/";
        int startIndex = url.indexOf(keyword);

        // Kiểm tra xem keyword có tồn tại trong URL không
        if (startIndex == -1) {
            throw new AmazonServiceS3Exception("The URL does not contain the specified bucket name.");
        }

        startIndex += keyword.length();

        // Kiểm tra xem startIndex có hợp lệ không
        if (startIndex >= url.length()) {
            throw new IllegalArgumentException("The URL does not contain any object key after the bucket name.");
        }

        return url.substring(startIndex);
    }


}