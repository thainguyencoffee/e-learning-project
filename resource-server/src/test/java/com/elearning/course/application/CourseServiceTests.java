package com.elearning.course.application;

import com.elearning.common.exception.InputInvalidException;
import com.elearning.common.exception.ResourceNotFoundException;
import com.elearning.course.application.dto.CourseDTO;
import com.elearning.course.application.dto.CourseUpdateDTO;
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
    private CourseUpdateDTO courseUpdateDTO;
    private Course course;

    @BeforeEach
    void setUp() {
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

        courseUpdateDTO = new CourseUpdateDTO(
                "Java Programming",
                "Learn Java from scratch",
                "http://example.com/image.jpg",
                Set.of("OOP", "Concurrency"),
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
    void testCreateCourse_ShouldCreateAndSaveCourse() {
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
    void testUpdateInfoCourse_ShouldUpdateCourseInfo() {
        // Giả lập hành vi của repository
        when(courseRepository.findByIdAndDeleted(1L, false)).thenReturn(java.util.Optional.of(course));
        when(courseRepository.save(any(Course.class))).thenReturn(course);

        // Thực thi use case
        Course updatedCourse = courseService.updateCourse(1L, courseUpdateDTO);

        // Xác minh rằng courseRepository.findByIdAndDeleted và courseRepository.save đã được gọi
        verify(courseRepository, times(1)).findByIdAndDeleted(1L, false);
        verify(courseRepository, times(1)).save(any(Course.class));

        // Kiểm tra các giá trị trả về
        assertNotNull(updatedCourse);
        assertEquals(courseUpdateDTO.title(), updatedCourse.getTitle());
        assertEquals(courseUpdateDTO.description(), updatedCourse.getDescription());
        assertEquals(courseUpdateDTO.thumbnailUrl(), updatedCourse.getThumbnailUrl());
        assertEquals(courseUpdateDTO.benefits(), updatedCourse.getBenefits());
        assertEquals(courseUpdateDTO.prerequisites(), updatedCourse.getPrerequisites());
        assertEquals(courseUpdateDTO.subtitles(), updatedCourse.getSubtitles());
        assertEquals("teacher123", updatedCourse.getTeacher());
    }

    @Test
    void testUpdateInfoCourse_ShouldThrowException_WhenCourseNotFound() {
        // Giả lập hành vi của repository
        when(courseRepository.findByIdAndDeleted(1L, false)).thenReturn(java.util.Optional.empty());

        // Kiểm tra xem ngoại lệ có được ném ra khi không tìm thấy khóa học
        assertThrows(ResourceNotFoundException.class, () -> {
            courseService.updateCourse(1L, courseUpdateDTO);
        });

        // Đảm bảo không có gì được lưu vào repository
        verify(courseRepository, never()).save(any(Course.class));
    }

    @Test
    void deleteCourse_ValidCourseId_DeletesCourse() {
        when(courseRepository.findByIdAndDeleted(1L, false)).thenReturn(java.util.Optional.of(course));
        courseService.deleteCourse(1L);
        verify(courseRepository, times(1)).save(course);
        assertTrue(course.isDeleted());
    }

    @Test
    void deleteCourse_CourseNotFound_ThrowsException() {
        when(courseRepository.findByIdAndDeleted(1L, false)).thenReturn(java.util.Optional.empty());
        assertThrows(ResourceNotFoundException.class, () -> courseService.deleteCourse(1L));
        verify(courseRepository, never()).save(any(Course.class));
    }

    @Test
    void deleteCourse_AlreadyDeletedCourse_ThrowsException() {
        course.delete();
        when(courseRepository.findByIdAndDeleted(1L, false)).thenReturn(java.util.Optional.of(course));
        assertThrows(InputInvalidException.class, () -> courseService.deleteCourse(1L));
        verify(courseRepository, never()).save(any(Course.class));
    }

}
