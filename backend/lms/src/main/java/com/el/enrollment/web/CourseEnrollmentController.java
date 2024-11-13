package com.el.enrollment.web;

import com.el.common.exception.InputInvalidException;
import com.el.common.exception.ResourceNotFoundException;
import com.el.enrollment.application.dto.CourseEnrollmentDTO;
import com.el.enrollment.application.CourseEnrollmentService;
import com.el.enrollment.application.dto.EnrolmentWithCourseDTO;
import com.el.enrollment.application.dto.QuizSubmitDTO;
import com.el.enrollment.domain.CourseEnrollment;
import com.el.enrollment.domain.CourseEnrollmentRepository;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("enrollments")
public class CourseEnrollmentController {

    private final CourseEnrollmentService courseEnrollmentService;
    private final CourseEnrollmentRepository enrollmentRepository;

    public CourseEnrollmentController(CourseEnrollmentService courseEnrollmentService, CourseEnrollmentRepository enrollmentRepository) {
        this.courseEnrollmentService = courseEnrollmentService;
        this.enrollmentRepository = enrollmentRepository;
    }

    @GetMapping
    public ResponseEntity<Page<CourseEnrollmentDTO>> getAllEnrollments(Pageable pageable) {
        List<CourseEnrollmentDTO> result = courseEnrollmentService.findAllCourseEnrollments(pageable);
        return ResponseEntity.ok(new PageImpl<>(result, pageable, result.size()));
    }

    @GetMapping("/{enrollmentId}")
    public ResponseEntity<CourseEnrollment> getEnrollmentById(@PathVariable Long enrollmentId) {
        return ResponseEntity.ok(courseEnrollmentService.findCourseEnrollmentById(enrollmentId));
    }

    @GetMapping("/count")
    public ResponseEntity<Integer> countEnrolmentsByCourseId(@RequestParam(name = "courseId") Long courseId) {
        return ResponseEntity.ok(enrollmentRepository.countAllByCourseId(courseId));
    }

    @GetMapping("/course/{courseId}")
    public ResponseEntity<CourseEnrollment> getEnrolmentByCourseId(@PathVariable Long courseId, @AuthenticationPrincipal Jwt jwt) {
        String username = jwt.getClaim("preferred_username");
        CourseEnrollment courseEnrollment = enrollmentRepository.findByCourseIdAndStudent(courseId, username)
                .orElseThrow(ResourceNotFoundException::new);
        return ResponseEntity.ok(courseEnrollment);
    }

    @GetMapping("/{enrollmentId}/content")
    public ResponseEntity<EnrolmentWithCourseDTO> getContentByEnrollmentId(@PathVariable Long enrollmentId) {
        return ResponseEntity.ok(courseEnrollmentService.findEnrolmentWithCourseById(enrollmentId));
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

    @PostMapping("/{enrollmentId}/submit-quiz")
    public ResponseEntity<Void> submitQuiz(@PathVariable Long enrollmentId,
                                           @Valid @RequestBody QuizSubmitDTO quizSubmitDTO) {
        courseEnrollmentService.submitQuiz(enrollmentId, quizSubmitDTO);
        return ResponseEntity.ok().build();
    }

}
