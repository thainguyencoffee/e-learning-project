package com.el.enrollment.application;

import com.el.enrollment.application.dto.CourseEnrollmentDTO;
import com.el.enrollment.application.dto.EnrolmentWithCourseDTO;
import com.el.enrollment.application.dto.QuizDetailDTO;
import com.el.enrollment.web.dto.QuizSubmitDTO;
import com.el.enrollment.domain.Enrollment;
import com.el.enrollment.domain.QuizSubmission;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface CourseEnrollmentService {

    List<CourseEnrollmentDTO> findAllCourseEnrollments(Pageable pageable);

    Enrollment findCourseEnrollmentByCourseIdAndStudent(Long courseId, String student);

    Enrollment findCourseEnrollmentById(Long id);

    EnrolmentWithCourseDTO findEnrolmentWithCourseById(Long id);

    /**
     * When OrderPaid event is received, this method is called to enroll the student in the course.
     * Result:
     * {@link Enrollment} created with a set of
     * {@link com.el.enrollment.domain.LessonProgress} for each lesson in the course.
     * Each LessonProgress is initialized with completed flag = false.
    * */
    void enrollment(String student, Long courseId);

    void markLessonAsCompleted(Long enrollmentId, Long courseId, Long lessonId);

    void markLessonAsIncomplete(Long enrollmentId, Long courseId, Long lessonId);

    void createCertificate(Long id, String student, Long courseId);

    boolean isSubmittedQuiz(Long enrollmentId, Long quizId);

    QuizSubmission getQuizSubmission(Long enrollmentId, Long quizSubmissionId);

    Long submitQuiz(Long enrollmentId, QuizSubmitDTO quizSubmitDTO);

    void markAsReviewed(Long courseId, String student);

    QuizDetailDTO findQuizByIdAndQuizId(Long enrollmentId, Long quizId);

    void deleteQuizSubmission(Long enrollmentId, Long quizSubmissionId);

}
