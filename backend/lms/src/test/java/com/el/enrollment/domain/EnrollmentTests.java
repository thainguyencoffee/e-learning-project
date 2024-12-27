package com.el.enrollment.domain;


import com.el.TestFactory;
import com.el.common.Currencies;
import com.el.common.exception.InputInvalidException;
import com.el.common.exception.ResourceNotFoundException;
import com.el.course.domain.QuestionType;
import org.javamoney.moneta.Money;
import org.junit.jupiter.api.Test;

import javax.money.MonetaryAmount;
import java.time.LocalDateTime;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.spy;

class EnrollmentTests {

    @Test
    void enrollmentConstructor_ShouldCreatedEnrollmentWhenValid() {
        Enrollment enrollment = new Enrollment("user", 1L, "teacher", Set.of(
                new LessonProgress("Course Lesson 1", 1L, 1),
                new LessonProgress("Course Lesson 2", 2L, 2)),
                Set.of(1L, 2L));

        assertNull(enrollment.getEnrollmentDate());
        assertEquals(TestFactory.user, enrollment.getStudent());
        assertEquals(1L, enrollment.getCourseId());
        assertFalse(enrollment.getCompleted());
        assertEquals(2L, enrollment.getLessonProgresses().size());

        assertEquals(2, enrollment.getQuizIds().size());
        assertFalse(enrollment.getChangedCourse());
        assertEquals(2L, enrollment.getProgress().totalLessons());
        assertEquals(0, enrollment.getProgress().completedLessons());
    }

    @Test
    void enrollmentConstructor_ShouldThrowException_WhenLessonProgressesIsEmpty() {
        assertThrows(InputInvalidException.class, () -> {
            new Enrollment("user",
                    1L,
                    "teacher",
                    Set.of(), // Empty lesson progresses
                    Set.of(1L, 2L));
        });
    }

    @Test
    void enrollmentConstructor_ShouldThrowException_WhenQuizIdsIsEmpty() {
        assertThrows(InputInvalidException.class, () -> {
            new Enrollment("user",
                    1L,
                    "teacher",
                    Set.of(new LessonProgress("Course Lesson 1", 1L, 1),
                            new LessonProgress("Course Lesson 2", 2L, 2)),
                    Set.of()); // Empty quiz ids
        });
    }

    @Test
    void markLessonAsCompleted_ShouldMarkLessonAsCompleted_WhenLessonExists() {
        Enrollment enrollment = new Enrollment("user", 1L, "teacher", Set.of(
                new LessonProgress("Course Lesson 1", 1L, 1),
                new LessonProgress("Course Lesson 2", 2L, 2)),
                Set.of(1L, 2L));

        enrollment.markLessonAsCompleted(1L, "Lesson title 1");
        enrollment.markLessonAsCompleted(2L, "Lesson title 2");
        enrollment.markLessonAsCompleted(3L, "Lesson title 3");

        assertEquals(2, enrollment.getProgress().totalLessons());
        assertEquals(2, enrollment.getProgress().completedLessons());
        assertEquals(1, enrollment.getProgress().totalLessonBonus());
        assertEquals(2, enrollment.getProgress().totalQuizzes());
        assertEquals(0, enrollment.getProgress().passedQuizzes());
        assertNotNull(enrollment.findLessonProgressByLessonId(1L).getCompletedDate());
        assertFalse(enrollment.getCompleted());
    }

    @Test
    void markLessonAsCompleted_ShouldMarkLessonAsCompleted_AndBonusWhenLessonIdNotContainInLessonProgresses() {
        Enrollment enrollment = new Enrollment("user", 1L, "teacher",
                Set.of(new LessonProgress("Course Lesson 1", 1L, 1)),
                Set.of(1L, 2L)); // quizIds

        enrollment.markLessonAsCompleted(1L, "Lesson title 1");
        // Bonus
        enrollment.markLessonAsCompleted(2L, "Lesson title 2");

        assertEquals(1, enrollment.getProgress().totalLessons());
        assertEquals(1, enrollment.getProgress().completedLessons());
        assertEquals(1, enrollment.getProgress().totalLessonBonus());
        assertEquals(2, enrollment.getProgress().totalQuizzes());
        assertEquals(0, enrollment.getProgress().passedQuizzes());
        assertFalse(enrollment.getCompleted());
    }

    @Test
    void markLessonAsCompleted_ShouldThrowWhenDoesntFollowTheProgress() {
        Enrollment enrollment = new Enrollment("user", 1L, "teacher",
                Set.of(new LessonProgress("Course Lesson 1", 1L, 1),
                        new LessonProgress("Course Lesson 2", 2L, 2)),
                Set.of(1L, 2L)); // quizIds

        assertThrows(InputInvalidException.class, () -> enrollment.markLessonAsCompleted(2L, "Lesson title 2"));
        // bonus
        assertThrows(InputInvalidException.class, () -> enrollment.markLessonAsCompleted(3L, "Lesson title 3 (bonus)"));
    }

