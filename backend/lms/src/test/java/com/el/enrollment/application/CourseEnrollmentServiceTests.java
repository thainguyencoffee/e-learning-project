package com.el.enrollment.application;

import com.el.common.RolesBaseUtil;
import com.el.common.exception.AccessDeniedException;
import com.el.common.exception.InputInvalidException;
import com.el.common.exception.ResourceNotFoundException;
import com.el.course.application.CourseQueryService;
import com.el.course.domain.Course;
import com.el.enrollment.application.impl.CourseEnrollmentServiceImpl;
import com.el.enrollment.domain.CourseEnrollment;
import com.el.enrollment.domain.CourseEnrollmentRepository;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CourseEnrollmentServiceTests {

    @Mock
    private CourseEnrollmentRepository courseEnrollmentRepository;

    @Mock
    private CourseQueryService courseQueryService;

    @Mock
    private RolesBaseUtil rolesBaseUtil;

    @InjectMocks
    private CourseEnrollmentServiceImpl courseEnrollmentService;

    @Test
    void enrollCourse_ValidCourseIdAndStudent_EnrollsCourseSuccessfully() {
        // Arrange
        String student = "student";
        Long courseId = 1L;

        // Tạo một Course giả lập với lessonIds
        Course mockCourse = Mockito.mock(Course.class);
        Set<Long> lessonIds = new HashSet<>(Arrays.asList(1L, 2L, 3L));
        when(mockCourse.getLessonIds()).thenReturn(lessonIds);
        when(courseQueryService.findPublishedCourseById(courseId)).thenReturn(mockCourse);

        // Act
        courseEnrollmentService.enrollment(student, courseId);

        // Assert
        verify(courseEnrollmentRepository, times(1)).save(any(CourseEnrollment.class));
    }

    @Test
    void enrollCourse_InvalidCourseId_ThrowsResourceNotFoundException() {
        // Arrange
        String student = "student";
        Long courseId = 1L;

        when(courseQueryService.findPublishedCourseById(courseId)).thenThrow(ResourceNotFoundException.class);

        // Act and Assert
        assertThrows(ResourceNotFoundException.class, () -> courseEnrollmentService.enrollment(student, courseId));
        verify(courseEnrollmentRepository, never()).save(any(CourseEnrollment.class));

    }

    @Test
    void enrollCourse_LessonProgressesEmpty_ThrowException() {
        // Arrange
        String student = "student";
        Long courseId = 1L;

        Course mockCourse = Mockito.mock(Course.class);
        when(courseQueryService.findPublishedCourseById(courseId)).thenReturn(mockCourse);
        when(mockCourse.getLessonIds()).thenReturn(new HashSet<>());

        // Act and Assert
        Assertions.assertThrows(InputInvalidException.class, () ->
                courseEnrollmentService.enrollment(student, courseId));

        // Verify
        verify(courseEnrollmentRepository, never()).save(any(CourseEnrollment.class));
    }

    @Test
    void markLessonAsCompleted_Admin_MarksLessonAsCompleted() {
        // Arrange
        Long enrollmentId = 1L;
        Long lessonId = 1L;

        CourseEnrollment mockCourseEnrollment = Mockito.mock(CourseEnrollment.class);
        when(rolesBaseUtil.isAdmin()).thenReturn(true);
        when(courseEnrollmentRepository.findById(enrollmentId)).thenReturn(Optional.of(mockCourseEnrollment));

        // Act
        courseEnrollmentService.markLessonAsCompleted(enrollmentId, lessonId);

        // Assert
        verify(mockCourseEnrollment, times(1)).markLessonAsCompleted(lessonId);
        verify(courseEnrollmentRepository, times(1)).save(mockCourseEnrollment);
    }

    @Test
    void markLessonAsCompleted_RoleUser_MarksLessonAsCompleted() {
        // Arrange
        Long enrollmentId = 1L;
        Long lessonId = 1L;

        CourseEnrollment mockCourseEnrollment = Mockito.mock(CourseEnrollment.class);
        when(rolesBaseUtil.isUser()).thenReturn(true);
        when(rolesBaseUtil.getCurrentPreferredUsernameFromJwt()).thenReturn("student");

        when(courseEnrollmentRepository.findByIdAndStudent(enrollmentId, "student"))
                .thenReturn(Optional.of(mockCourseEnrollment));

        // Act
        courseEnrollmentService.markLessonAsCompleted(enrollmentId, lessonId);

        // Assert
        verify(mockCourseEnrollment, times(1)).markLessonAsCompleted(lessonId);
        verify(courseEnrollmentRepository, times(1)).save(mockCourseEnrollment);
    }

    @Test
    void markLessonAsCompleted_AccessDenied_ThrowsException() {
        // Arrange
        Long enrollmentId = 1L;
        Long lessonId = 1L;

        CourseEnrollment mockCourseEnrollment = Mockito.mock(CourseEnrollment.class);
        when(rolesBaseUtil.isUser()).thenReturn(true);
        when(rolesBaseUtil.getCurrentPreferredUsernameFromJwt()).thenReturn("student");

        when(courseEnrollmentRepository.findByIdAndStudent(enrollmentId, "student"))
                .thenThrow(ResourceNotFoundException.class);

        // Act and Assert
        assertThrows(ResourceNotFoundException.class, () ->
                courseEnrollmentService.markLessonAsCompleted(enrollmentId, lessonId));

        // Verify
        verify(courseEnrollmentRepository, never()).save(any(CourseEnrollment.class));
    }

    @Test
    void markLessonAsIncomplete_Admin_MarksLessonAsIncomplete() {
        // Arrange
        Long enrollmentId = 1L;
        Long lessonId = 1L;

        CourseEnrollment mockCourseEnrollment = Mockito.mock(CourseEnrollment.class);
        when(rolesBaseUtil.isAdmin()).thenReturn(true);
        when(courseEnrollmentRepository.findById(enrollmentId)).thenReturn(Optional.of(mockCourseEnrollment));

        // Act
        courseEnrollmentService.markLessonAsIncomplete(enrollmentId, lessonId);

        // Assert
        verify(mockCourseEnrollment, times(1)).markLessonAsIncomplete(lessonId);
        verify(courseEnrollmentRepository, times(1)).save(mockCourseEnrollment);
    }

    @Test
    void markLessonAsIncomplete_RoleUser_MarksLessonAsIncomplete() {
        // Arrange
        Long enrollmentId = 1L;
        Long lessonId = 1L;

        CourseEnrollment mockCourseEnrollment = Mockito.mock(CourseEnrollment.class);
        when(rolesBaseUtil.isUser()).thenReturn(true);
        when(rolesBaseUtil.getCurrentPreferredUsernameFromJwt()).thenReturn("student");

        when(courseEnrollmentRepository.findByIdAndStudent(enrollmentId, "student"))
                .thenReturn(Optional.of(mockCourseEnrollment));

        // Act
        courseEnrollmentService.markLessonAsIncomplete(enrollmentId, lessonId);

        // Assert
        verify(mockCourseEnrollment, times(1)).markLessonAsIncomplete(lessonId);
        verify(courseEnrollmentRepository, times(1)).save(mockCourseEnrollment);
    }

    @Test
    void markLessonAsIncomplete_AccessDenied_ThrowsException() {
        // Arrange
        Long enrollmentId = 1L;
        Long lessonId = 1L;

        CourseEnrollment mockCourseEnrollment = Mockito.mock(CourseEnrollment.class);
        when(rolesBaseUtil.isUser()).thenReturn(true);
        when(rolesBaseUtil.getCurrentPreferredUsernameFromJwt()).thenReturn("student");

        when(courseEnrollmentRepository.findByIdAndStudent(enrollmentId, "student"))
                .thenThrow(ResourceNotFoundException.class);

        // Act and Assert
        assertThrows(ResourceNotFoundException.class, () ->
                courseEnrollmentService.markLessonAsIncomplete(enrollmentId, lessonId));

        // Verify
        verify(courseEnrollmentRepository, never()).save(any(CourseEnrollment.class));
    }


}
