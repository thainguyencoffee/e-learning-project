package com.el.enrollment.domain;


import com.el.TestFactory;
import com.el.common.exception.InputInvalidException;
import com.el.common.exception.ResourceNotFoundException;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class CourseEnrollmentTests {

    @Test
    void courseEnrollmentConstructor_ShouldCreatedEnrollmentWhenValid() {
        CourseEnrollment enrollment = TestFactory.createDefaultCourseEnrollment();

        assertNotNull(enrollment.getEnrollmentDate());
        assertEquals(TestFactory.user, enrollment.getStudent());
        assertEquals(1L, enrollment.getCourseId());
        assertFalse(enrollment.getCompleted());
        assertEquals(2L, enrollment.getLessonProgresses().size());

        assertEquals(2L, enrollment.getProgress().totalLessons());
        assertEquals(0, enrollment.getProgress().completedLessons());
    }

    @Test
    void courseEnrollmentConstructor_ShouldThrowException_WhenLessonProgressesIsEmpty() {
        assertThrows(InputInvalidException.class, TestFactory::createCourseEnrollmentWithEmptyLessonProgress);
    }

    @Test
    void markLessonAsCompleted_ShouldMarkLessonAsCompleted_WhenLessonExists() {
        CourseEnrollment enrollment = TestFactory.createDefaultCourseEnrollment();

        enrollment.markLessonAsCompleted(1L);

        assertEquals(2, enrollment.getProgress().totalLessons());
        assertEquals(1, enrollment.getProgress().completedLessons());
        assertEquals(1, enrollment.getLessonProgresses().stream().filter(LessonProgress::isCompleted).count());
        assertNotNull(enrollment.findLessonProgressByLessonId(1L).getCompletedDate());
        assertFalse(enrollment.getCompleted());
    }

    @Test
    void markLessonAsCompleted_ShouldThrowException_WhenLessonAlreadyCompleted() {
        CourseEnrollment enrollment = TestFactory.createDefaultCourseEnrollment();
        enrollment.markLessonAsCompleted(1L);

        InputInvalidException e = assertThrows(InputInvalidException.class, () -> enrollment.markLessonAsCompleted(1L));
        assertEquals("LessonProgress is already completed.", e.getMessage());
    }

    @Test
    void markAllLessonsAsCompleted_ShouldMarkEnrollmentAsCompleted_WhenAllLessonsCompleted() {
        CourseEnrollment enrollment = TestFactory.createDefaultCourseEnrollment();
        enrollment.markLessonAsCompleted(1L);
        enrollment.markLessonAsCompleted(2L);

        assertTrue(enrollment.getCompleted());
        assertEquals(2, enrollment.getProgress().totalLessons());
        assertEquals(2, enrollment.getProgress().completedLessons());
        assertTrue(enrollment.getLessonProgresses().stream().allMatch(LessonProgress::isCompleted));
    }

    @Test
    void markLessonAsIncomplete_ShouldMarkLessonAsIncomplete_WhenLessonExists() {
        CourseEnrollment enrollment = TestFactory.createDefaultCourseEnrollment();
        enrollment.markLessonAsCompleted(1L);

        enrollment.markLessonAsIncomplete(1L);

        assertEquals(2, enrollment.getProgress().totalLessons());
        assertEquals(0, enrollment.getProgress().completedLessons());
        assertEquals(0, enrollment.getLessonProgresses().stream().filter(LessonProgress::isCompleted).count());
        assertNull(enrollment.findLessonProgressByLessonId(1L).getCompletedDate());
        assertFalse(enrollment.getCompleted());
    }

    @Test
    void markLessonAsIncomplete_ShouldThrowException_WhenLessonAlreadyIncomplete() {
        CourseEnrollment enrollment = TestFactory.createDefaultCourseEnrollment();

        InputInvalidException e = assertThrows(InputInvalidException.class, () -> enrollment.markLessonAsIncomplete(1L));
        assertEquals("LessonProgress is already incomplete.", e.getMessage());
    }

    @Test
    void whenCourseEnrollmentAsComplete_DoesNotMarkAsIncomplete() {
        CourseEnrollment enrollment = TestFactory.createDefaultCourseEnrollment();
        enrollment.markLessonAsCompleted(1L);
        enrollment.markLessonAsCompleted(2L);

        InputInvalidException e = assertThrows(InputInvalidException.class, () -> enrollment.markLessonAsIncomplete(1L));
        assertEquals("You can't mark lesson as incomplete for a completed enrollment.", e.getMessage());
    }

    @Test
    void addLessonProgress_ShouldThrowException_WhenLessonProgressIsNull() {
        CourseEnrollment enrollment = TestFactory.createDefaultCourseEnrollment();

        InputInvalidException e = assertThrows(InputInvalidException.class, () -> enrollment.addLessonProgress(null));
        assertEquals("LessonProgress must not be null.", e.getMessage());
    }

    @Test
    void findLessonProgressByLessonId_ShouldThrowException_WhenLessonProgressNotFound() {
        CourseEnrollment enrollment = TestFactory.createDefaultCourseEnrollment();

        ResourceNotFoundException e = assertThrows(ResourceNotFoundException.class,
                () -> enrollment.findLessonProgressByLessonId(3L));
        assertEquals("Resource not found", e.getMessage());
    }

    @Test
    void findLessonProgressByLessonId_ShouldThrowException_WhenLessonIdIsNull() {
        CourseEnrollment enrollment = TestFactory.createDefaultCourseEnrollment();

        InputInvalidException e = assertThrows(InputInvalidException.class, () -> enrollment.findLessonProgressByLessonId(null));
        assertEquals("LessonId must not be null.", e.getMessage());
    }

}
