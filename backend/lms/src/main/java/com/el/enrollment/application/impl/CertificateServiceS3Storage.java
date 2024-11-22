    package com.el.enrollment.application.impl;

    import com.el.common.utils.pdfgen.PdfGenerationService;
    import com.el.common.utils.signer.PdfSignerService;
    import com.el.common.upload.application.impl.awss3.AwsS3UploadService;
    import com.el.enrollment.application.CertificateService;
    import com.el.enrollment.domain.Certificate;
    import lombok.extern.slf4j.Slf4j;
    import org.springframework.stereotype.Service;

    import java.util.List;

    @Service
    @Slf4j
    public class CertificateServiceS3Storage implements CertificateService {

        private final PdfSignerService pdfSignerService;
        private final AwsS3UploadService awsS3UploadService;

        public CertificateServiceS3Storage(PdfSignerService pdfSignerService, AwsS3UploadService awsS3UploadService) {
            this.pdfSignerService = pdfSignerService;
            this.awsS3UploadService = awsS3UploadService;
        }

        @Override
        public void createCertUrl(Certificate certificate) {
            String backgroundUrl = "https://bookstore-bucket.sgp1.digitaloceanspaces.com/assets/certificate-background.jpg";
            byte[] background = awsS3UploadService.downloadFile(backgroundUrl);
            byte[] pdfContent = PdfGenerationService.generatePdfFromCertificate(certificate, background);
            byte[] signedPdf = pdfSignerService.signPdf(pdfContent);

            String url = awsS3UploadService.uploadFile(signedPdf, generateFileName(certificate), "pdf", false);
            certificate.markAsCertified(url);
        }

        @Override
        public void revocationCertificate(String certificateUrl) {
            awsS3UploadService.deleteFiles(List.of(certificateUrl));
        }

        private String generateFileName(Certificate certificate) {
            return "CERT_" + certificate.getStudent() + "_" + certificate.getIssuedDate() + ".pdf";
        }

    }
