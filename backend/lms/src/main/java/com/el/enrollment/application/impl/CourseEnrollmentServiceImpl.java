package com.el.enrollment.application.impl;

import com.el.common.RolesBaseUtil;
import com.el.common.auth.application.UsersManagement;
import com.el.common.exception.AccessDeniedException;
import com.el.common.exception.ResourceNotFoundException;
import com.el.course.application.CourseQueryService;
import com.el.course.application.CourseService;
import com.el.course.application.dto.PublishedCourseDTO;
import com.el.course.domain.Course;
import com.el.course.domain.QuizCalculationResult;
import com.el.course.domain.Quiz;
import com.el.enrollment.application.dto.CourseEnrollmentDTO;
import com.el.enrollment.application.CourseEnrollmentService;
import com.el.enrollment.application.dto.EnrollmentWithCourseDTO;
import com.el.enrollment.application.dto.QuizDetailDTO;
import com.el.enrollment.web.dto.QuizSubmitDTO;
import com.el.enrollment.domain.Enrollment;
import com.el.enrollment.domain.EnrollmentRepository;
import com.el.enrollment.domain.LessonProgress;
import com.el.enrollment.domain.QuizSubmission;
import lombok.extern.slf4j.Slf4j;
import org.keycloak.representations.idm.UserRepresentation;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
@Service
public class CourseEnrollmentServiceImpl implements CourseEnrollmentService {

    private final CertificateServiceS3Storage certificateServiceS3Storage;
    private final EnrollmentRepository repository;
    private final CourseQueryService courseQueryService;
    private final CourseService courseService;
    private final UsersManagement usersManagement;
    private final RolesBaseUtil rolesBaseUtil;

    public CourseEnrollmentServiceImpl(CertificateServiceS3Storage certificateServiceS3Storage, EnrollmentRepository repository,
                                       CourseQueryService courseQueryService, CourseService courseService, UsersManagement usersManagement,
                                       RolesBaseUtil rolesBaseUtil) {
        this.certificateServiceS3Storage = certificateServiceS3Storage;
        this.repository = repository;
        this.courseQueryService = courseQueryService;
        this.courseService = courseService;
        this.usersManagement = usersManagement;
        this.rolesBaseUtil = rolesBaseUtil;
    }

    @Override
    public List<CourseEnrollmentDTO> findAllCourseEnrollments(Pageable pageable) {
        int page = pageable.getPageNumber();
        int size = pageable.getPageSize();
        String currentUsername = rolesBaseUtil.getCurrentPreferredUsernameFromJwt();

        if (rolesBaseUtil.isAdmin()) {
            return repository.findAllCourseEnrollmentDTOs(page, size);
        } else if (rolesBaseUtil.isTeacher()) {
            return repository.findAllCourseEnrollmentDTOsByTeacher(currentUsername, page, size);
        } else if (rolesBaseUtil.isUser()) {
            return repository.findAllCourseEnrollmentDTOsByStudent(currentUsername, page, size);
        }
        throw new AccessDeniedException("Access denied");
    }

    @Override
    public Enrollment findCourseEnrollmentByCourseIdAndStudent(Long courseId, String student) {
        return repository.findByCourseIdAndStudent(courseId, student)
                .orElseThrow(ResourceNotFoundException::new);
    }

    @Override
    public Enrollment findCourseEnrollmentById(Long id) {
        String currentUsername = rolesBaseUtil.getCurrentPreferredUsernameFromJwt();
        if (rolesBaseUtil.isAdmin()) {
            return repository.findById(id)
                    .orElseThrow(ResourceNotFoundException::new);
        } else if (rolesBaseUtil.isTeacher()) {
            return repository.findByIdAndTeacher(id, currentUsername)
                    .orElseThrow(ResourceNotFoundException::new);
        } else if (rolesBaseUtil.isUser()) {
            return repository.findByIdAndStudent(id, currentUsername)
                    .orElseThrow(ResourceNotFoundException::new);
        }
        throw new AccessDeniedException("Access denied");
    }

    @Override
    public EnrollmentWithCourseDTO findEnrollmentWithCourseById(Long id) {
        Enrollment enrollment;
        if (rolesBaseUtil.isAdmin() || rolesBaseUtil.isTeacher()) {
            enrollment = repository.findById(id)
                    .orElseThrow(ResourceNotFoundException::new);
        } else {
            String student = rolesBaseUtil.getCurrentPreferredUsernameFromJwt();
            enrollment = repository.findByIdAndStudent(id, student)
                    .orElseThrow(ResourceNotFoundException::new);
        }
        Course course = courseQueryService.findPublishedCourseById(enrollment.getCourseId());
        return EnrollmentWithCourseDTO.of(enrollment, course);
    }

