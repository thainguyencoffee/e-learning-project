package com.elearning.course.domain;

import com.elearning.common.Currencies;
import com.elearning.common.config.DataAuditConfig;
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
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

@Import({DataAuditConfig.class})
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@ActiveProfiles("integration")
@DataJdbcTest
public class CourseJdbcTests {

    @Autowired
    private CourseRepository courseRepository;

    private Course course;

    @BeforeEach
    void setUp() {
        course = new Course(
                "Java Programming",
                "Learn Java from scratch",
                "http://example.com/thumbnail.jpg",
                Set.of("OOP", "Concurrency"),
                Language.ENGLISH,
                Set.of("Basic Programming Knowledge"),
                Set.of(Language.ENGLISH, Language.SPANISH),
                "teacher123"
        );
        course.changePrice(Money.of(100, Currencies.VND));
    }

    @Test
    public void testSaveCourse() {
        // Lưu một course vào repository
        Course savedCourse = courseRepository.save(course);

        // Kiểm tra xem course có được lưu không
        assertNotNull(savedCourse.getId());
        assertEquals("Java Programming", savedCourse.getTitle());
        assertEquals("teacher123", savedCourse.getTeacher());
    }

    @Test
    void testSaveCourseWithSections() {
        // Tạo một course với các sections
        CourseSection section1 = new CourseSection("Introduction");
        section1.addLesson(new Lesson("What is Java?", Lesson.Type.TEXT, "https://example.com/lesson1", null));
        section1.addLesson(new Lesson("What is Java 2?", Lesson.Type.QUIZ, null, 1L));
        course.addSection(section1);

        // Lưu course vào database
        Course savedCourse = courseRepository.save(course);

        // Kiểm tra xem course và các sections có được lưu không
        assertNotNull(savedCourse.getId());
        assertEquals(1, savedCourse.getSections().size());
        assertEquals(2, savedCourse.getSections().iterator().next().getLessons().size());
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
    public void testUpdateCourse() {
        // Lưu course vào database trước
        Course savedCourse = courseRepository.save(course);

        // Cập nhật thông tin của course
        savedCourse.updateInfo(
                "Advanced Java Programming",
                "Master Java concepts",
                "http://example.com/new-thumbnail.jpg",
                Set.of("OOP", "Concurrency", "JVM internals"),
                Set.of("Basic Programming Knowledge"),
                Set.of(Language.ENGLISH)
        );

        // Lưu lại cập nhật
        courseRepository.save(savedCourse);

        // Tìm lại course và kiểm tra
        Optional<Course> updatedCourse = courseRepository.findById(savedCourse.getId());
        assertTrue(updatedCourse.isPresent());
        assertEquals("Advanced Java Programming", updatedCourse.get().getTitle());
        assertEquals("Master Java concepts", updatedCourse.get().getDescription());
    }

    @Test
    public void testFindAllCourses() {
        // Tạo và lưu nhiều course vào database
        courseRepository.save(course);

        Course anotherCourse = new Course(
                "Spring Boot",
                "Learn Spring Boot",
                "http://example.com/spring-thumbnail.jpg",
                Set.of("Spring Framework", "Dependency Injection"),
                Language.ENGLISH,
                Set.of("Basic Java Knowledge"),
                Set.of(Language.ENGLISH),
                "teacher456"
        );
        courseRepository.save(anotherCourse);

        // Phân trang và tìm tất cả course
        Page<Course> coursesPage = courseRepository.findAll(PageRequest.of(0, 10));

        // Kiểm tra số lượng và nội dung các course
        assertEquals(2, coursesPage.getTotalElements());
        assertEquals("Java Programming", coursesPage.getContent().get(0).getTitle());
        assertEquals("Spring Boot", coursesPage.getContent().get(1).getTitle());
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
