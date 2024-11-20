package com.el.course.web;

import com.el.TestFactory;
import com.el.common.config.CustomAuthenticationEntryPoint;
import com.el.common.config.SecurityConfig;
import com.el.common.config.jackson.JacksonCustomizations;
import com.el.common.exception.ResourceNotFoundException;
import com.el.course.application.CourseQueryService;
import com.el.course.application.dto.CourseWithoutSectionsDTO;
import com.el.course.domain.Course;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.Pageable;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(BrowseCourseController.class)
@Import({SecurityConfig.class, JacksonCustomizations.class, CustomAuthenticationEntryPoint.class})
class BrowseCourseControllerTests {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private CourseQueryService courseQueryService;

    private Course course;
    private CourseWithoutSectionsDTO courseWithoutSectionsDTO;

    @BeforeEach
    public void setUp() {
        course = TestFactory.createDefaultCourse();
        courseWithoutSectionsDTO = new CourseWithoutSectionsDTO(
                course.getId(),
                course.getTitle(),
                course.getThumbnailUrl(),
                course.getDescription(),
                course.getLanguage(),
                course.getSubtitles(),
                course.getBenefits(),
                course.getPrerequisites(),
                course.getPrice(),
                course.getTeacher(),
                course.getReviews(),
                course.getAverageRating()
        );
    }

    @Test
    void getAllPublishedCourses_ShouldReturnPageOfPublishedCourses() throws Exception {
        List<CourseWithoutSectionsDTO> courseList = List.of(courseWithoutSectionsDTO);
        Mockito.when(courseQueryService.findAllCourseWithoutSectionsDTOs(any(Pageable.class))).thenReturn(courseList);

        mockMvc.perform(get("/published-courses"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").exists());
    }

    @Test
    void getAllPublishedCourses_ShouldReturnEmptyPage_WhenNoPublishedCoursesExist() throws Exception {
        Mockito.when(courseQueryService.findAllCourseWithoutSectionsDTOs(any(Pageable.class))).thenReturn(List.of());

        mockMvc.perform(get("/published-courses"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isEmpty());
    }

    @Test
    void getPublishedCourseById_ShouldReturnPublishedCourse() throws Exception {
        Mockito.when(courseQueryService.findCourseWithoutSectionsDTOById(1L)).thenReturn(courseWithoutSectionsDTO);

        mockMvc.perform(get("/published-courses/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value(course.getTitle()));
    }

    @Test
    void getPublishedCourseById_ShouldReturn404_WhenCourseNotFound() throws Exception {
        Mockito.when(courseQueryService.findCourseWithoutSectionsDTOById(1L)).thenThrow(ResourceNotFoundException.class);

        mockMvc.perform(get("/published-courses/1"))
                .andExpect(status().isNotFound());
    }

}
