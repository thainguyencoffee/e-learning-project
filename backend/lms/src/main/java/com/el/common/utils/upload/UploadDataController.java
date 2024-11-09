package com.el.common.utils.upload;

import com.el.common.utils.upload.dto.UploadDTO;
import com.el.common.utils.upload.dto.UploadResponse;
import com.el.common.utils.upload.impl.awss3.AwsS3UploadService;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.services.s3.model.CompletedPart;

import java.util.List;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/upload")
@RequiredArgsConstructor
public class UploadDataController {

    private final AwsS3UploadService awsS3UploadService;

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<UploadResponse> uploadSingle(@RequestPart @NotNull(message = "File to upload is required.") MultipartFile file) {
        log.info("Uploading single file: {}", file.getOriginalFilename());
        String url = awsS3UploadService.uploadFile(file);
        return ResponseEntity.ok(UploadResponse.success(url));
    }

    @DeleteMapping(path = "/delete", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> deleteAll(@RequestBody UploadDTO uploadDTO) {
        log.info("Deleting files: {}", uploadDTO.urls());
        awsS3UploadService.deleteFiles(uploadDTO.urls());
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/start")
    public ResponseEntity<Map<String, String>> startMultipartUpload(@RequestParam String fileName) {
        log.info("Starting multipart upload for file: {}", fileName);
        return ResponseEntity.ok(awsS3UploadService.startMultipartUpload(fileName));
    }

    @PostMapping("/complete")
    public ResponseEntity<Void> completeMultipartUpload(@RequestParam String key,
                                                        @RequestParam String uploadId,
                                                        @RequestBody List<CompletedPart> parts) {
        log.info("Completing multipart upload for key: {}, uploadId: {}", key, uploadId);
        awsS3UploadService.completeMultipartUpload(key, uploadId, parts);
        return ResponseEntity.ok().build();
    }

}
