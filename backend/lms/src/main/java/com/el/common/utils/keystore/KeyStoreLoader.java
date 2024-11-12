package com.el.common.utils.keystore;

import org.springframework.stereotype.Component;

import java.io.FileInputStream;
import java.security.KeyStore;
import java.security.PrivateKey;
import java.security.cert.Certificate;

@Component
public class KeyStoreLoader {

    public PrivateKey loadPrivateKey(String keystorePath, String keystorePassword, String alias, String keyPassword) throws Exception {
        FileInputStream keystoreFile = new FileInputStream(keystorePath);
        KeyStore keystore = KeyStore.getInstance("PKCS12");
        keystore.load(keystoreFile, keystorePassword.toCharArray());

        return (PrivateKey) keystore.getKey(alias, keyPassword.toCharArray());
    }

    public Certificate loadCertificate(String keystorePath, String keystorePassword, String alias) throws Exception {
        FileInputStream keystoreFile = new FileInputStream(keystorePath);
        KeyStore keystore = KeyStore.getInstance("PKCS12");
        keystore.load(keystoreFile, keystorePassword.toCharArray());

        // Lấy chứng chỉ từ keystore
        return keystore.getCertificate(alias);
    }

}
