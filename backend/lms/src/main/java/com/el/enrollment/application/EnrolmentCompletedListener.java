package com.el.enrollment.application;

import com.el.enrollment.domain.CourseEnrollment.EnrolmentCompletedEvent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.modulith.events.ApplicationModuleListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class EnrolmentCompletedListener {

    private final CourseEnrollmentService courseEnrollmentService;

    public EnrolmentCompletedListener(CourseEnrollmentService courseEnrollmentService) {
        this.courseEnrollmentService = courseEnrollmentService;
    }

    @ApplicationModuleListener
    public void onEnrolmentCompleted(EnrolmentCompletedEvent e) {
        // Create certificate
        log.info("Creating certificate for enrolment: {}", e.id());
        courseEnrollmentService.createCertificate(e.id(), e.student(), e.courseId());
    }

}
