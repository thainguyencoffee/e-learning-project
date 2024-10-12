package com.el.awss3.infrastructure;

import com.amazonaws.AmazonClientException;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.CannedAccessControlList;
import com.amazonaws.services.s3.model.PutObjectRequest;
import com.el.awss3.application.AmazonServiceS3Exception;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.net.URL;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class AmazonS3ServiceTests {

    @Mock
    private AmazonS3 amazonS3;

    @InjectMocks
    private AmazonS3ServiceImpl amazonS3Service;


    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this); // Khởi tạo các mock
    }


    @Test
    public void uploadFileWithFileNameIsNullShouldThrowsException() {
        // Arrange
        MultipartFile file = Mockito.mock(MultipartFile.class);
        when(file.getOriginalFilename()).thenReturn(null);

        // Act & Assert
        Assertions.assertThatThrownBy(() -> amazonS3Service.uploadFile(file)).isInstanceOf(AmazonServiceS3Exception.class).hasMessage("File name is null.");
    }


    @Test
    public void testUploadFile_Success() throws IOException {
        // Arrange
        MultipartFile file = Mockito.mock(MultipartFile.class);
        String fileName = "test.txt";
        byte[] content = "Hello, World!".getBytes();

        // Mock các thuộc tính của MultipartFile
        when(file.getOriginalFilename()).thenReturn(fileName);
        when(file.getInputStream()).thenReturn(new ByteArrayInputStream(content));
        when(file.getSize()).thenReturn((long) content.length);

        // Mock phương thức getUrl để trả về URL mong đợi
        // Chúng ta không cần biết tên tệp duy nhất ở đây, chỉ cần mock cho bất kỳ tên nào
        String expectedUrl = "https://bucket.s3.amazonaws.com/someUniqueFileName"; // URL mong đợi
        when(amazonS3.getUrl(any(), any())).thenReturn(new URL(expectedUrl));

        // Act
        String fileUrl = amazonS3Service.uploadFile(file);

        // Assert
        // Kiểm tra rằng phương thức putObject được gọi đúng cách
        verify(amazonS3).putObject(any(PutObjectRequest.class));

        // Kiểm tra rằng URL trả về khớp với URL mong đợi
        Assertions.assertThat(fileUrl).isEqualTo(expectedUrl);

        // Kiểm tra ACL (Access Control List) của tệp đã được đặt thành public-read
        verify(amazonS3).putObject(argThat((PutObjectRequest req) ->
                req.getCannedAcl().equals(CannedAccessControlList.PublicRead)
        ));
    }

    @Test
    public void testUploadFileFailedShouldThrows() throws IOException {
        //Arrange
        MultipartFile file = Mockito.mock(MultipartFile.class);
        String fileName = "test.txt";
        byte[] content = "Hello, World!".getBytes();
        when(file.getOriginalFilename()).thenReturn(fileName);
        when(file.getInputStream()).thenReturn(new ByteArrayInputStream(content));
        when(file.getSize()).thenReturn((long) content.length);

        // Mock phương thức putObject để ném ra ngoại lệ
        when(amazonS3.putObject(any(PutObjectRequest.class)))
                .thenThrow(new AmazonServiceS3Exception("Upload media failed."));

        // Assert
        Assertions.assertThatThrownBy(() -> amazonS3Service.uploadFile(file))
                .isInstanceOf(AmazonServiceS3Exception.class)
                .hasMessage("Upload media failed.");
    }

    @Test
    void testDeleteFileWithUrlIsNullShouldThrows() {
        // Arrange
        String url = null;

        // Act & Assert
        Assertions.assertThatThrownBy(() -> amazonS3Service.deleteFile(url))
                .isInstanceOf(AmazonServiceS3Exception.class)
                .hasMessage("URL and bucket name must not be null or empty.");
    }

    @Test
    public void testDeleteFile_Success() {
        // Arrange
        String bucketName = "mybucket";
        amazonS3Service.setBucketName(bucketName);
        String url = "http://example.com/mybucket/myfile.txt";

        amazonS3Service.deleteFile(url);

        // Assert
        verify(amazonS3).deleteObject(bucketName, "myfile.txt");
    }

    @Test
    public void testDeleteFile_BucketNameNotInURL() {
        // Arrange
        var bucketName = "mybucket";
        amazonS3Service.setBucketName(bucketName);
        String url = "http://example.com/otherbucket/myfile.txt";

        // Assert
        Assertions.assertThatThrownBy(() -> amazonS3Service.deleteFile(url))
                .isInstanceOf(AmazonServiceS3Exception.class)
                .hasMessage("The URL does not contain the specified bucket name.");
    }

    @Test
    public void testDeleteFile_EmptyBucketName() {
        amazonS3Service.setBucketName(""); // Giả sử bạn có phương thức setter cho bucketName
        String url = "http://example.com/mybucket/myfile.txt";

        // Assert
        Assertions.assertThatThrownBy(() -> amazonS3Service.deleteFile(url))
                .isInstanceOf(AmazonServiceS3Exception.class)
                .hasMessage("URL and bucket name must not be null or empty.");
    }

    @Test
    public void testDeleteFile_AmazonClientException() {
        // Arrange
        var bucketName = "mybucket";
        amazonS3Service.setBucketName(bucketName);
        String url = "http://example.com/mybucket/myfile.txt";
        doThrow(new AmazonClientException("Error")).when(amazonS3).deleteObject(bucketName, "myfile.txt");

        // Assert
        Assertions.assertThatThrownBy(() -> amazonS3Service.deleteFile(url))
                .isInstanceOf(AmazonServiceS3Exception.class)
                .hasMessage("Delete media failed.");

    }

}
