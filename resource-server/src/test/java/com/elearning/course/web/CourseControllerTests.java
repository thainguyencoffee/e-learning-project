package com.elearning.course.web;

import com.elearning.common.config.JacksonCustomizations;
import com.elearning.common.config.SecurityConfig;
import com.elearning.common.exception.ResourceNotFoundException;
import com.elearning.course.application.AudienceDTO;
import com.elearning.course.application.CourseRequestDTO;
import com.elearning.course.application.CourseService;
import com.elearning.course.domain.Course;
import com.elearning.course.domain.Language;
import com.elearning.course.domain.Term;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.javamoney.moneta.Money;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.core.oidc.StandardClaimNames;
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

import java.util.Collections;
import java.util.Set;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(CourseController.class)
@Import({SecurityConfig.class, JacksonCustomizations.class})
class CourseControllerTests {

    @Autowired
    MockMvc mockMvc;

    @MockBean
    CourseService courseService;

    @Autowired
    ObjectMapper objectMapper;

    @Test
    void testGetCourses() throws Exception {
        when(courseService.findAll(any())).thenReturn(Collections.emptyList());

        mockMvc.perform(MockMvcRequestBuilders.get("/courses")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().json("[]"));
    }

    @Test
    void testGetCourseById() throws Exception {
        Course course = new Course();
        when(courseService.findById(anyLong())).thenReturn(course);

        mockMvc.perform(MockMvcRequestBuilders.get("/courses/{id}", 1L)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().json("{}"));
    }

