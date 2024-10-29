package com.el.enrollment.web;

import com.el.TestFactory;
import com.el.common.config.SecurityConfig;
import com.el.common.config.jackson.JacksonCustomizations;
import com.el.common.exception.ResourceNotFoundException;
import com.el.enrollment.application.CourseEnrollmentDTO;
import com.el.enrollment.application.impl.CourseEnrollmentServiceImpl;
import com.el.enrollment.domain.CourseEnrollment;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Instant;
import java.util.List;

import static org.hamcrest.Matchers.hasSize;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;


@WebMvcTest(CourseEnrollmentController.class)
@Import({SecurityConfig.class, JacksonCustomizations.class})
class CourseEnrollmentControllerTests {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private CourseEnrollmentServiceImpl courseEnrollmentService;

    @Test
    void getAllEnrollments_ValidRequest_ReturnsAllEnrollments() throws Exception {
        Pageable pageable = PageRequest.of(0, 10);

        CourseEnrollment enrollment = TestFactory.createDefaultCourseEnrollment();
        CourseEnrollmentDTO enrollmentDTO = new CourseEnrollmentDTO(
                enrollment.getId(),
                enrollment.getStudent(),
                enrollment.getCourseId(),
                "demo title",
                "https://example.com",
                TestFactory.teacher,
                Instant.now(),
                false
        );
        when(courseEnrollmentService.findAllCourseEnrollments(pageable)).thenReturn(List.of(enrollmentDTO));

        mockMvc.perform(get("/enrollments")
                        .param("page", "0")
                        .param("size", "10")
                .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_user"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(1)));
    }

    @Test
    void getEnrollmentById_ValidRequest_ReturnsEnrollment() throws Exception {
        CourseEnrollment enrollment = TestFactory.createDefaultCourseEnrollment();
        when(courseEnrollmentService.findCourseEnrollmentById(any())).thenReturn(enrollment);

        mockMvc.perform(get("/enrollments/{enrollmentId}", 1L)
                .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_user"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.courseId").value(enrollment.getCourseId().toString()));
    }

    @Test
    void markLessonAsCompleted_ValidRequest_MarksLessonAsCompleted() throws Exception {
        doNothing().when(courseEnrollmentService).markLessonAsCompleted(any(), any());

        mockMvc.perform(put("/enrollments/{enrollmentId}/lessons/{lessonId}", 1L, 1L)
                .param("mark", "completed")
                .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_user")))
        ).andExpect(status().isOk());
    }

    @Test
    void markLessonAsCompleted_InvalidRequest_ReturnsBadRequest() throws Exception {
        mockMvc.perform(put("/enrollments/{enrollmentId}/lessons/{lessonId}", 1L, 1L)
                .param("mark", "invalid")
                .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_user")))
        ).andExpect(status().isBadRequest());
    }

    @Test
    void markLessonAsCompleted_NotAuthorized_ReturnsForbidden() throws Exception {
        mockMvc.perform(put("/enrollments/{enrollmentId}/lessons/{lessonId}", 1L, 1L)
                .param("mark", "completed")
        ).andExpect(status().isUnauthorized());
    }

    @Test
    void markLessonAsCompleted_NotFound_ReturnsNotFound() throws Exception {
        doThrow(ResourceNotFoundException.class).when(courseEnrollmentService).markLessonAsCompleted(any(), any());

        mockMvc.perform(put("/enrollments/{enrollmentId}/lessons/{lessonId}", 1L, 1L)
                .param("mark", "completed")
                .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_user")))
        ).andExpect(status().isNotFound());
    }

    @Test
    void markLessonAsIncomplete_ValidRequest_MarksLessonAsIncomplete() throws Exception {
        doNothing().when(courseEnrollmentService).markLessonAsIncomplete(any(), any());

        mockMvc.perform(put("/enrollments/{enrollmentId}/lessons/{lessonId}", 1L, 1L)
                .param("mark", "incomplete")
                .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_user")))
        ).andExpect(status().isOk());
    }

    @Test
    void markLessonAsIncomplete_NotAuthorized_ReturnsForbidden() throws Exception {
        mockMvc.perform(put("/enrollments/{enrollmentId}/lessons/{lessonId}", 1L, 1L)
                .param("mark", "incomplete")
        ).andExpect(status().isUnauthorized());
    }

    @Test
    void markLessonAsIncomplete_NotFound_ReturnsNotFound() throws Exception {
        doThrow(ResourceNotFoundException.class).when(courseEnrollmentService).markLessonAsIncomplete(any(), any());

        mockMvc.perform(put("/enrollments/{enrollmentId}/lessons/{lessonId}", 1L, 1L)
                .param("mark", "incomplete")
                .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_user")))
        ).andExpect(status().isNotFound());
    }


}
