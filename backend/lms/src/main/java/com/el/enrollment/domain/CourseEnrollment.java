package com.el.enrollment.domain;

import com.el.common.exception.InputInvalidException;
import com.el.common.exception.ResourceNotFoundException;
import lombok.Getter;
import lombok.ToString;
import org.springframework.data.annotation.*;
import org.springframework.data.domain.AbstractAggregateRoot;
import org.springframework.data.relational.core.mapping.MappedCollection;
import org.springframework.data.relational.core.mapping.Table;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

@Table("course_enrollment")
@Getter
@ToString
public class CourseEnrollment extends AbstractAggregateRoot<CourseEnrollment> {
    @Id
    private Long id;
    private String student;
    private Long courseId;
    private String teacher;
    private Integer totalQuizzes;
    private Integer totalLessons;
    private LocalDateTime enrollmentDate;
    @MappedCollection(idColumn = "course_enrollment")
    private Set<LessonProgress> lessonProgresses = new HashSet<>();
    private Boolean completed;
    private Boolean reviewed = false;
    private LocalDateTime completedDate;
    private Certificate certificate;
    @MappedCollection(idColumn = "course_enrollment")
    private Set<QuizSubmission> quizSubmissions = new HashSet<>();
    @CreatedBy
    private String createdBy;
    @CreatedDate
    private LocalDateTime createdDate;
    @LastModifiedBy
    private String lastModifiedBy;
    @LastModifiedDate
    private LocalDateTime lastModifiedDate;

    public CourseEnrollment(String student, Long courseId, String teacher, Set<LessonProgress> lessonProgresses, Integer totalQuizzes) {
        if (student == null) throw new InputInvalidException("Student must not be null.");
        if (courseId == null) throw new InputInvalidException("CourseId must not be null.");
        if (teacher == null) throw new InputInvalidException("Teacher must not be null.");
        if (lessonProgresses == null || lessonProgresses.isEmpty())
            throw new InputInvalidException("LessonProgresses must not be null or empty.");
        if (totalQuizzes == null || totalQuizzes < 0)
            throw new InputInvalidException("TotalQuizzes must be a non-negative integer.");

        this.student = student;
        this.courseId = courseId;
        this.teacher = teacher;
        this.completed = false;
        this.totalQuizzes = totalQuizzes;
        this.totalLessons = 0;

        lessonProgresses.forEach(this::addLessonProgress);
    }

    public void markLessonAsCompleted(Long lessonId) {
        LessonProgress lessonProgress = lessonProgresses.stream()
                .filter(lp -> lp.getLessonId().equals(lessonId))
                .findFirst()
                .orElseThrow(ResourceNotFoundException::new);
        lessonProgress.markAsCompleted();
        checkCompleted();
    }

    public void markLessonAsIncomplete(Long lessonId) {
        if (this.completed)
            throw new InputInvalidException("You can't mark lesson as incomplete for a completed enrollment.");
        LessonProgress lessonProgress = lessonProgresses.stream()
                .filter(lp -> lp.getLessonId().equals(lessonId))
                .findFirst()
                .orElseThrow(ResourceNotFoundException::new);
        lessonProgress.markAsIncomplete();
        this.completed = false;
    }

    private void checkCompleted() {
        if (allLessonsCompleted() && allQuizSubmitPassed()) {
            this.completed = true;
            this.completedDate = LocalDateTime.now();
            registerEvent(new EnrolmentCompletedEvent(this.id, this.courseId, this.student));
        }
    }

    private boolean allLessonsCompleted() {
        long completedLesson = this.lessonProgresses.stream().filter(LessonProgress::isCompleted).count();
        return completedLesson == this.totalLessons;
    }

    private boolean allQuizSubmitPassed() {
        long passedQuizzes = this.quizSubmissions.stream().filter(QuizSubmission::isPassed).count();
        return passedQuizzes == this.totalQuizzes;
    }

    public Progress getProgress() {
        int totalLessons = this.lessonProgresses.size();
        int completedLessons = (int) this.lessonProgresses.stream().filter(LessonProgress::isCompleted).count();
        return new Progress(totalLessons, completedLessons);
    }

    public void addLessonProgress(LessonProgress lessonProgress) {
        if (lessonProgress == null) throw new InputInvalidException("LessonProgress must not be null.");
        lessonProgresses.add(lessonProgress);
        this.totalLessons ++;
    }

    public void addQuizSubmission(QuizSubmission quizSubmission) {
        if (quizSubmission == null) throw new InputInvalidException("QuizSubmission must not be null.");
        if (isSubmittedQuiz(quizSubmission.getQuizId())) {
            throw new InputInvalidException("QuizSubmission for this quiz already exists.");
        }
        quizSubmissions.add(quizSubmission);
        checkCompleted();
    }

    public boolean isSubmittedQuiz(Long quizId) {
        return quizSubmissions.stream().anyMatch(qs -> qs.getQuizId().equals(quizId));
    }

    public LessonProgress findLessonProgressByLessonId(Long lessonId) {
        if (lessonId == null) throw new InputInvalidException("LessonId must not be null.");
        return lessonProgresses.stream()
                .filter(lp -> lp.getLessonId().equals(lessonId))
                .findFirst()
                .orElseThrow(ResourceNotFoundException::new);
    }

    public void createCertificate(String fullName, String email, String courseTitle, String teacher) {
        // Create certificate
        if (!this.completed) {
            throw new InputInvalidException("You can't create certificate for an incomplete enrollment.");
        }

        this.certificate = new Certificate(fullName, email, this.student, this.courseId, courseTitle, teacher);
    }

    public void markAsEnrolled() {
        this.enrollmentDate = LocalDateTime.now();
        registerEvent(new EnrolmentCreatedEvent(teacher));
    }

    public void markAsReviewed() {
        if (reviewed) {
            throw new InputInvalidException("Course enrollment is already reviewed.");
        }
        reviewed = true;
    }

    public QuizSubmission getQuizSubmission(Long quizSubmissionId) {
        return quizSubmissions.stream()
                .filter(qs -> qs.getId().equals(quizSubmissionId))
                .findFirst()
                .orElseThrow(ResourceNotFoundException::new);
    }

    public void deleteQuizSubmission(Long quizId) {
        QuizSubmission quizSubmission = getQuizSubmission(quizId);
        quizSubmissions.remove(quizSubmission);
    }

    public record EnrolmentCompletedEvent(Long id, Long courseId, String student) {}
    public record EnrolmentCreatedEvent(String teacher) {}

}
