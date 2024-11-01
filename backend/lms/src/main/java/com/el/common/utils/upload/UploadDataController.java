package com.el.common.utils.upload;

import com.el.common.utils.upload.impl.awss3.AwsS3UploadService;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.Set;

@Slf4j
@RestController
@RequestMapping("/upload")
@RequiredArgsConstructor
public class UploadDataController {

    private final AwsS3UploadService awsS3UploadService;

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ObjectUrl> upload(@RequestPart
                                         @NotNull(message = "File to upload is required.") MultipartFile file) {
        log.info("Uploading file: {}", file.getOriginalFilename());
        String fileUrl = awsS3UploadService.uploadFile(file);
        return new ResponseEntity<>(new ObjectUrl(fileUrl), HttpStatus.CREATED);
    }

    @DeleteMapping(consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> deleteAll(@RequestBody ObjectUrls objectUrls) {
        log.info("Deleting files: {}", objectUrls.urls());
        awsS3UploadService.deleteFiles(objectUrls.urls());
        return ResponseEntity.noContent().build();
    }

    record ObjectUrls(
            Set<String> urls
    ) {}

    record ObjectUrl(
            String url
    ) {}

}
