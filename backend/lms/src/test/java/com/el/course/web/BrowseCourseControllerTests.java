package com.el.course.web;

import com.el.common.config.SecurityConfig;
import com.el.common.config.jackson.JacksonCustomizations;
import com.el.common.exception.ResourceNotFoundException;
import com.el.course.application.CourseQueryService;
import com.el.course.domain.Course;
import com.el.course.domain.Language;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.Set;

import static org.mockito.ArgumentMatchers.any;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(BrowseCourseController.class)
@Import({SecurityConfig.class, JacksonCustomizations.class})
class BrowseCourseControllerTests {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private CourseQueryService courseQueryService;

    private Course course;

    @BeforeEach
    public void setUp() {
        course = new Course(
                "Java Programming",
                "Learn Java from scratch",
                "http://example.com/image.jpg",
                Set.of("OOP", "Concurrency"),
                Language.ENGLISH,
                Set.of("Basic Programming Knowledge"),
                Set.of(Language.ENGLISH, Language.SPANISH),
                "teacher123"
        );
    }

    @Test
    void getAllPublishedCourses_ShouldReturnPageOfPublishedCourses() throws Exception {
        Page<Course> coursePage = new PageImpl<>(List.of(course));
        Mockito.when(courseQueryService.findAllPublishedCourses(any(Pageable.class))).thenReturn(coursePage);

        mockMvc.perform(get("/published-courses"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").exists());
    }

    @Test
    void getAllPublishedCourses_ShouldReturnEmptyPage_WhenNoPublishedCoursesExist() throws Exception {
        Page<Course> emptyPage = Page.empty();
        Mockito.when(courseQueryService.findAllPublishedCourses(any(Pageable.class))).thenReturn(emptyPage);

        mockMvc.perform(get("/published-courses"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isEmpty());
    }

    @Test
    void getPublishedCourseById_ShouldReturnPublishedCourse() throws Exception {
        Mockito.when(courseQueryService.findPublishedCourseById(1L)).thenReturn(course);

        mockMvc.perform(get("/published-courses/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("Java Programming"));
    }

    @Test
    void getPublishedCourseById_ShouldReturn404_WhenCourseNotFound() throws Exception {
        Mockito.when(courseQueryService.findPublishedCourseById(1L)).thenThrow(ResourceNotFoundException.class);

        mockMvc.perform(get("/published-courses/1"))
                .andExpect(status().isNotFound());
    }

}
