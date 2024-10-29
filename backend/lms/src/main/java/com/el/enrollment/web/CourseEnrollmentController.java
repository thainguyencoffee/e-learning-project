package com.el.enrollment.web;

import com.el.common.exception.InputInvalidException;
import com.el.enrollment.application.CourseEnrollmentService;
import com.el.enrollment.domain.CourseEnrollment;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("enrollments")
public class CourseEnrollmentController {

    private final CourseEnrollmentService courseEnrollmentService;

    public CourseEnrollmentController(CourseEnrollmentService courseEnrollmentService) {
        this.courseEnrollmentService = courseEnrollmentService;
    }

    @GetMapping
    public ResponseEntity<Page<CourseEnrollment>> getAllEnrollments(Pageable pageable) {
        return ResponseEntity.ok(courseEnrollmentService.findAllCourseEnrollments(pageable));
    }

    @GetMapping("/{enrollmentId}")
    public ResponseEntity<CourseEnrollment> getEnrollmentById(@PathVariable Long enrollmentId) {
        return ResponseEntity.ok(courseEnrollmentService.findCourseEnrollmentById(enrollmentId));
    }

    @PutMapping("/{enrollmentId}/lessons/{lessonId}")
    public ResponseEntity<Void> markLessonAsCompleted(@PathVariable Long enrollmentId,
                                                      @PathVariable Long lessonId,
                                                      @RequestParam(name = "mark") String mark) {
        if (!"completed".equals(mark) && !"incomplete".equals(mark)) {
            throw new InputInvalidException("Mark value must be 'completed' or 'incomplete' only.");
        }
        if ("completed".equals(mark)) {
            courseEnrollmentService.markLessonAsCompleted(enrollmentId, lessonId);
        } else {
            courseEnrollmentService.markLessonAsIncomplete(enrollmentId, lessonId);
        }
        return ResponseEntity.ok().build();
    }

}
