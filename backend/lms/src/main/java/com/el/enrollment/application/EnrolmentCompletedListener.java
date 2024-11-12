package com.el.enrollment.application;

import com.el.enrollment.domain.CourseEnrollment.EnrolmentCompletedEvent;
import org.springframework.modulith.events.ApplicationModuleListener;
import org.springframework.stereotype.Component;

@Component
public class EnrolmentCompletedListener {

    private final CourseEnrollmentService courseEnrollmentService;

    public EnrolmentCompletedListener(CourseEnrollmentService courseEnrollmentService) {
        this.courseEnrollmentService = courseEnrollmentService;
    }

    @ApplicationModuleListener
    public void onEnrolmentCompleted(EnrolmentCompletedEvent e) {
        // Create certificate
        courseEnrollmentService.createCertificate(e.id(), e.student(), e.courseId());
    }

}
