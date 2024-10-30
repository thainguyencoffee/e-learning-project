package com.el.course.web;

import com.el.enrollment.domain.CourseEnrollment;
import com.el.enrollment.domain.CourseEnrollmentRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(path = "/published-courses", produces = MediaType.APPLICATION_JSON_VALUE)
public class EnrolmentsPerCourseController {

    private final CourseEnrollmentRepository enrollmentRepository;

    public EnrolmentsPerCourseController(CourseEnrollmentRepository enrollmentRepository) {
        this.enrollmentRepository = enrollmentRepository;
    }

    @GetMapping("/{courseId}/enrolments")
    public ResponseEntity<Page<CourseEnrollment>> getEnrolmentsByCourseId(@PathVariable Long courseId, Pageable pageable) {
        return ResponseEntity.ok(enrollmentRepository.findAllByCourseId(courseId, pageable));
    }

    @GetMapping("/{courseId}/enrolments/count")
    public ResponseEntity<Integer> countEnrolmentsByCourseId(@PathVariable Long courseId) {
        return ResponseEntity.ok(enrollmentRepository.countAllByCourseId(courseId));
    }

}
