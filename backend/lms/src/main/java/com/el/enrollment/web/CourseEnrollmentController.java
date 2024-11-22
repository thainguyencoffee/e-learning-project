package com.el.enrollment.web;

import com.el.common.exception.InputInvalidException;
import com.el.common.exception.ResourceNotFoundException;
import com.el.enrollment.application.dto.*;
import com.el.enrollment.application.CourseEnrollmentService;
import com.el.enrollment.domain.CourseEnrollment;
import com.el.enrollment.domain.CourseEnrollmentRepository;
import com.el.enrollment.domain.QuizSubmission;
import com.el.enrollment.web.dto.LessonMarkRequest;
import com.el.enrollment.web.dto.QuizSubmitDTO;
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

    @PutMapping("/{enrollmentId}/mark-lesson")
    public ResponseEntity<Void> markLessonAsCompleted(@PathVariable Long enrollmentId,
                                                      @RequestBody LessonMarkRequest lessonMarkRequest) {
        if (!LessonMarkRequest.MarkType.COMPLETED.equals(lessonMarkRequest.mark()) && !LessonMarkRequest.MarkType.INCOMPLETE.equals(lessonMarkRequest.mark())) {
            throw new InputInvalidException("Mark value must be 'completed' or 'incomplete' only.");
        }
        if (LessonMarkRequest.MarkType.COMPLETED.equals(lessonMarkRequest.mark())) {
            courseEnrollmentService.markLessonAsCompleted(enrollmentId, lessonMarkRequest.courseId(), lessonMarkRequest.lessonId());
        } else {
            courseEnrollmentService.markLessonAsIncomplete(enrollmentId, lessonMarkRequest.courseId(), lessonMarkRequest.lessonId());
        }
        return ResponseEntity.ok().build();
    }

    @GetMapping("/{enrollmentId}/quizzes/{quizId}")
    public ResponseEntity<QuizDetailDTO> getQuiz(@PathVariable Long enrollmentId,
                                        @PathVariable Long quizId) {
        QuizDetailDTO quizDetailDTO = courseEnrollmentService.findQuizByIdAndQuizId(enrollmentId, quizId);
        return ResponseEntity.ok(quizDetailDTO);
    }

    @GetMapping("/{enrollmentId}/is-submitted-quiz")
    public ResponseEntity<Boolean> isSubmittedQuiz(@PathVariable Long enrollmentId,
                                                   @RequestParam(name = "quizId") Long quizId) {
        return ResponseEntity.ok(courseEnrollmentService.isSubmittedQuiz(enrollmentId, quizId));
    }

    @GetMapping("/{enrollmentId}/quizzes/{quizSubmissionId}/submission")
    public ResponseEntity<QuizSubmission> getQuizSubmission(@PathVariable Long enrollmentId,
                                                           @PathVariable Long quizSubmissionId) {
        return ResponseEntity.ok(courseEnrollmentService.getQuizSubmission(enrollmentId, quizSubmissionId));
    }

    @DeleteMapping("/{enrollmentId}/quizzes/{quizSubmissionId}/submission")
    public ResponseEntity<Void> deleteQuizSubmission(@PathVariable Long enrollmentId,
                                                     @PathVariable Long quizSubmissionId) {
        courseEnrollmentService.deleteQuizSubmission(enrollmentId, quizSubmissionId);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{enrollmentId}/submit-quiz")
    public ResponseEntity<Long> submitQuiz(@PathVariable Long enrollmentId,
                                                            @Valid @RequestBody QuizSubmitDTO quizSubmitDTO) {
        return ResponseEntity.ok(courseEnrollmentService.submitQuiz(enrollmentId, quizSubmitDTO));
    }


}
