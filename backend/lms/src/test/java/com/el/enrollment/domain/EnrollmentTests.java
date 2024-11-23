package com.el.enrollment.domain;


import com.el.TestFactory;
import com.el.common.exception.InputInvalidException;
import com.el.common.exception.ResourceNotFoundException;
import com.el.course.domain.QuestionType;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class EnrollmentTests {

    @Test
    void courseEnrollmentConstructor_ShouldCreatedEnrollmentWhenValid() {
        Enrollment enrollment = new Enrollment("user", 1L, "teacher", Set.of(
                new LessonProgress("Course Lesson 1", 1L), 
                new LessonProgress("Course Lesson 2", 2L)), 
                Set.of(1L, 2L));

        assertNull(enrollment.getEnrollmentDate());
        assertEquals(TestFactory.user, enrollment.getStudent());
        assertEquals(1L, enrollment.getCourseId());
        assertFalse(enrollment.getCompleted());
        assertEquals(2L, enrollment.getLessonProgresses().size());

        assertEquals(2, enrollment.getQuizIds().size());
        assertEquals(2L, enrollment.getProgress().totalLessons());
        assertEquals(0, enrollment.getProgress().completedLessons());
    }

    @Test
    void courseEnrollmentConstructor_ShouldThrowException_WhenLessonProgressesIsEmpty() {
        assertThrows(InputInvalidException.class, () -> {
            new Enrollment("user",
                    1L,
                    "teacher",
                    Set.of(), // Empty lesson progresses
                    Set.of(1L, 2L));
        });
    }

    @Test
    void courseEnrollmentConstructor_ShouldThrowException_WhenQuizIdsIsEmpty() {
        assertThrows(InputInvalidException.class, () -> {
            new Enrollment("user",
                    1L,
                    "teacher",
                    Set.of(new LessonProgress("Course Lesson 1", 1L), new LessonProgress("Course Lesson 2", 2L)),
                    Set.of()); // Empty quiz ids
        });
    }

    @Test
    void markLessonAsCompleted_ShouldMarkLessonAsCompleted_WhenLessonExists() {
        Enrollment enrollment = new Enrollment("user", 1L, "teacher", Set.of(
                new LessonProgress("Course Lesson 1", 1L), 
                new LessonProgress("Course Lesson 2", 2L)), 
                Set.of(1L, 2L));

        enrollment.markLessonAsCompleted(1L, "Lesson title 1");
        enrollment.markLessonAsCompleted(3L, "Lesson title 3");

        assertEquals(2, enrollment.getProgress().totalLessons());
        assertEquals(1, enrollment.getProgress().completedLessons());
        assertEquals(1, enrollment.getProgress().totalLessonBonus());
        assertEquals(2, enrollment.getProgress().totalQuizzes());
        assertEquals(0, enrollment.getProgress().passedQuizzes());
        assertNotNull(enrollment.findLessonProgressByLessonId(1L).getCompletedDate());
        assertFalse(enrollment.getCompleted());
    }

    @Test
    void markLessonAsCompleted_ShouldMarkLessonAsCompleted_AndBonusWhenLessonIdNotContainInLessonProgresses() {
        Enrollment enrollment = new Enrollment("user", 1L, "teacher",
                Set.of(new LessonProgress("Course Lesson 1", 1L)),
                Set.of(1L, 2L)); // quizIds

        // Bonus
        enrollment.markLessonAsCompleted(2L, "Lesson title 2");

        assertEquals(1, enrollment.getProgress().totalLessons());
        assertEquals(0, enrollment.getProgress().completedLessons());
        assertEquals(1, enrollment.getProgress().totalLessonBonus());
        assertEquals(2, enrollment.getProgress().totalQuizzes());
        assertEquals(0, enrollment.getProgress().passedQuizzes());
        assertFalse(enrollment.getCompleted());
    }

    @Test
    void markLessonAsCompleted_ShouldThrowException_WhenLessonAlreadyCompleted() {
        Enrollment enrollment = new Enrollment("user", 1L, "teacher", Set.of(
                new LessonProgress("Course Lesson 1", 1L), 
                new LessonProgress("Course Lesson 2", 2L)), 
                Set.of(1L, 2L));
        enrollment.markLessonAsCompleted(1L, "Lesson title");

        InputInvalidException e = assertThrows(InputInvalidException.class, () -> enrollment.markLessonAsCompleted(1L, "Lesson title"));
        assertEquals("LessonProgress is already completed.", e.getMessage());
    }

    @Test
    void markAllLessonsAsCompleted_ShouldOK() {
        Enrollment enrollment = new Enrollment("user", 1L, "teacher", Set.of(
                new LessonProgress("Course Lesson 1", 1L), 
                new LessonProgress("Course Lesson 2", 2L)), 
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
                new LessonProgress("Course Lesson 1", 1L), 
                new LessonProgress("Course Lesson 2", 2L)), 
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
                new LessonProgress("Course Lesson 1", 1L), 
                new LessonProgress("Course Lesson 2", 2L)), 
                Set.of(1L, 2L));

        InputInvalidException e = assertThrows(InputInvalidException.class, () -> enrollment.markLessonAsIncomplete(1L));
        assertEquals("LessonProgress is already incomplete.", e.getMessage());
    }

    @Test
    void addLessonProgress_ShouldThrowException_WhenLessonProgressIsNull() {
        Enrollment enrollment = new Enrollment("user", 1L, "teacher", Set.of(
                new LessonProgress("Course Lesson 1", 1L), 
                new LessonProgress("Course Lesson 2", 2L)), 
                Set.of(1L, 2L));

        InputInvalidException e = assertThrows(InputInvalidException.class, () -> enrollment.addLessonProgress(null));
        assertEquals("LessonProgress must not be null.", e.getMessage());
    }

    @Test
    void findLessonProgressByLessonId_ShouldThrowException_WhenLessonProgressNotFound() {
        Enrollment enrollment = new Enrollment("user", 1L, "teacher", Set.of(
                new LessonProgress("Course Lesson 1", 1L), 
                new LessonProgress("Course Lesson 2", 2L)), 
                Set.of(1L, 2L));

        ResourceNotFoundException e = assertThrows(ResourceNotFoundException.class,
                () -> enrollment.findLessonProgressByLessonId(3L));
        assertEquals("Resource not found", e.getMessage());
    }

    @Test
    void findLessonProgressByLessonId_ShouldThrowException_WhenLessonIdIsNull() {
        Enrollment enrollment = new Enrollment("user", 1L, "teacher", Set.of(
                new LessonProgress("Course Lesson 1", 1L), 
                new LessonProgress("Course Lesson 2", 2L)), 
                Set.of(1L, 2L));

        InputInvalidException e = assertThrows(InputInvalidException.class, () -> enrollment.findLessonProgressByLessonId(null));
        assertEquals("LessonId must not be null.", e.getMessage());
    }

}
