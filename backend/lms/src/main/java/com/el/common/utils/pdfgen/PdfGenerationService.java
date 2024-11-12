package com.el.common.utils.pdfgen;

import com.el.enrollment.domain.Certificate;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.Paragraph;
import lombok.extern.slf4j.Slf4j;

import java.io.ByteArrayOutputStream;

@Slf4j
public class PdfGenerationService {

    public static byte[] generatePdfFromCertificate(Certificate certificate) {
        // Logic tạo PDF từ chứng chỉ
        // using ByteArrayOutputStream to write the content
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        // Logic to generate PDF
        try {
            PdfWriter writer = new PdfWriter(out);
            PdfDocument pdf = new PdfDocument(writer);
            Document document = new Document(pdf);

            // Add content to PDF
            document.add(new Paragraph("Certificate of Completion"));
            document.add(new Paragraph("Student: " + certificate.getStudent()));
            document.add(new Paragraph("Course: " + certificate.getCourseTitle()));
            document.add(new Paragraph("Issued Date: " + certificate.getIssuedDate()));

            document.close();
            log.info("PDF generated for certificate of student {}", certificate.getStudent());
        } catch (Exception e) {
            log.error("Error generating PDF for certificate: {}", e.getMessage());
            throw new RuntimeException("Failed to generate PDF.", e);
        }
        return out.toByteArray();
    }

}
