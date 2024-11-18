package com.el.enrollment.domain;

import com.el.TestFactory;
import com.el.common.config.DataAuditConfig;
import com.el.enrollment.application.dto.CourseEnrollmentDTO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.data.jdbc.DataJdbcTest;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.Page;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@Import({DataAuditConfig.class})
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@ActiveProfiles("integration")
@DataJdbcTest
class CourseEnrollmentJdbcTests {

    @Autowired
    private CourseEnrollmentRepository repository;

    @BeforeEach
    void setUp() {
        CourseEnrollment courseEnrollment = TestFactory.createDefaultCourseEnrollment();
        courseEnrollment.markAsEnrolled();
        repository.save(courseEnrollment);
    }

    @Test
    void testSave() {
        CourseEnrollment courseEnrollment = TestFactory.createDefaultCourseEnrollment();
        courseEnrollment.markAsEnrolled();
        repository.save(courseEnrollment);
        assertNotNull(courseEnrollment.getId());
    }

    @Test
    void findAllCourseEnrollmentDTOsByStudent() {
        repository.findAllCourseEnrollmentDTOsByStudent("student", 0, 10);
    }


}
