package com.el.enrollment.application;

import com.el.enrollment.domain.Certificate;

public interface CertificateService {

    void createCertUrl(Certificate certificate);

    void revocationCertificate(String certificateUrl);
}
