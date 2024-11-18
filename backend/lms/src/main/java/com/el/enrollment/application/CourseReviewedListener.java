package com.el.enrollment.application;

import com.el.course.domain.Course;
import org.springframework.modulith.events.ApplicationModuleListener;
import org.springframework.stereotype.Component;

@Component
public class CourseReviewedListener {

    private final CourseEnrollmentService courseEnrollmentService;

    public CourseReviewedListener(CourseEnrollmentService courseEnrollmentService) {
        this.courseEnrollmentService = courseEnrollmentService;
    }

    @ApplicationModuleListener
    public void onCourseReviewed(Course.CourseReviewedEvent event) {
        courseEnrollmentService.markAsReviewed(event.courseId(), event.username());
    }

}
