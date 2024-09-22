package com.elearning.course.web;

import com.elearning.common.config.SecurityConfig;
import com.elearning.common.util.ResourceNotFoundException;
import com.elearning.course.application.CourseRequestDTO;
import com.elearning.course.application.CourseService;
import com.elearning.course.domain.Course;
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
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

import java.util.Collections;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(CourseController.class)
@Import(SecurityConfig.class)
class CourseControllerTests {

    @Autowired
    MockMvc mockMvc;

    @MockBean
    CourseService courseService;

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

        when(courseService.createCourse(any(CourseRequestDTO.class))).thenReturn(1L);

        mockMvc.perform(MockMvcRequestBuilders.post("/courses")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"title\": \"New Course\"}")
                        .with(SecurityMockMvcRequestPostProcessors.jwt()
                                .authorities(new SimpleGrantedAuthority("ROLE_teacher"))
                                .jwt(builder -> builder
                                        .claim(StandardClaimNames.PREFERRED_USERNAME, username)
                                        .claim("roles", "teacher")
                                ))
                )
                .andExpect(status().isCreated())
                .andExpect(content().json("1"));
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
        var courseId = 1L;
        var username = "some_username";
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
    void whenUpdateCourseWithAdminRoleThenReturn200() throws Exception {
        var username = "some_username";
        mockMvc.perform(MockMvcRequestBuilders.put("/courses/{id}", 1L)
                        .with(SecurityMockMvcRequestPostProcessors.jwt()
                                .authorities(new SimpleGrantedAuthority("ROLE_admin"))
                                .jwt(builder -> builder
                                        .claim(StandardClaimNames.PREFERRED_USERNAME, username)
                                        .claim("roles", "admin")
                                )
                        ).contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "title": "Updated Course"
                                }"""))
                .andExpect(MockMvcResultMatchers.status().isOk());
        verify(courseService, times(1)).updateCourse(any(Long.class), any(CourseRequestDTO.class));
    }


    @Test
    void whenDeleteCourseWithNotAuthenticatedShouldReturn401() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.delete("/courses/{id}", 1L))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void whenDeleteCourseWithTeacherRoleAndCourseNotExistsShouldReturn404() throws Exception {
        var courseId = 1L;
        var username = "some_username";
        when(courseService.isItMyCourse(courseId, username)).thenReturn(true);
        when(courseService.deleteCourse(courseId)).thenThrow(new ResourceNotFoundException(Course.class, courseId));

        mockMvc.perform(MockMvcRequestBuilders.delete("/courses/{id}", 1L)
                        .with(SecurityMockMvcRequestPostProcessors.jwt()
                                .authorities(new SimpleGrantedAuthority("ROLE_teacher"))
                                .jwt(builder -> builder
                                        .claim(StandardClaimNames.PREFERRED_USERNAME, username)
                                        .claim("roles", "teacher"))
                        ))
                .andExpect(status().isNotFound());
    }

    @Test
    void whenDeleteCourseWithTeacherRoleAndCourseExistsButNotOwnShouldReturn400() throws Exception {
        var courseId = 1L;
        var username = "some_username";
        when(courseService.isItMyCourse(courseId, username)).thenReturn(false);

        mockMvc.perform(MockMvcRequestBuilders.delete("/courses/{id}", 1L)
                        .with(SecurityMockMvcRequestPostProcessors.jwt()
                                .authorities(new SimpleGrantedAuthority("ROLE_teacher"))
                                .jwt(builder -> builder
                                        .claim(StandardClaimNames.PREFERRED_USERNAME, username)
                                        .claim("roles", "teacher")
                                )))
                .andExpect(status().isBadRequest());
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
    }


}