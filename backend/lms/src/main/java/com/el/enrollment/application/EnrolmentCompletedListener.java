package com.el.enrollment.application;

import com.el.course.application.CourseService;
import com.el.enrollment.application.impl.CertificateServiceS3Storage;
import com.el.enrollment.domain.CourseEnrollment;
import com.el.enrollment.domain.CourseEnrollment.EnrolmentCompletedEvent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.modulith.events.ApplicationModuleListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class EnrolmentCompletedListener {

    private final CourseEnrollmentService courseEnrollmentService;
    private final CertificateServiceS3Storage certificateServiceS3Storage;
    private final CourseService courseService;

    public EnrolmentCompletedListener(CourseEnrollmentService courseEnrollmentService, CertificateServiceS3Storage certificateServiceS3Storage, CourseService courseService) {
        this.courseEnrollmentService = courseEnrollmentService;
        this.certificateServiceS3Storage = certificateServiceS3Storage;
        this.courseService = courseService;
    }

    @ApplicationModuleListener
    public void onEnrolmentCompleted(EnrolmentCompletedEvent e) {
        // Create certificate
        log.info("Creating certificate for enrolment: {}", e.id());
        courseEnrollmentService.createCertificate(e.id(), e.student(), e.courseId());
    }

    @ApplicationModuleListener
    public void onEnrolmentIncompleteEvent(CourseEnrollment.EnrolmentIncompleteEvent e) {
        log.info("Enrolment incomplete event received: {}", e.id());
        certificateServiceS3Storage.revocationCertificate(e.certificateUrl());
        courseService.deleteReview(e.courseId(), e.student());
    }

}
