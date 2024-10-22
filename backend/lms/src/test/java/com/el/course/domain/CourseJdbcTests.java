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

    private Course course;

    @BeforeEach
    void setUp() {
        course = TestFactory.createDefaultCourse();
        course.changePrice(Money.of(100, Currencies.VND));
    }

    @Test
    public void testSaveCourse() {
        // Lưu một course vào repository
        Course savedCourse = courseRepository.save(course);

        // Kiểm tra xem course có được lưu không
        assertNotNull(savedCourse.getId());
        assertEquals(course.getTitle(), savedCourse.getTitle());
        assertEquals(course.getTeacher(), savedCourse.getTeacher());
    }

    @Test
    public void testFindById() {
        // Lưu course vào database trước
        Course savedCourse = courseRepository.save(course);

        // Tìm course theo ID
        Optional<Course> retrievedCourse = courseRepository.findById(savedCourse.getId());

        // Kiểm tra kết quả
        assertTrue(retrievedCourse.isPresent());
        assertEquals(savedCourse.getTitle(), retrievedCourse.get().getTitle());
    }

    @Test
    public void testFindAllCourses() {
        // Tạo và lưu nhiều course vào database
        courseRepository.save(course);

        // Phân trang và tìm tất cả course
        Page<Course> coursesPage = courseRepository.findAll(PageRequest.of(0, 10));

        // Kiểm tra số lượng và nội dung các course
        assertEquals(1, coursesPage.getTotalElements());
        assertEquals(course.getTitle(), coursesPage.getContent().get(0).getTitle());
    }

    @Test
    public void testDeleteCourse() {
        // Lưu course vào database trước
        Course savedCourse = courseRepository.save(course);

        // Xóa course
        savedCourse.delete();
        courseRepository.save(savedCourse);

        // Tìm course theo ID đã xóa
        Optional<Course> deletedCourse = courseRepository.findByIdAndDeleted(savedCourse.getId(), true);
        assertTrue(deletedCourse.isPresent());
        assertTrue(deletedCourse.get().isDeleted());
    }

}