    public void enrollment(String student, Long courseId) {
        Course course = courseQueryService.findPublishedCourseById(courseId);
        Set<LessonProgress> lessonProgresses = course.getLessonIdAndTitleMap().entrySet()
                .stream()
                .map(entry -> new LessonProgress(entry.getValue(), entry.getKey()))
                .collect(Collectors.toSet());

        Enrollment enrollment = new Enrollment(student, courseId, course.getTeacher(), lessonProgresses, course.getQuizIds());
        enrollment.markAsEnrolled();
        repository.save(enrollment);
    }

    @Override
    public void markLessonAsCompleted(Long enrollmentId, Long courseId, Long lessonId) {
        checkAccess();
        var lesson = courseQueryService.findLessonByCourseIdAndLessonId(courseId, lessonId);

        Enrollment enrollment = findCourseEnrollmentById(enrollmentId);
        enrollment.markLessonAsCompleted(lesson.getId(), lesson.getTitle());
        repository.save(enrollment);
    }

    @Override
    public void markLessonAsIncomplete(Long enrollmentId, Long courseId, Long lessonId) {
        checkAccess();
        Enrollment enrollment = findCourseEnrollmentById(enrollmentId);
        enrollment.markLessonAsIncomplete(lessonId);
        repository.save(enrollment);
    }

    @Override
    public void createCertificate(Long id, String student, Long courseId) {
        Enrollment enrollment = repository.findByIdAndStudent(id, student)
                .orElseThrow(ResourceNotFoundException::new);
        PublishedCourseDTO courseInfo =
                courseQueryService.findCoursePublishedById(courseId);
        UserRepresentation userRepresentation = usersManagement.getUser(student);

        enrollment.createCertificate(getFullName(userRepresentation),
                userRepresentation.getEmail(),
                courseInfo.title(),
                courseInfo.teacher());

        certificateServiceS3Storage.createCertUrl(enrollment.getCertificate());
        repository.save(enrollment);
    }

    @Override
    public boolean isSubmittedQuiz(Long enrollmentId, Long quizId) {
        Enrollment enrollment = findCourseEnrollmentById(enrollmentId);
        return enrollment.isSubmittedQuiz(quizId);
    }

    @Override
    public QuizSubmission getQuizSubmission(Long enrollmentId, Long quizSubmissionId) {
        Enrollment enrollment = findCourseEnrollmentById(enrollmentId);
        return enrollment.getQuizSubmission(quizSubmissionId);
    }

    @Override
    public Long submitQuiz(Long enrollmentId, QuizSubmitDTO quizSubmitDTO) {
        Enrollment enrollment = findCourseEnrollmentById(enrollmentId);
        log.info("Found enrollment: {}", enrollment);
        QuizCalculationResult calculationResult = courseService.calculateQuizScore(enrollment.getCourseId(), quizSubmitDTO.quizId(), quizSubmitDTO.getAnswers());
        log.info("Calculation result: {}", calculationResult);
        QuizSubmission quizSubmission = new QuizSubmission(quizSubmitDTO.quizId(), calculationResult.afterLessonId(),
                quizSubmitDTO.toQuizAnswers(), calculationResult.score(), calculationResult.passed());

        enrollment.addQuizSubmission(quizSubmission);
        repository.save(enrollment);
        return quizSubmission.getId();
    }

    @Override
    public void markAsReviewed(Long courseId, String student) {
        Enrollment enrollment = findCourseEnrollmentByCourseIdAndStudent(courseId, student);
        enrollment.markAsReviewed();
        repository.save(enrollment);
    }

    @Override
    public QuizDetailDTO findQuizByIdAndQuizId(Long enrollmentId, Long quizId) {
        Quiz quiz = courseQueryService.findQuizByQuizId(quizId);
        return QuizDetailDTO.fromQuiz(quiz);
    }

    @Override
    public void deleteQuizSubmission(Long enrollmentId, Long quizSubmissionId) {
        Enrollment enrollment = findCourseEnrollmentById(enrollmentId);
        enrollment.deleteQuizSubmission(quizSubmissionId);
        repository.save(enrollment);
    }

    private String getFullName(UserRepresentation userRepresentation) {
        return userRepresentation.getFirstName() + " " + userRepresentation.getLastName();
    }

    private void checkAccess() {
        if (rolesBaseUtil.isTeacher() || rolesBaseUtil.isAdmin()) {
            throw new AccessDeniedException("You are not allowed to write, only students can write. You just can read.");
        }
    }

}
