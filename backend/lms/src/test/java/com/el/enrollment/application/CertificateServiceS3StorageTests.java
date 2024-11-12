package com.el.enrollment.application;

import com.el.TestFactory;
import com.el.common.utils.pdfgen.PdfGenerationService;
import com.el.common.utils.signer.PdfSignerService;
import com.el.common.utils.upload.impl.awss3.AwsS3UploadService;
import com.el.enrollment.application.impl.CertificateServiceS3Storage;
import com.el.enrollment.domain.Certificate;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CertificateServiceS3StorageTests {

    @Mock
    AwsS3UploadService awsS3UploadService;

    @Mock
    PdfSignerService pdfSignerService;

    @InjectMocks
    CertificateServiceS3Storage service;

    @Test
    void createCertUrl_GeneratesSignedPdfAndUploads() {
        // Arrange
        Certificate certificate = new Certificate("Student A", "demo@gmail.com",
                TestFactory.user, 1L, "Course Title", TestFactory.teacher);

        // Mock service
        mockStatic(PdfGenerationService.class).when(() -> PdfGenerationService.generatePdfFromCertificate(any(Certificate.class)))
                .thenReturn(new byte[]{1, 2, 3});
        when(pdfSignerService.signPdf(any(byte[].class))).thenReturn(new byte[]{1, 2, 3});
        // Mock AWS S3 upload
        when(awsS3UploadService.uploadFile(any(byte[].class), any(), eq("pdf"), eq(false))).thenReturn("https://s3-url/certificate.pdf");

        // Act
        service.createCertUrl(certificate);

        // Assert
        verify(awsS3UploadService, times(1)).uploadFile(any(byte[].class), any(), eq("pdf"), eq(false));
        assertEquals("https://s3-url/certificate.pdf", certificate.getUrl());
        assertTrue(certificate.isCertified());
    }

}
