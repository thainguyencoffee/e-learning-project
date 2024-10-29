package com.el.enrollment.domain;

import com.el.TestFactory;
import com.el.common.config.DataAuditConfig;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.data.jdbc.DataJdbcTest;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.Page;
import org.springframework.test.context.ActiveProfiles;

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

    @Test
    void testSave() {
        CourseEnrollment courseEnrollment = repository.save(TestFactory.createDefaultCourseEnrollment());
        assertNotNull(courseEnrollment.getId());

        Page<CourseEnrollment> allByStudent = repository.findAllByStudent(courseEnrollment.getStudent(), null);
        assertNotNull(allByStudent);
        long totalElements = allByStudent.getTotalElements();
        assertEquals(1, totalElements);

        Optional<CourseEnrollment> byIdAndStudent = repository.findByIdAndStudent(courseEnrollment.getId(), courseEnrollment.getStudent());
        assertNotNull(byIdAndStudent);
        assertEquals(courseEnrollment.getId(), byIdAndStudent.get().getId());
    }

    @Test
    void findAllCourseEnrollmentDTOsByStudent() {
        repository.findAllCourseEnrollmentDTOsByStudent("student", 0, 10);
    }


}