    @Test
    void markLessonAsCompleted_ShouldThrowException_WhenLessonAlreadyCompleted() {
        Enrollment enrollment = new Enrollment("user", 1L, "teacher", Set.of(
                new LessonProgress("Course Lesson 1", 1L, 1),
                new LessonProgress("Course Lesson 2", 2L, 2)),
                Set.of(1L, 2L));
        enrollment.markLessonAsCompleted(1L, "Lesson title");

        InputInvalidException e = assertThrows(InputInvalidException.class, () -> enrollment.markLessonAsCompleted(1L, "Lesson title"));
        assertEquals("LessonProgress is already completed.", e.getMessage());
    }

    @Test
    void markAllLessonsAsCompleted_ShouldOK() {
        Enrollment enrollment = new Enrollment("user", 1L, "teacher", Set.of(
                new LessonProgress("Course Lesson 1", 1L, 1),
                new LessonProgress("Course Lesson 2", 2L, 2)),
                Set.of(1L, 2L));
        enrollment.markLessonAsCompleted(1L, "Lesson title");
        enrollment.markLessonAsCompleted(2L, "Lesson title 2");

        // bonus
        enrollment.markLessonAsCompleted(3L, "Lesson title 3");

        // Just need to mark all lessons as completed to complete the enrollment, quizzes don't need
        long quizId1 = 1L;
        Set<QuizAnswer> answers1 = Set.of(
                new QuizAnswer(1L, 1L, QuestionType.SINGLE_CHOICE),
                new QuizAnswer(2L, 2L, QuestionType.SINGLE_CHOICE));
        QuizSubmission quizSubmission1 = new QuizSubmission(quizId1, 1L, answers1, 5, true);
        enrollment.addQuizSubmission(quizSubmission1);

        long quizId2 = 2L;
        Set<QuizAnswer> answers2 = Set.of(
                new QuizAnswer(1L, 1L, QuestionType.SINGLE_CHOICE),
                new QuizAnswer(2L, 2L, QuestionType.SINGLE_CHOICE));
        QuizSubmission quizSubmission2 = new QuizSubmission(quizId2, 2L, answers2, 4, true);
        enrollment.addQuizSubmission(quizSubmission2);

        // Bonus
        long quizId3 = 3L;
        Set<QuizAnswer> answers3 = Set.of(
                new QuizAnswer(1L, 1L, QuestionType.SINGLE_CHOICE),
                new QuizAnswer(2L, 2L, QuestionType.SINGLE_CHOICE));
        QuizSubmission quizSubmission3 = new QuizSubmission(quizId3, 3L, answers3, 4, true);
        enrollment.addQuizSubmission(quizSubmission3);

        assertTrue(enrollment.getCompleted());
        assertEquals(2, enrollment.getProgress().totalLessons());
        assertEquals(2, enrollment.getProgress().completedLessons());
        assertEquals(1, enrollment.getProgress().totalLessonBonus());
        assertEquals(2, enrollment.getProgress().totalQuizzes());
        assertEquals(2, enrollment.getProgress().passedQuizzes());
        assertEquals(1, enrollment.getProgress().totalQuizBonus());

        InputInvalidException e = assertThrows(InputInvalidException.class, () -> enrollment.markLessonAsIncomplete(1L));
        assertEquals("You can't mark lesson as incomplete for a completed enrollment.", e.getMessage());
    }

    @Test
    void markLessonAsIncomplete_ShouldMarkLessonAsIncomplete_WhenLessonExists() {
        Enrollment enrollment = new Enrollment("user", 1L, "teacher", Set.of(
                new LessonProgress("Course Lesson 1", 1L, 1),
                new LessonProgress("Course Lesson 2", 2L, 2)),
                Set.of(1L, 2L));
        enrollment.markLessonAsCompleted(1L, "Lesson title");

        enrollment.markLessonAsIncomplete(1L);

        assertEquals(2, enrollment.getProgress().totalLessons());
        assertEquals(0, enrollment.getProgress().completedLessons());
        assertEquals(0, enrollment.getLessonProgresses().stream().filter(LessonProgress::isCompleted).count());
        assertNull(enrollment.findLessonProgressByLessonId(1L).getCompletedDate());
        assertFalse(enrollment.getCompleted());
    }

    @Test
    void markLessonAsIncomplete_ShouldThrowException_WhenLessonAlreadyIncomplete() {
        Enrollment enrollment = new Enrollment("user", 1L, "teacher", Set.of(
                new LessonProgress("Course Lesson 1", 1L, 1),
                new LessonProgress("Course Lesson 2", 2L, 2)),
                Set.of(1L, 2L));

        InputInvalidException e = assertThrows(InputInvalidException.class, () -> enrollment.markLessonAsIncomplete(1L));
        assertEquals("LessonProgress is already incomplete.", e.getMessage());
    }

    @Test
    void addLessonProgress_ShouldThrowException_WhenLessonProgressIsNull() {
        Enrollment enrollment = new Enrollment("user", 1L, "teacher", Set.of(
                new LessonProgress("Course Lesson 1", 1L, 1),
                new LessonProgress("Course Lesson 2", 2L, 2)),
                Set.of(1L, 2L));

        InputInvalidException e = assertThrows(InputInvalidException.class, () -> enrollment.addLessonProgress(null));
        assertEquals("LessonProgress must not be null.", e.getMessage());
    }

