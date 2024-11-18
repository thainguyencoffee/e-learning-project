package com.el.course.web;

import com.el.TestFactory;
import com.el.common.config.CustomAuthenticationEntryPoint;
import com.el.common.config.SecurityConfig;
import com.el.common.config.jackson.JacksonCustomizations;
import com.el.common.exception.ResourceNotFoundException;
import com.el.course.application.dto.QuizDTO;
import com.el.course.application.dto.QuizUpdateDTO;
import com.el.course.application.impl.CourseQueryServiceImpl;
import com.el.course.application.impl.CourseServiceImpl;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(CourseQuizController.class)
@Import({SecurityConfig.class, JacksonCustomizations.class, CustomAuthenticationEntryPoint.class})
class CourseQuizControllerTests {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private CourseServiceImpl courseService;

    @MockBean
    private CourseQueryServiceImpl courseQueryService;

    @Test
    void addQuizToSection_ValidRequest_ReturnsCreatedStatus() throws Exception {
        QuizDTO quizDTO = TestFactory.createDefaultQuizDTO(1L);
        when(courseService.addQuizToSection(anyLong(), anyLong(), any(QuizDTO.class))).thenReturn(1L);

        mockMvc.perform(post("/courses/1/sections/1/quizzes")
                        .contentType("application/json")
                        .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_teacher")))
                        .content(objectMapper.writeValueAsString(quizDTO)))
                .andExpect(status().isOk());
    }

    @Test
    void addQuizToSection_CourseNotFound_ReturnsNotFoundStatus() throws Exception {
        QuizDTO quizDTO = TestFactory.createDefaultQuizDTO(1L);
        when(courseService.addQuizToSection(anyLong(), anyLong(), any(QuizDTO.class))).thenThrow(new ResourceNotFoundException());

        mockMvc.perform(post("/courses/1/sections/1/quizzes")
                        .contentType("application/json")
                        .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_teacher")))
                        .content(objectMapper.writeValueAsString(quizDTO)))
                .andExpect(status().isNotFound());
    }

    @Test
    void addQuizToSection_NoPermission_ReturnsForbiddenStatus() throws Exception {
        QuizDTO quizDTO = TestFactory.createDefaultQuizDTO(1L);

        mockMvc.perform(post("/courses/1/sections/1/quizzes")
                        .contentType("application/json")
                        .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_user")))
                        .content(objectMapper.writeValueAsString(quizDTO)))
                .andExpect(status().isForbidden());
    }

    @Test
    void updateQuiz_ValidRequest_ReturnsOkStatus() throws Exception {
        QuizUpdateDTO quizDTO = TestFactory.createDefaultQuizUpdateDTO();
        doNothing().when(courseService).updateQuiz(anyLong(), anyLong(), anyLong(), any(QuizUpdateDTO.class));

        mockMvc.perform(MockMvcRequestBuilders.put("/courses/1/sections/1/quizzes/1")
                        .contentType("application/json")
                        .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_teacher")))
                        .content(objectMapper.writeValueAsString(quizDTO)))
                .andExpect(status().isOk());
    }

    @Test
    void updateQuiz_CourseNotFound_ReturnsNotFoundStatus() throws Exception {
        QuizUpdateDTO quizDTO = TestFactory.createDefaultQuizUpdateDTO();
        doThrow(new ResourceNotFoundException()).when(courseService).updateQuiz(anyLong(), anyLong(), anyLong(), any(QuizUpdateDTO.class));

        mockMvc.perform(MockMvcRequestBuilders.put("/courses/1/sections/1/quizzes/1")
                        .contentType("application/json")
                        .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_teacher")))
                        .content(objectMapper.writeValueAsString(quizDTO)))
                .andExpect(status().isNotFound());
    }

    @Test
    void updateQuiz_NoPermission_ReturnsForbiddenStatus() throws Exception {
        QuizUpdateDTO quizDTO = TestFactory.createDefaultQuizUpdateDTO();

        mockMvc.perform(MockMvcRequestBuilders.put("/courses/1/sections/1/quizzes/1")
                        .contentType("application/json")
                        .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_user")))
                        .content(objectMapper.writeValueAsString(quizDTO)))
                .andExpect(status().isForbidden());
    }

