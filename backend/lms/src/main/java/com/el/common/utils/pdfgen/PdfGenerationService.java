package com.el.common.utils.pdfgen;

import com.el.enrollment.domain.Certificate;
import com.itextpdf.io.image.ImageDataFactory;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.Image;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.properties.TextAlignment;
import lombok.extern.slf4j.Slf4j;

import java.io.ByteArrayOutputStream;

@Slf4j
public class PdfGenerationService {

    public static byte[] generatePdfFromCertificate(Certificate certificate, byte[] background) {

        try (ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            PdfWriter writer = new PdfWriter(out);
            PdfDocument pdf = new PdfDocument(writer);
            Document document = new Document(pdf);

            document.setMargins(50, 50, 50, 50);

            Image bgImage = new Image(ImageDataFactory.create(background)).scaleToFit(900, 300);
            bgImage.setFixedPosition(95, 500);
            document.add(bgImage);

            document.add(new Paragraph(certificate.getFullName())
                    .setFontSize(18)
                    .setBold()
                    .setTextAlignment(com.itextpdf.layout.properties.TextAlignment.CENTER)
                    .setFixedPosition(55, 673, 500));

            document.add(new Paragraph("has successfully completed the course")
                    .setFontSize(14)
                    .setTextAlignment(com.itextpdf.layout.properties.TextAlignment.CENTER)
                    .setFixedPosition(65, 655, 500));

            document.add(new Paragraph(certificate.getCourseTitle())
                    .setFontSize(16)
                    .setBold()
                    .setTextAlignment(com.itextpdf.layout.properties.TextAlignment.CENTER)
                    .setFixedPosition(60, 632, 500));

            document.add(new Paragraph("Teacher: "  +  certificate.getTeacher())
                    .setFontSize(16)
                    .setBold()
                    .setTextAlignment(TextAlignment.CENTER)
                    .setFixedPosition(60, 610, 500));

            document.add(new Paragraph("Issued date: " + certificate.getIssuedDate())
                    .setFontSize(12)
                    .setBold()
                    .setFontColor(com.itextpdf.kernel.colors.ColorConstants.WHITE)
                    .setTextAlignment(com.itextpdf.layout.properties.TextAlignment.CENTER)
                    .setFixedPosition(60, 500, 500));

            document.close();
            return out.toByteArray();
        } catch (Exception e) {
            throw new RuntimeException("Error generating certificate", e);
        }
    }
}
