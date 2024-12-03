package com.el.enrollment.domain;

import com.el.common.config.DataAuditConfig;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.data.jdbc.DataJdbcTest;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertNotNull;

@Import({DataAuditConfig.class})
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@ActiveProfiles("integration")
@DataJdbcTest
class EnrollmentJdbcTests {

    @Autowired
    private EnrollmentRepository repository;

    @BeforeEach
    void setUp() {
        Enrollment enrollment = new Enrollment("user", 1L, "teacher", Set.of(
                new LessonProgress("Course Lesson 1", 1L, 1),
                new LessonProgress("Course Lesson 2", 2L, 2)),
                Set.of(1L, 2L));
        enrollment.markAsEnrolled();
        repository.save(enrollment);
    }

    @Test
    void testSave() {
        Enrollment enrollment = new Enrollment("user", 1L, "teacher", Set.of(
                new LessonProgress("Course Lesson 1", 1L, 1),
                new LessonProgress("Course Lesson 2", 2L, 2)),
                Set.of(1L, 2L));
        enrollment.markAsEnrolled();
        repository.save(enrollment);
        assertNotNull(enrollment.getId());
    }

    @Test
    void findAllCourseEnrollmentDTOsByStudent() {
        repository.findAllCourseEnrollmentDTOsByStudent("student", 0, 10);
    }


}
