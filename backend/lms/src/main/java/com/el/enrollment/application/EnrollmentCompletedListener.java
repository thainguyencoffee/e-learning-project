package com.el.enrollment.application;

import com.el.course.application.CourseService;
import com.el.enrollment.application.impl.CertificateServiceS3Storage;
import com.el.enrollment.domain.Enrollment;
import com.el.enrollment.domain.Enrollment.EnrollmentCompletedEvent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.modulith.events.ApplicationModuleListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class EnrollmentCompletedListener {

    private final CourseEnrollmentService courseEnrollmentService;
    private final CertificateServiceS3Storage certificateServiceS3Storage;
    private final CourseService courseService;

    public EnrollmentCompletedListener(CourseEnrollmentService courseEnrollmentService,
                                       CertificateServiceS3Storage certificateServiceS3Storage,
                                       CourseService courseService) {
        this.courseEnrollmentService = courseEnrollmentService;
        this.certificateServiceS3Storage = certificateServiceS3Storage;
        this.courseService = courseService;
    }

    @ApplicationModuleListener
    public void onEnrollmentCompleted(EnrollmentCompletedEvent e) {
        // Create certificate
        log.info("Creating certificate for enrollment: {}", e.id());
        courseEnrollmentService.createCertificate(e.id(), e.student(), e.courseId());
    }

    @ApplicationModuleListener
    public void onEnrollmentIncompleteEvent(Enrollment.EnrollmentIncompleteEvent e) {
        log.info("Enrollment incomplete event received: {}", e.id());
        certificateServiceS3Storage.revocationCertificate(e.certificateUrl());
        courseService.deleteReview(e.courseId(), e.student());
    }

}
