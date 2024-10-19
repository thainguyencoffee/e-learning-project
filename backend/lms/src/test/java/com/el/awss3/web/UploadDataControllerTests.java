package com.el.awss3.web;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.el.awss3.application.AmazonS3Service;
import com.el.awss3.application.AmazonServiceS3Exception;
import com.el.common.config.SecurityConfig;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Base64;
import java.util.Set;
import java.util.UUID;

@WebMvcTest(UploadDataController.class)
@Import(SecurityConfig.class)
class UploadDataControllerTests {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private AmazonS3Service amazonS3Service;

    @Autowired
    private ObjectMapper objectMapper;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void whenUploadMediaWithTeacherRoleThen201() throws Exception {
        // Arrange
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "test.txt",
                "text/plain",
                "Hello, World!".getBytes());
        var url = "https://example.com/bucket-name/" + UUID.randomUUID();

        // Mock hành vi của amazonS3Service
        when(amazonS3Service.uploadFile(any())).thenReturn(url);

        // Gửi yêu cầu POST đến endpoint /upload
        mockMvc.perform(multipart("/upload")
                        .file(file)
                        .contentType(MediaType.MULTIPART_FORM_DATA)
                        .with(SecurityMockMvcRequestPostProcessors.jwt().authorities(new SimpleGrantedAuthority("ROLE_teacher"))))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.url").value(url));
    }

    @Test
    void whenUploadMediaWithAdminRoleThen201() throws Exception {
        // Arrange
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "test.txt",
                "text/plain",
                "Hello, World!".getBytes());
        var url = "https://example.com/bucket-name/" + UUID.randomUUID();

        // Mock hành vi của amazonS3Service
        when(amazonS3Service.uploadFile(any())).thenReturn(url);

        // Gửi yêu cầu POST đến endpoint /upload
        mockMvc.perform(multipart("/upload")
                        .file(file)
                        .contentType(MediaType.MULTIPART_FORM_DATA)
                        .with(SecurityMockMvcRequestPostProcessors.jwt().authorities(new SimpleGrantedAuthority("ROLE_admin"))))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.url").value(url));
    }

    @Test
    void whenUploadMediaWithUserRoleThen403() throws Exception {
        // Arrange
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "test.txt",
                "text/plain",
                "Hello, World!".getBytes());

        // Gửi yêu cầu POST đến endpoint /upload
        mockMvc.perform(multipart("/upload")
                        .file(file)
                        .contentType(MediaType.MULTIPART_FORM_DATA)
                        .with(SecurityMockMvcRequestPostProcessors.jwt().authorities(new SimpleGrantedAuthority("ROLE_user")))
                )
                .andExpect(status().isForbidden());
    }

    @Test
    void whenUploadMediaWithNotAuthenticatedThen401() throws Exception {
        // Arrange
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "test.txt",
                "text/plain",
                "Hello, World!".getBytes());

        // Gửi yêu cầu POST đến endpoint /upload
        mockMvc.perform(multipart("/upload").file(file).contentType(MediaType.MULTIPART_FORM_DATA))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void whenUploadMediaWithErrorsThen500() throws Exception {
        // Arrange
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "test.txt",
                "text/plain",
                "Hello, World!".getBytes());

        // Mock hành vi của amazonS3Service
        doThrow(new AmazonServiceS3Exception("something went wrong")).when(amazonS3Service).uploadFile(any());

        // Gửi yêu cầu POST đến endpoint /upload
        mockMvc.perform(multipart("/upload")
                        .file(file)
                        .contentType(MediaType.MULTIPART_FORM_DATA)
                        .with(SecurityMockMvcRequestPostProcessors.jwt().authorities(new SimpleGrantedAuthority("ROLE_teacher")))
                )
                .andExpect(status().isInternalServerError());
    }

    @Test
    void whenDeleteMediaWithAdminRoleThenReturn204() throws Exception {
        // Arrange
        String url = "https://example.com/bucket-name/" + UUID.randomUUID();
        String urlEncode = Base64.getEncoder().encodeToString(url.getBytes());

        // Giả lập hành vi của amazonS3Service
        doNothing().when(amazonS3Service).deleteFile(url);

        // Gửi yêu cầu DELETE đến endpoint /upload/{urlEncode}
        mockMvc.perform(delete("/upload/{urlEncode}", urlEncode)
                        .with(SecurityMockMvcRequestPostProcessors.jwt().authorities(new SimpleGrantedAuthority("ROLE_admin"))))
                .andExpect(status().isNoContent());
    }

    @Test
    void whenDeleteMediaWithTeacherRoleThenReturn204() throws Exception {
        // Arrange
        String url = "https://example.com/bucket-name/" + UUID.randomUUID();
        String urlEncode = Base64.getEncoder().encodeToString(url.getBytes());

        // Giả lập hành vi của amazonS3Service
        doNothing().when(amazonS3Service).deleteFile(url);

        // Gửi yêu cầu DELETE đến endpoint /upload/{urlEncode}
        mockMvc.perform(delete("/upload/{urlEncode}", urlEncode)
                        .with(SecurityMockMvcRequestPostProcessors.jwt().authorities(new SimpleGrantedAuthority("ROLE_teacher"))))
                .andExpect(status().isNoContent());
    }

    @Test
    void whenDeleteMediaWithUserRoleThenReturn403() throws Exception {
        // Arrange
        String url = "https://example.com/bucket-name/" + UUID.randomUUID();
        String urlEncode = Base64.getEncoder().encodeToString(url.getBytes());

        // Gửi yêu cầu DELETE đến endpoint /upload/{urlEncode}
        mockMvc.perform(delete("/upload/{urlEncode}", urlEncode)
                        .with(SecurityMockMvcRequestPostProcessors.jwt().authorities(new SimpleGrantedAuthority("ROLE_user"))))
                .andExpect(status().isForbidden());
    }

    @Test
    void whenDeleteMediaWithNotAuthenticatedThenReturn401() throws Exception {
        // Arrange
        String url = "https://example.com/bucket-name/" + UUID.randomUUID();
        String urlEncode = Base64.getEncoder().encodeToString(url.getBytes());

        // Gửi yêu cầu DELETE đến endpoint /upload/{urlEncode}
        mockMvc.perform(delete("/upload/{urlEncode}", urlEncode))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void whenDeleteMediaWithErrorThrowsThenReturn() throws Exception {
        // Arrange
        String url = "https://example.com/bucket-name/" + UUID.randomUUID();
        String urlEncode = Base64.getEncoder().encodeToString(url.getBytes());

        // Giả lập hành vi của amazonS3Service
        doThrow(new AmazonServiceS3Exception("something went wrong")).when(amazonS3Service).deleteFile(url);

        // Gửi yêu cầu DELETE đến endpoint /upload/{urlEncode}
        mockMvc.perform(delete("/upload/{urlEncode}", urlEncode)
                        .with(SecurityMockMvcRequestPostProcessors.jwt().authorities(new SimpleGrantedAuthority("ROLE_teacher"))))
                .andExpect(status().isInternalServerError());
    }

    @Test
    void whenDeleteAllWithTeacherThen204() throws Exception {
        // Arrange
        Set<String> urlsSet = Set.of(
                "https://example.com/bucket-name/" + UUID.randomUUID(),
                "https://example.com/bucket-name/" + UUID.randomUUID(),
                "https://example.com/bucket-name/" + UUID.randomUUID());
        var objectUrls = new UploadDataController.ObjectUrls(urlsSet);

        doNothing().when(amazonS3Service).deleteFiles(urlsSet);

        // Gửi yêu cầu DELETE đến endpoint /upload/{urlEncode}
        mockMvc.perform(delete("/upload")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(objectUrls))
                        .with(SecurityMockMvcRequestPostProcessors.jwt().authorities(new SimpleGrantedAuthority("ROLE_teacher"))))
                .andExpect(status().isNoContent());
    }

    @Test
    void whenDeleteAllWithUserRoleThen403() throws Exception {
        // Arrange
        Set<String> urlsSet = Set.of(
                "https://example.com/bucket-name/" + UUID.randomUUID(),
                "https://example.com/bucket-name/" + UUID.randomUUID(),
                "https://example.com/bucket-name/" + UUID.randomUUID());
        var objectUrls = new UploadDataController.ObjectUrls(urlsSet);

        doNothing().when(amazonS3Service).deleteFiles(urlsSet);

        // Gửi yêu cầu DELETE đến endpoint /upload/{urlEncode}
        mockMvc.perform(delete("/upload")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(objectUrls))
                        .with(SecurityMockMvcRequestPostProcessors.jwt().authorities(new SimpleGrantedAuthority("ROLE_user"))))
                .andExpect(status().isForbidden());
    }

}