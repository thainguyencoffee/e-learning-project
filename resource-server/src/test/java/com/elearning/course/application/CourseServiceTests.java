package com.elearning.course.application;

import com.elearning.course.application.dto.CourseDTO;
import com.elearning.course.application.impl.CourseServiceImpl;
import com.elearning.course.domain.Course;
import com.elearning.course.domain.CourseRepository;
import com.elearning.course.domain.Language;
import com.elearning.discount.application.DiscountService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class CourseServiceTests {

    @Mock
    private CourseRepository courseRepository;

    @Mock
    private DiscountService discountService;

    @InjectMocks
    private CourseServiceImpl courseService;

    private CourseDTO courseDTO;
    private Course course;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);

        // Giả lập CourseDTO với dữ liệu mẫu
        courseDTO = new CourseDTO(
                "Java Programming",
                "Learn Java from scratch",
                "http://example.com/image.jpg",
                Set.of("OOP", "Concurrency"),
                Language.ENGLISH,
                Set.of("Basic Programming Knowledge"),
                Set.of(Language.ENGLISH, Language.SPANISH)
        );

        // Giả lập Course sau khi được tạo từ CourseDTO
        course = new Course(
                courseDTO.title(),
                courseDTO.description(),
                courseDTO.thumbnailUrl(),
                courseDTO.benefits(),
                courseDTO.language(),
                courseDTO.prerequisites(),
                courseDTO.subtitles(),
                "teacher123"
        );
    }

    @Test
    public void testCreateCourse_ShouldCreateAndSaveCourse() {
        // Giả lập hành vi của repository
        when(courseRepository.save(any(Course.class))).thenReturn(course);

        // Thực thi use case
        Course createdCourse = courseService.createCourse("teacher123", courseDTO);

        // Xác minh rằng courseRepository.save đã được gọi
        verify(courseRepository, times(1)).save(any(Course.class));

        // Kiểm tra các giá trị trả về
        assertNotNull(createdCourse);
        assertEquals(courseDTO.title(), createdCourse.getTitle());
        assertEquals(courseDTO.description(), createdCourse.getDescription());
        assertEquals(courseDTO.thumbnailUrl(), createdCourse.getThumbnailUrl());
        assertEquals(courseDTO.language(), createdCourse.getLanguage());
        assertEquals(courseDTO.benefits(), createdCourse.getBenefits());
        assertEquals(courseDTO.prerequisites(), createdCourse.getPrerequisites());
        assertEquals(courseDTO.subtitles(), createdCourse.getSubtitles());
        assertEquals("teacher123", createdCourse.getTeacher());
    }

    @Test
    public void testCreateCourse_ShouldThrowException_WhenTitleIsBlank() {
        // Giả lập CourseDTO với tiêu đề trống
        CourseDTO invalidCourseDTO = new CourseDTO(
                "",
                "Learn Java from scratch",
                "http://example.com/image.jpg",
                Set.of("OOP", "Concurrency"),
                Language.ENGLISH,
                Set.of("Basic Programming Knowledge"),
                Set.of(Language.ENGLISH, Language.SPANISH)
        );

        // Kiểm tra xem ngoại lệ có được ném ra khi tiêu đề trống không
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            courseService.createCourse("teacher123", invalidCourseDTO);
        });

        assertEquals("Title must not be empty.", exception.getMessage());

        // Đảm bảo không có gì được lưu vào repository
        verify(courseRepository, never()).save(any(Course.class));
    }
}
