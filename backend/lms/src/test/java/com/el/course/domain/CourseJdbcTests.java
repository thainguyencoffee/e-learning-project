package com.el.course.domain;

import com.el.TestFactory;
import com.el.common.Currencies;
import com.el.common.config.DataAuditConfig;
import org.javamoney.moneta.Money;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.data.jdbc.DataJdbcTest;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.context.ActiveProfiles;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@Import({DataAuditConfig.class})
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@ActiveProfiles("integration")
@DataJdbcTest
class CourseJdbcTests {

    @Autowired
    private CourseRepository courseRepository;

    private Course courseWithSections;

    @BeforeEach
    void setUp() {
        courseWithSections = TestFactory.createCourseWithSections();
        courseWithSections.changePrice(Money.of(100, Currencies.VND));
    }

    @Test
    public void testSaveCourse() {
        Course savedCourse = courseRepository.save(courseWithSections);

        assertNotNull(savedCourse.getId());
        assertEquals(courseWithSections.getTitle(), savedCourse.getTitle());
        assertEquals(courseWithSections.getTeacher(), savedCourse.getTeacher());
    }

    @Test
    public void testFindById() {
        Course savedCourse = courseRepository.save(courseWithSections);

        Optional<Course> retrievedCourse = courseRepository.findById(savedCourse.getId());

        assertTrue(retrievedCourse.isPresent());
        assertEquals(savedCourse.getTitle(), retrievedCourse.get().getTitle());
    }

    @Test
    public void testFindAllCourses() {
        courseRepository.save(courseWithSections);

        Page<Course> coursesPage = courseRepository.findAll(PageRequest.of(0, 10));

        assertEquals(1, coursesPage.getTotalElements());
        assertEquals(courseWithSections.getTitle(), coursesPage.getContent().get(0).getTitle());
    }

    @Test
    public void testDeleteCourse() {
        Course savedCourse = courseRepository.save(courseWithSections);

        // Xóa course
        savedCourse.delete();
        courseRepository.save(savedCourse);

        // Tìm course theo ID đã xóa
        Optional<Course> deletedCourse = courseRepository.findByIdAndDeleted(savedCourse.getId(), true);
        assertTrue(deletedCourse.isPresent());
        assertTrue(deletedCourse.get().isDeleted());
    }

}
