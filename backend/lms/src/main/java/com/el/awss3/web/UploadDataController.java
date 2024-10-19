package com.el.awss3.web;

import com.el.awss3.application.AmazonS3Service;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.Base64;
import java.util.Set;

@Slf4j
@RestController
@RequestMapping("/upload")
@RequiredArgsConstructor
public class UploadDataController {

    private final AmazonS3Service amazonS3Service;

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ObjectUrl> upload(@RequestPart
                                         @NotNull(message = "File to upload is required.") MultipartFile file) {
        log.info("Uploading file: {}", file.getOriginalFilename());
        String fileUrl = amazonS3Service.uploadFile(file);
        return new ResponseEntity<>(new ObjectUrl(fileUrl), HttpStatus.CREATED);
    }

    @DeleteMapping(consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> deleteAll(@RequestBody ObjectUrls objectUrls) {
        log.info("Deleting files: {}", objectUrls.urls());
        amazonS3Service.deleteFiles(objectUrls.urls());
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/{urlEncode}")
    public ResponseEntity<Void> delete(@PathVariable String urlEncode) {
        String url = new String(Base64.getDecoder().decode(urlEncode));
        log.info("Deleting file: {}", url);
        amazonS3Service.deleteFile(url);
        return ResponseEntity.noContent().build();
    }

    record ObjectUrls(
            Set<String> urls
    ) {}

    record ObjectUrl(
            String url
    ) {}

}
