package com.el.common.utils.signer;

import com.el.common.utils.keystore.KeyStoreLoader;
import com.itextpdf.kernel.pdf.PdfReader;
import com.itextpdf.kernel.pdf.StampingProperties;
import com.itextpdf.signatures.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.security.PrivateKey;

@Service
@Slf4j
public class PdfSignerService {

    private final KeyStoreLoader keyStoreLoader;

    @Value("${KEYSTORE_PASSWORD}")
    private String keystorePassword;

    @Value("${KEY_PASSWORD}")
    private String keyPassword;

    @Value("${KEYSTORE_PATH}")
    private String keystorePath;

    @Value("${KEYSTORE_TYPE}")
    private String keystoreType;

    @Value("${KEY_ALIAS}")
    private String keyAlias;

    public PdfSignerService(KeyStoreLoader keyStoreLoader) {
        this.keyStoreLoader = keyStoreLoader;
    }

    public byte[] signPdf(byte[] pdfContent) {
        ByteArrayOutputStream signedOut = new ByteArrayOutputStream();
        try {
            PdfReader reader = new PdfReader(new ByteArrayInputStream(pdfContent));
            PdfSigner signer = new PdfSigner(reader, signedOut, new StampingProperties());

            signer.setFieldName("sig");

            IExternalSignature pks = new PrivateKeySignature(loadPrivateKey(), "SHA-256", "BC");
            IExternalDigest digest = new BouncyCastleDigest();

            signer.signDetached(digest, pks, loadCertificateChain(), null, null, null, 0, PdfSigner.CryptoStandard.CMS);

            log.info("Digital signature applied.");
        } catch (Exception  e) {
            log.error("Error signing PDF: {}", e.getMessage());
            throw new RuntimeException("Failed to sign PDF.", e);
        }
        return signedOut.toByteArray();
    }

    private PrivateKey loadPrivateKey() {
        try {
            return keyStoreLoader.loadPrivateKey(keystorePath, keystorePassword, keyAlias, keyPassword);
        } catch (Exception e) {
            log.error("Error loading private key from keystore: {}", e.getMessage());
            throw new RuntimeException("Failed to load private key.", e);
        }
    }

    private java.security.cert.Certificate[] loadCertificateChain() {
        try {
            return new java.security.cert.Certificate[]{keyStoreLoader.loadCertificate(keystorePath, keystorePassword, keyAlias)};
        } catch (Exception e) {
            log.error("Error loading certificate from keystore: {}", e.getMessage());
            throw new RuntimeException("Failed to load certificate.", e);
        }
    }

}
