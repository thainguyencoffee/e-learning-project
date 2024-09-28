package com.elearning.course.web;

import com.elearning.common.config.SecurityConfig;
import com.elearning.course.application.dto.CourseDTO;
import com.elearning.course.application.impl.CourseServiceImpl;
import com.elearning.course.domain.Course;
import com.elearning.course.domain.Language;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Set;

import static org.mockito.ArgumentMatchers.any;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(CourseController.class)
@Import(SecurityConfig.class)
class CourseControllerTests {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private CourseServiceImpl courseService;

    @Autowired
    private ObjectMapper objectMapper;

    private CourseDTO courseDTO;

    @BeforeEach
    public void setUp() {
        // Setup CourseDTO với dữ liệu mẫu
        courseDTO = new CourseDTO(
                "Java Programming",
                "Learn Java from scratch",
                "http://example.com/image.jpg",
                Set.of("OOP", "Concurrency"),
                Language.ENGLISH,
                Set.of("Basic Programming Knowledge"),
                Set.of(Language.ENGLISH, Language.SPANISH)
        );
    }

    @Test
    public void testCreateCourse_ShouldReturnCreatedStatus() throws Exception {
        // Giả lập khóa học được tạo với ID
        Course createdCourse = Mockito.mock(Course.class);
        Mockito.when(createdCourse.getId()).thenReturn(1L);  // Giả lập ID được gán sau khi lưu

        // Giả lập các thuộc tính khác của khóa học
        Mockito.when(createdCourse.getTitle()).thenReturn(courseDTO.title());
        Mockito.when(createdCourse.getDescription()).thenReturn(courseDTO.description());
        Mockito.when(createdCourse.getTeacher()).thenReturn("teacher123");

        // Giả lập hành vi của CreateCourseUseCase trả về đối tượng khóa học với ID
        Mockito.when(courseService.createCourse(any(String.class), any(CourseDTO.class)))
                .thenReturn(createdCourse);

        // Thực thi HTTP POST request với JWT
        mockMvc.perform(post("/courses")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(courseDTO))
                        .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_teacher"))))
                .andExpect(status().isCreated())  // Kiểm tra phản hồi 201 Created
                .andExpect(header().string("Location", "/courses/1"))  // Kiểm tra header Location với URI chính xác
                .andExpect(jsonPath("$.title").value(courseDTO.title()))  // Kiểm tra giá trị trả về
                .andExpect(jsonPath("$.teacher").value("teacher123"));
    }

    @Test
    public void testCreateCourse_ShouldReturnForbidden_WhenUserIsNotTeacher() throws Exception {
        // Tạo một JWT với role khác không phải "teacher" hoặc "admin"
        Jwt jwt = Jwt.withTokenValue("token")
                .claim("sub", "student123") // Giả lập một người dùng không phải "teacher"
                .claim("roles", Set.of("student")) // Giả lập roles không có quyền "teacher"
                .header("alg", "HS256")
                .build();

        // Thực thi HTTP POST request với JWT không có quyền "teacher"
        mockMvc.perform(post("/courses")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(courseDTO))
                        .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_user"))))
                .andExpect(status().isForbidden());  // Kiểm tra phản hồi 403 Forbidden
    }

    @Test
    public void testCreateCourse_ShouldReturnBadRequest_WhenTitleIsBlank() throws Exception {
        // CourseDTO với tiêu đề trống
        CourseDTO invalidCourseDTO = new CourseDTO(
                "",
                "Learn Java from scratch",
                "http://example.com/image.jpg",
                Set.of("OOP", "Concurrency"),
                Language.ENGLISH,
                Set.of("Basic Programming Knowledge"),
                Set.of(Language.ENGLISH, Language.SPANISH)
        );

        // Thực thi HTTP POST request với dữ liệu không hợp lệ
        mockMvc.perform(post("/courses")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidCourseDTO))
                        .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_teacher"))))
                .andExpect(status().isBadRequest());
    }

}
