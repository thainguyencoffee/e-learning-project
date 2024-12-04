package com.el.enrollment.application;

import com.el.TestFactory;
import com.el.common.RolesBaseUtil;
import com.el.common.exception.AccessDeniedException;
import com.el.common.exception.InputInvalidException;
import com.el.common.exception.ResourceNotFoundException;
import com.el.course.application.CourseQueryService;
import com.el.course.domain.Course;
import com.el.course.domain.Lesson;
import com.el.enrollment.application.dto.ChangeCourseResponse;
import com.el.enrollment.application.impl.CourseEnrollmentServiceImpl;
import com.el.enrollment.domain.AdditionalPaymentRequiredException;
import com.el.enrollment.domain.Enrollment;
import com.el.enrollment.domain.EnrollmentRepository;
import org.javamoney.moneta.Money;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.*;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class EnrollmentServiceTests {

    @Mock
    private EnrollmentRepository enrollmentRepository;

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

        // mock lessons
        Lesson lesson1 = spy(new Lesson("Lesson 1", Lesson.Type.VIDEO, "https://www.youtube.com/watch?v=123"));
        when(lesson1.getId()).thenReturn(1L);
        when(lesson1.getOrderIndex()).thenReturn(1);
        Lesson lesson2 = spy(new Lesson("Lesson 2", Lesson.Type.VIDEO, "https://www.youtube.com/watch?v=456"));
        when(lesson2.getId()).thenReturn(2L);
        when(lesson2.getOrderIndex()).thenReturn(2);
        Stream<Lesson> lessonStream = Stream.of(lesson1, lesson2);

        when(mockCourse.getLessons()).thenReturn(lessonStream);
        when(mockCourse.getQuizIds()).thenReturn(Set.of(1L, 2L));
        when(mockCourse.getTeacher()).thenReturn(TestFactory.teacher);
        when(courseQueryService.findPublishedCourseById(courseId)).thenReturn(mockCourse);

        // Act
        courseEnrollmentService.enrollment(student, courseId);

        // Assert
        verify(enrollmentRepository, times(1)).save(any(Enrollment.class));
    }

    @Test
    void enrollCourse_InvalidCourseId_ThrowsResourceNotFoundException() {
        // Arrange
        String student = "student";
        Long courseId = 1L;

        when(courseQueryService.findPublishedCourseById(courseId)).thenThrow(ResourceNotFoundException.class);

        // Act and Assert
        assertThrows(ResourceNotFoundException.class, () -> courseEnrollmentService.enrollment(student, courseId));
        verify(enrollmentRepository, never()).save(any(Enrollment.class));

    }

    @Test
    void enrollCourse_LessonProgressesEmpty_ThrowException() {
        // Arrange
        String student = "student";
        Long courseId = 1L;

        Course mockCourse = Mockito.mock(Course.class);
        when(courseQueryService.findPublishedCourseById(courseId)).thenReturn(mockCourse);

        // Act and Assert
        Assertions.assertThrows(InputInvalidException.class, () ->
                courseEnrollmentService.enrollment(student, courseId));

        // Verify
        verify(enrollmentRepository, never()).save(any(Enrollment.class));
    }

    @Test
    void markLessonAsCompleted_Admin_Throws() {
        // Arrange
        Long enrollmentId = 1L;
        Long courseId = 1L;
        Long lessonId = 1L;

        Enrollment mockEnrollment = Mockito.mock(Enrollment.class);
        when(rolesBaseUtil.isAdmin()).thenReturn(true);

        // Act
        assertThrows(AccessDeniedException.class, () ->
                courseEnrollmentService.markLessonAsCompleted(enrollmentId, courseId, lessonId));

        // Assert
        verify(mockEnrollment, never()).markLessonAsCompleted(lessonId, "Lesson Title", 1);
        verify(enrollmentRepository, never()).save(mockEnrollment);
    }

    @Test
    void markLessonAsCompleted_RoleUser_MarksLessonAsCompleted() {
        // Arrange
        Long enrollmentId = 1L;
        Long courseId = 1L;
        Long lessonId = 1L;

        Enrollment mockEnrollment = Mockito.mock(Enrollment.class);
        when(rolesBaseUtil.isUser()).thenReturn(true);
        when(rolesBaseUtil.getCurrentPreferredUsernameFromJwt()).thenReturn("student");

        when(enrollmentRepository.findByIdAndStudent(enrollmentId, "student"))
                .thenReturn(Optional.of(mockEnrollment));

        // Update
        Lesson lesson = spy(new Lesson("Lesson Title", Lesson.Type.VIDEO, "https://www.youtube.com/watch?v=123"));
        when(lesson.getTitle()).thenReturn("Lesson Title");
        when(lesson.getId()).thenReturn(lessonId);
        when(courseQueryService.findLessonByCourseIdAndLessonId(courseId, lessonId)).thenReturn(lesson);

        // Act
        courseEnrollmentService.markLessonAsCompleted(enrollmentId, courseId, lessonId);

        // Assert
        verify(mockEnrollment, times(1)).markLessonAsCompleted(anyLong(), anyString(), any());
        verify(enrollmentRepository, times(1)).save(mockEnrollment);
    }

    @Test
    void markLessonAsCompleted_AccessDenied_ThrowsException() {
        // Arrange
        Long enrollmentId = 1L;
        Long courseId = 1L;
        Long lessonId = 1L;

        Enrollment mockEnrollment = Mockito.mock(Enrollment.class);
        when(rolesBaseUtil.isUser()).thenReturn(true);
        when(rolesBaseUtil.getCurrentPreferredUsernameFromJwt()).thenReturn("student");

        when(enrollmentRepository.findByIdAndStudent(enrollmentId, "student"))
                .thenThrow(ResourceNotFoundException.class);

        // Act and Assert
        assertThrows(ResourceNotFoundException.class, () ->
                courseEnrollmentService.markLessonAsCompleted(enrollmentId, courseId, lessonId));

        // Verify
        verify(enrollmentRepository, never()).save(any(Enrollment.class));
    }

    @Test
    void markLessonAsIncomplete_Teacher_Throws() {
        // Arrange
        Long enrollmentId = 1L;
        Long courseId = 1L;
        Long lessonId = 1L;

        Enrollment mockEnrollment = Mockito.mock(Enrollment.class);
        when(rolesBaseUtil.isTeacher()).thenReturn(true);

        // Act
        assertThrows(AccessDeniedException.class, () ->
                courseEnrollmentService.markLessonAsIncomplete(enrollmentId, courseId, lessonId));

        // Assert
        verify(mockEnrollment, never()).markLessonAsIncomplete(lessonId);
        verify(enrollmentRepository, never()).save(mockEnrollment);
    }

    @Test
    void markLessonAsIncomplete_RoleUser_MarksLessonAsIncomplete() {
        // Arrange
        Long enrollmentId = 1L;
        Long courseId = 1L;
        Long lessonId = 1L;

        Enrollment mockEnrollment = Mockito.mock(Enrollment.class);
        when(rolesBaseUtil.isUser()).thenReturn(true);
        when(rolesBaseUtil.getCurrentPreferredUsernameFromJwt()).thenReturn("student");

        when(enrollmentRepository.findByIdAndStudent(enrollmentId, "student"))
                .thenReturn(Optional.of(mockEnrollment));

        // Act
        courseEnrollmentService.markLessonAsIncomplete(enrollmentId, courseId, lessonId);

        // Assert
        verify(mockEnrollment, times(1)).markLessonAsIncomplete(lessonId);
        verify(enrollmentRepository, times(1)).save(mockEnrollment);
    }

    @Test
    void markLessonAsIncomplete_AccessDenied_ThrowsException() {
        // Arrange
        Long enrollmentId = 1L;
        Long courseId = 1L;
        Long lessonId = 1L;

        Enrollment mockEnrollment = Mockito.mock(Enrollment.class);
        when(rolesBaseUtil.isUser()).thenReturn(true);
        when(rolesBaseUtil.getCurrentPreferredUsernameFromJwt()).thenReturn("student");

        when(enrollmentRepository.findByIdAndStudent(enrollmentId, "student"))
                .thenThrow(ResourceNotFoundException.class);

        // Act and Assert
        assertThrows(ResourceNotFoundException.class, () ->
                courseEnrollmentService.markLessonAsIncomplete(enrollmentId, courseId, lessonId));

        // Verify
        verify(enrollmentRepository, never()).save(any(Enrollment.class));
    }

    @Test
    void changeCourse_ValidRequest_ChangesCourse() throws AdditionalPaymentRequiredException {
        Enrollment enrollment = mock(Enrollment.class);
        Course oldCourse = mock(Course.class);
        Course newCourse = mock(Course.class);

        // mock for enrollmentService.findCourseEnrollmentById
        when(rolesBaseUtil.getCurrentPreferredUsernameFromJwt()).thenReturn("student");
        when(rolesBaseUtil.isUser()).thenReturn(true);
        when(enrollmentRepository.findByIdAndStudent(anyLong(), anyString())).thenReturn(Optional.of(enrollment));

        when(courseQueryService.findPublishedCourseById(anyLong())).thenReturn(oldCourse).thenReturn(newCourse);

        when(oldCourse.getPrice()).thenReturn(Money.of(100, "USD"));
        when(newCourse.getPrice()).thenReturn(Money.of(80, "USD"));
        when(newCourse.getTeacher()).thenReturn("newTeacher");
        when(newCourse.getQuizIds()).thenReturn(Set.of(3L, 4L));

        ChangeCourseResponse response = courseEnrollmentService.changeCourse(1L, 2L);

        verify(enrollment).requestChangeCourse(eq(2L), any(), any(), eq("newTeacher"), anySet(), anySet());
        verify(enrollmentRepository, times(1)).save(enrollment);
        assertEquals(ChangeCourseResponse.basicChange(), response);
    }

}