    @Test
    void findLessonProgressByLessonId_ShouldThrowException_WhenLessonProgressNotFound() {
        Enrollment enrollment = new Enrollment("user", 1L, "teacher", Set.of(
                new LessonProgress("Course Lesson 1", 1L, 1),
                new LessonProgress("Course Lesson 2", 2L, 2)),
                Set.of(1L, 2L));

        ResourceNotFoundException e = assertThrows(ResourceNotFoundException.class,
                () -> enrollment.findLessonProgressByLessonId(3L));
        assertEquals("Resource not found", e.getMessage());
    }

    @Test
    void findLessonProgressByLessonId_ShouldThrowException_WhenLessonIdIsNull() {
        Enrollment enrollment = new Enrollment("user", 1L, "teacher", Set.of(
                new LessonProgress("Course Lesson 1", 1L, 1),
                new LessonProgress("Course Lesson 2", 2L, 2)),
                Set.of(1L, 2L));

        InputInvalidException e = assertThrows(InputInvalidException.class, () -> enrollment.findLessonProgressByLessonId(null));
        assertEquals("LessonId must not be null.", e.getMessage());
    }

    @Test
    void requestChangeCourse_ShouldChangeCourse_WhenValid() throws AdditionalPaymentRequiredException {
        Enrollment enrollment = spy(new Enrollment("user", 1L, "teacher", Set.of(
                new LessonProgress("Course Lesson 1", 1L, 1),
                new LessonProgress("Course Lesson 2", 2L, 2)),
                Set.of(1L, 2L)));
        enrollment.markAsEnrolled();

        LocalDateTime enrollmentDate = enrollment.getEnrollmentDate();
        MonetaryAmount oldCoursePrice = Money.of(100, Currencies.USD);
        MonetaryAmount newCoursePrice = Money.of(80, Currencies.USD);

        enrollment.requestChangeCourse(2L, oldCoursePrice, newCoursePrice, "newTeacher", Set.of(
                        new LessonProgress("New Course Lesson 1", 3L, 3),
                        new LessonProgress("New Course Lesson 2", 4L, 4)),
                Set.of(3L, 4L, 5L));

        assertEquals(2L, enrollment.getCourseId());
        assertEquals("newTeacher", enrollment.getTeacher());
        assertEquals(2, enrollment.getLessonProgresses().size());
        assertEquals(3, enrollment.getQuizIds().size());
        assertTrue(enrollment.getChangedCourse());
        assertNotEquals(enrollment.getEnrollmentDate(), enrollmentDate);
    }

    @Test
    void requestChangeCourse_ShouldThrowException_WhenAdditionalPaymentRequired() {
        Enrollment enrollment = new Enrollment("user", 1L, "teacher", Set.of(
                new LessonProgress("Course Lesson 1", 1L, 1),
                new LessonProgress("Course Lesson 2", 2L, 2)),
                Set.of(1L, 2L));
        enrollment.markAsEnrolled();

        MonetaryAmount oldCoursePrice = Money.of(100, Currencies.USD);
        MonetaryAmount newCoursePrice = Money.of(120, Currencies.USD);

        assertThrows(AdditionalPaymentRequiredException.class, () -> enrollment.requestChangeCourse(2L, oldCoursePrice, newCoursePrice, "newTeacher", Set.of(
                        new LessonProgress("New Course Lesson 1", 3L, 3),
                        new LessonProgress("New Course Lesson 2", 4L, 4)),
                Set.of(3L, 4L)));
    }

    @Test
    void changeCourse_ShouldChangeCourse_WhenValid() {
        Enrollment enrollment = spy(new Enrollment("user", 1L, "teacher", Set.of(
                new LessonProgress("Course Lesson 1", 1L, 1),
                new LessonProgress("Course Lesson 2", 2L, 2)),
                Set.of(1L, 2L)));
        enrollment.markAsEnrolled();

        enrollment.changeCourse(2L, "newTeacher", Set.of(
                        new LessonProgress("New Course Lesson 1", 3L, 3)),
                Set.of(3L, 4L, 5L));

        assertEquals(2L, enrollment.getCourseId());
        assertEquals("newTeacher", enrollment.getTeacher());
        assertEquals(1, enrollment.getLessonProgresses().size());
        assertEquals(3, enrollment.getQuizIds().size());
        assertTrue(enrollment.getChangedCourse());
        assertNotNull(enrollment.getEnrollmentDate());
    }

    @Test
    void changeCourse_ShouldThrowException_WhenNewCourseIdIsSameAsCurrent() {
        Enrollment enrollment = new Enrollment("user", 1L, "teacher", Set.of(
                new LessonProgress("Course Lesson 1", 1L, 1),
                new LessonProgress("Course Lesson 2", 2L, 2)),
                Set.of(1L, 2L));

        assertThrows(InputInvalidException.class, () -> enrollment.changeCourse(1L, "newTeacher", Set.of(
                        new LessonProgress("New Course Lesson 1", 3L, 3),
                        new LessonProgress("New Course Lesson 2", 4L, 4)),
                Set.of(3L, 4L)));
    }

}