    @Test
    void whenCreateCourseWithTeacherRoleThenOK() throws Exception {
        var username = "some_username";

        // Giả lập hành vi của courseService
        when(courseService.createCourse(any(CourseRequestDTO.class))).thenAnswer(invocationOnMock -> {
            CourseRequestDTO dto = invocationOnMock.getArgument(0, CourseRequestDTO.class);
            Course course = dto.toCourse();
            course.setId(1L); // Giả lập ID cho khóa học
            return course;
        });

        var courseDto = new CourseRequestDTO(
                "foo",
                Money.of(100, "USD"),
                "foo description",
                new AudienceDTO(true, Collections.emptySet()),
                Collections.emptySet(),
                null,
                "foo.jpg",
                UUID.randomUUID().toString(),
                Term.LIFETIME,
                Language.ENGLISH,
                Set.of(Language.VIETNAMESE),
                Set.of("Benefit 1"),
                Set.of("Prerequisite 1")
        );

        String requestBody = objectMapper.writeValueAsString(courseDto);

        // Thực hiện yêu cầu và xác minh phản hồi
        MvcResult mvcResult = mockMvc.perform(MockMvcRequestBuilders.post("/courses")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody)
                        .with(SecurityMockMvcRequestPostProcessors.jwt()
                                .authorities(new SimpleGrantedAuthority("ROLE_teacher"))
                                .jwt(builder -> builder
                                        .claim(StandardClaimNames.PREFERRED_USERNAME, username)
                                        .claim("roles", "teacher")
                                ))
                )
                .andExpect(status().isCreated())
                .andReturn();
        String response = mvcResult.getResponse().getContentAsString();
        assertThat(response).isNotNull();
    }

    @Test
    void whenUpdateCourseAndNotAuthenticatedShouldReturn401() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.put("/courses/{id}", 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                      {
                                        "title": "Updated Course"
                                        }
                                """))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void whenUpdateCourseWithTeacherRoleButNotOwnThenReturn400() throws Exception {
        // Arrange
        var courseId = 1L;
        var username = "some_username";

        // Mock isItMyCourse
        when(courseService.isItMyCourse(courseId, username)).thenReturn(false);
        mockMvc.perform(MockMvcRequestBuilders.put("/courses/{id}", 1L)
                        .with(SecurityMockMvcRequestPostProcessors.jwt()
                                .authorities(new SimpleGrantedAuthority("ROLE_teacher"))
                                .jwt(builder -> builder
                                        .claim(StandardClaimNames.PREFERRED_USERNAME, username)
                                        .claim("roles", "teacher")
                                )
                        ))
                .andExpect(MockMvcResultMatchers.status().isBadRequest());

        verify(courseService, times(0)).updateCourse(any(Long.class), any(CourseRequestDTO.class));
    }

    @Test
    void whenUpdateCourseWithTeacherRoleAndOwnThenReturn200() throws Exception {
        // Arrange
        var courseId = 1L;
        var username = "some_username";
        var courseDto = new CourseRequestDTO(
                "foo",
                Money.of(100, "USD"),
                "foo description",
                new AudienceDTO(true, Collections.emptySet()),
                Collections.emptySet(),
                null,
                "foo.jpg",
                UUID.randomUUID().toString(),
                Term.LIFETIME,
                Language.ENGLISH,
                Set.of(Language.VIETNAMESE),
                Set.of("Benefit 1"),
                Set.of("Prerequisite 1")
        );
        String requestBody = objectMapper.writeValueAsString(courseDto);

        // Mock isItMyCourse
        when(courseService.isItMyCourse(courseId, username)).thenReturn(true);
        // Mock update
        when(courseService.updateCourse(eq(courseId), any(CourseRequestDTO.class))).thenAnswer(invocationOnMock -> {
            CourseRequestDTO dto = invocationOnMock.getArgument(1, CourseRequestDTO.class);
            Course course = dto.toCourse();
            course.setId(1L);
            return course;
        });

        mockMvc.perform(MockMvcRequestBuilders.put("/courses/{id}", courseId)
                        .with(SecurityMockMvcRequestPostProcessors.jwt()
                                .authorities(new SimpleGrantedAuthority("ROLE_teacher"))
                                .jwt(builder -> builder
                                        .claim(StandardClaimNames.PREFERRED_USERNAME, username)
                                        .claim("roles", "teacher")
                                )
                        ).contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(MockMvcResultMatchers.status().isOk());
        verify(courseService, times(1)).updateCourse(any(Long.class), any(CourseRequestDTO.class));
    }

    @Test
    void whenUpdateCourseWithAdminRoleThenReturn200() throws Exception {
        // Arrange
        var username = "some_username";
        var courseDto = new CourseRequestDTO(
                "foo",
                Money.of(100, "USD"),
                "foo description",
                new AudienceDTO(true, Collections.emptySet()),
                Collections.emptySet(),
                null,
                "foo.jpg",
                UUID.randomUUID().toString(),
                Term.LIFETIME,
                Language.ENGLISH,
                Set.of(Language.VIETNAMESE),
                Set.of("Benefit 1"),
                Set.of("Prerequisite 1")
        );
        String requestBody = objectMapper.writeValueAsString(courseDto);

        // Mock update
        when(courseService.updateCourse(eq(1L), any(CourseRequestDTO.class))).thenAnswer(invocationOnMock -> {
            CourseRequestDTO dto = invocationOnMock.getArgument(1, CourseRequestDTO.class);
            Course course = dto.toCourse();
            course.setId(1L);
            return course;
        });


        mockMvc.perform(MockMvcRequestBuilders.put("/courses/{id}", 1L)
                        .with(SecurityMockMvcRequestPostProcessors.jwt()
                                .authorities(new SimpleGrantedAuthority("ROLE_admin"))
                                .jwt(builder -> builder
                                        .claim(StandardClaimNames.PREFERRED_USERNAME, username)
                                        .claim("roles", "admin")
                                )
                        ).contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(MockMvcResultMatchers.status().isOk());
        verify(courseService, times(1)).updateCourse(any(Long.class), any(CourseRequestDTO.class));
    }

    @Test
    void whenUpdateCourseWithAdminRoleAndCourseNotFoundThenReturn404() throws Exception {
        // Arrange
        var courseId = 1L;
        var username = "some_username";
        var courseDto = new CourseRequestDTO(
                "foo",
                Money.of(100, "USD"),
                "foo description",
                new AudienceDTO(true, Collections.emptySet()),
                Collections.emptySet(),
                null,
                "foo.jpg",
                UUID.randomUUID().toString(),
                Term.LIFETIME,
                Language.ENGLISH,
                Set.of(Language.VIETNAMESE),
                Set.of("Benefit 1"),
                Set.of("Prerequisite 1")
        );
        String requestBody = objectMapper.writeValueAsString(courseDto);

        // Mock update
        when(courseService.updateCourse(eq(1L), any(CourseRequestDTO.class))).thenThrow(new ResourceNotFoundException(Course.class, courseId));

        mockMvc.perform(MockMvcRequestBuilders.put("/courses/{id}", 1L)
                        .with(SecurityMockMvcRequestPostProcessors.jwt()
                                .authorities(new SimpleGrantedAuthority("ROLE_admin"))
                                .jwt(builder -> builder
                                        .claim(StandardClaimNames.PREFERRED_USERNAME, username)
                                        .claim("roles", "admin")
                                )
                        ).contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(MockMvcResultMatchers.status().isNotFound());
        verify(courseService, times(1)).updateCourse(any(Long.class), any(CourseRequestDTO.class));
    }

    @Test
    void whenDeleteCourseWithNotAuthenticatedShouldReturn401() throws Exception {
        // Arrange
        var courseId = 1L;

        mockMvc.perform(MockMvcRequestBuilders.delete("/courses/{id}", courseId))
                .andExpect(status().isUnauthorized());

        // Verify
        verify(courseService, times(0)).deleteCourse(courseId);
    }

    @Test
    void whenDeleteCourseWithAdminRoleAndCourseNotExistsShouldReturn404() throws Exception {
        // Arrange
        var courseId = 1L;
        var username = "some_username";
        // Mock
        when(courseService.isItMyCourse(courseId, username)).thenReturn(true);
        when(courseService.deleteCourse(courseId)).thenThrow(new ResourceNotFoundException(Course.class, courseId));

        mockMvc.perform(MockMvcRequestBuilders.delete("/courses/{id}", 1L)
                        .with(SecurityMockMvcRequestPostProcessors.jwt()
                                .authorities(new SimpleGrantedAuthority("ROLE_admin"))
                                .jwt(builder -> builder
                                        .claim(StandardClaimNames.PREFERRED_USERNAME, username)
                                        .claim("roles", "admin"))
                        ))
                .andExpect(status().isNotFound());
        // Verify
        verify(courseService, times(1)).deleteCourse(courseId);
    }

    @Test
    void whenDeleteCourseWithTeacherRoleAndCourseExistsButNotOwnShouldReturn400() throws Exception {
        // Arrange
        var courseId = 1L;
        var username = "some_username";
        // Mock
        when(courseService.isItMyCourse(courseId, username)).thenReturn(false);

        mockMvc.perform(MockMvcRequestBuilders.delete("/courses/{id}", 1L)
                        .with(SecurityMockMvcRequestPostProcessors.jwt()
                                .authorities(new SimpleGrantedAuthority("ROLE_teacher"))
                                .jwt(builder -> builder
                                        .claim(StandardClaimNames.PREFERRED_USERNAME, username)
                                        .claim("roles", "teacher")
                                )))
                .andExpect(status().isBadRequest());
        // Verify
        verify(courseService, times(0)).deleteCourse(courseId);
    }

    @Test
    void whenDeleteCourseWithTeacherRoleAndCourseExistsShouldReturn204() throws Exception {
        var courseId = 1L;
        var username = "some_username";
        when(courseService.isItMyCourse(courseId, username)).thenReturn(true);

        mockMvc.perform(MockMvcRequestBuilders.delete("/courses/{id}", 1L)
                        .with(SecurityMockMvcRequestPostProcessors.jwt()
                                .authorities(new SimpleGrantedAuthority("ROLE_teacher"))
                                .jwt(builder -> builder
                                        .claim(StandardClaimNames.PREFERRED_USERNAME, username)
                                        .claim("roles", "teacher")
                                )))
                .andExpect(status().isNoContent());
        // Verify
        verify(courseService, times(1)).deleteCourse(courseId);
    }

}