    @Test
    void deleteQuiz_ValidRequest_ReturnsNoContentStatus() throws Exception {
        doNothing().when(courseService).deleteQuiz(anyLong(), anyLong(), anyLong());

        mockMvc.perform(MockMvcRequestBuilders.delete("/courses/1/sections/1/quizzes/1")
                        .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_teacher")))
                )
                .andExpect(status().isNoContent());
    }

    @Test
    void deleteForceQuiz_ValidRequest_ReturnsNoContentStatus() throws Exception {
        doNothing().when(courseService).deleteForceQuiz(anyLong(), anyLong(), anyLong());

        mockMvc.perform(MockMvcRequestBuilders.delete("/courses/1/sections/1/quizzes/1?force=true")
                        .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_teacher")))
                )
                .andExpect(status().isNoContent());
    }

    @Test
    void restoreQuiz_ValidRequest_ReturnsOkStatus() throws Exception {
        doNothing().when(courseService).restoreQuiz(anyLong(), anyLong(), anyLong());

        mockMvc.perform(post("/courses/1/sections/1/quizzes/1/restore")
                        .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_teacher")))
                )
                .andExpect(status().isOk());
    }

    @Test
    void addQuestionToQuiz_ValidRequest_ReturnsCreatedStatus() throws Exception {
        when(courseService.addQuestionToQuiz(anyLong(), anyLong(), anyLong(), any())).thenReturn(1L);

        mockMvc.perform(post("/courses/1/sections/1/quizzes/1/questions")
                        .contentType("application/json")
                        .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_teacher")))
                        .content(objectMapper.writeValueAsString(TestFactory.createDefaultQuestionDTO()))
                )
                .andExpect(status().isOk());
    }

    @Test
    void addQuestionToQuiz_NoPermission_ReturnsForbiddenStatus() throws Exception {
        mockMvc.perform(post("/courses/1/sections/1/quizzes/1/questions")
                        .contentType("application/json")
                        .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_user")))
                        .content(objectMapper.writeValueAsString(TestFactory.createDefaultQuestionDTO()))
                )
                .andExpect(status().isForbidden());
    }

    @Test
    void updateQuestion_ValidRequest_ReturnsOkStatus() throws Exception {
        doNothing().when(courseService).updateQuestion(anyLong(), anyLong(), anyLong(), anyLong(), any());

        mockMvc.perform(MockMvcRequestBuilders.put("/courses/1/sections/1/quizzes/1/questions/1")
                        .contentType("application/json")
                        .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_teacher")))
                        .content(objectMapper.writeValueAsString(TestFactory.createDefaultQuestionDTO()))
                )
                .andExpect(status().isOk());
    }

    @Test
    void updateQuestion_NoPermission_ReturnsForbiddenStatus() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.put("/courses/1/sections/1/quizzes/1/questions/1")
                        .contentType("application/json")
                        .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_user")))
                        .content(objectMapper.writeValueAsString(TestFactory.createDefaultQuestionDTO()))
                )
                .andExpect(status().isForbidden());
    }

    @Test
    void deleteQuestion_ValidRequest_ReturnsNoContentStatus() throws Exception {
        doNothing().when(courseService).deleteQuestion(anyLong(), anyLong(), anyLong(), anyLong());

        mockMvc.perform(MockMvcRequestBuilders.delete("/courses/1/sections/1/quizzes/1/questions/1")
                        .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_teacher")))
                )
                .andExpect(status().isNoContent());
    }

    @Test
    void deleteQuestion_CourseNotFound_ReturnsNotFoundStatus() throws Exception {
        doThrow(new ResourceNotFoundException()).when(courseService).deleteQuestion(anyLong(), anyLong(), anyLong(), anyLong());

        mockMvc.perform(MockMvcRequestBuilders.delete("/courses/1/sections/1/quizzes/1/questions/1")
                        .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_teacher")))
                )
                .andExpect(status().isNotFound());
    }

    @Test
    void deleteQuestion_NoPermission_ReturnsForbiddenStatus() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.delete("/courses/1/sections/1/quizzes/1/questions/1")
                        .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_user")))
                )
                .andExpect(status().isForbidden());
    }


}
