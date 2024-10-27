package com.el.enrollment.application;

import com.el.enrollment.domain.CourseEnrollment;
import com.el.enrollment.domain.CourseEnrollmentRepository;
import org.springframework.stereotype.Service;

@Service
public class CourseEnrollmentService {

    private final CourseEnrollmentRepository repository;

    public CourseEnrollmentService(CourseEnrollmentRepository repository) {
        this.repository = repository;
    }

    public void enrollment(String student, Long courseId) {
        CourseEnrollment enrollment = new CourseEnrollment(student, courseId);
        repository.save(enrollment);
    }

}
