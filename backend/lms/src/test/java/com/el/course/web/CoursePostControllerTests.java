package com.el.course.web;

import com.el.TestFactory;
import com.el.common.config.CustomAuthenticationEntryPoint;
import com.el.common.config.SecurityConfig;
import com.el.common.config.jackson.JacksonCustomizations;
import com.el.common.exception.ResourceNotFoundException;
import com.el.course.application.impl.CourseQueryServiceImpl;
import com.el.course.application.impl.CourseServiceImpl;
import com.el.course.domain.Post;
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

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(CoursePostController.class)
@Import({SecurityConfig.class, JacksonCustomizations.class, CustomAuthenticationEntryPoint.class})
class CoursePostControllerTests {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private CourseServiceImpl courseService;

    @MockBean
    private CourseQueryServiceImpl courseQueryService;

    @Test
    void getAllPosts_ValidCourseId_ReturnsPosts() throws Exception {
        Pageable pageable = PageRequest.of(0, 10);
        List<Post> posts = List.of(TestFactory.createDefaultPost(), TestFactory.createDefaultPost());
        when(courseQueryService.findAllPostsByCourseId(1L, pageable)).thenReturn(posts);

        mockMvc.perform(get("/courses/1/posts")
                        .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_user"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").exists());
    }

    @Test
    void getPost_ValidCourseIdAndPostId_ReturnsPost() throws Exception {
        Post post = TestFactory.createDefaultPost();
        when(courseQueryService.findPostByCourseIdAndPostId(any(), any())).thenReturn(post);

        mockMvc.perform(get("/courses/1/posts/1")
                        .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_user"))))
                .andExpect(status().isOk());
    }

    @Test
    void getPost_NotFoundPost_ReturnsNotFound() throws Exception {
        when(courseQueryService.findPostByCourseIdAndPostId(any(), any()))
                .thenThrow(new ResourceNotFoundException());

        mockMvc.perform(get("/courses/1/posts/1")
                        .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_user"))))
                .andExpect(status().isNotFound());
    }

    @Test
    void createPost_ValidCourseIdAndPost_ReturnsPostId() throws Exception {
        when(courseService.addPost(any(), any())).thenReturn(1L);

        mockMvc.perform(post("/courses/1/posts")
                        .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_teacher")))
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(TestFactory.createDefaultCoursePostDTO()))
                )
                .andExpect(status().isOk());
    }

    @Test
    void createPost_InvalidCourseId_ReturnsBadRequest() throws Exception {
        mockMvc.perform(post("/courses/1/posts")
                        .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_teacher")))
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(TestFactory.createCoursePostDTOBadRequest()))
                )
                .andExpect(status().isBadRequest());
    }

    @Test
    void createPost_RoleIsUser_ReturnsForbidden() throws Exception {
        mockMvc.perform(post("/courses/1/posts")
                        .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_user")))
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(TestFactory.createDefaultCoursePostDTO()))
                )
                .andExpect(status().isForbidden());
    }

    @Test
    void updatePost_ValidCourseIdAndPost_ReturnsOk() throws Exception {
        mockMvc.perform(put("/courses/1/posts/1")
                        .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_teacher")))
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(TestFactory.createDefaultCoursePostDTO()))
                )
                .andExpect(status().isOk());
    }

    @Test
    void deletePost_ValidCourseIdAndPost_ReturnsNoContent() throws Exception {
        mockMvc.perform(delete("/courses/1/posts/1")
                        .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_teacher")))
                )
                .andExpect(status().isNoContent());
    }

    @Test
    void restorePost_ValidCourseIdAndPost_ReturnsOk() throws Exception {
        mockMvc.perform(post("/courses/1/posts/1/restore")
                        .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_teacher")))
                )
                .andExpect(status().isOk());
    }

    @Test
    void deletePost_ForceTrue_ReturnsNoContent() throws Exception {
        mockMvc.perform(delete("/courses/1/posts/1?force=true")
                        .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_teacher")))
                )
                .andExpect(status().isNoContent());
    }

}
