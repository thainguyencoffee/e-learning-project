package com.el.course.web;

import com.el.TestFactory;
import com.el.common.Currencies;
import com.el.common.config.CustomAuthenticationEntryPoint;
import com.el.common.config.jackson.JacksonCustomizations;
import com.el.common.config.SecurityConfig;
import com.el.common.exception.InputInvalidException;
import com.el.common.exception.ResourceNotFoundException;
import com.el.course.application.CourseQueryService;
import com.el.course.application.dto.*;
import com.el.course.application.impl.CourseServiceImpl;
import com.el.course.domain.Course;
import com.el.course.domain.Language;
import com.el.course.domain.Lesson;
import com.el.course.web.dto.*;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.javamoney.moneta.Money;
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
import org.springframework.http.MediaType;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.Set;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(CourseManagementController.class)
@Import({SecurityConfig.class, JacksonCustomizations.class, CustomAuthenticationEntryPoint.class})
class CourseManagementControllerTests {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private CourseServiceImpl courseService;

    @MockBean
    private CourseQueryService courseQueryService;

    @Autowired
    private ObjectMapper objectMapper;

    private CourseDTO courseDTO;
    private Course course;

    @BeforeEach
    public void setUp() {
        // Setup CourseDTO với dữ liệu mẫu
        courseDTO = TestFactory.createDefaultCourseDTO();

        course = TestFactory.createDefaultCourse();
    }

    @Test
    void getAllCourses_ShouldReturnPageOfCourses() throws Exception {
        Page<Course> coursePage = new PageImpl<>(List.of(course));

        when(courseQueryService.findAllCourses(any(Pageable.class))).thenReturn(coursePage);

        mockMvc.perform(get("/courses")
                        .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_admin"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").exists());
    }

    @Test
    void getAllCourses_ShouldReturnForbidden_WhenAuthenticatedWithUserRole() throws Exception {
        mockMvc.perform(get("/courses")
                        .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_user"))))
                .andExpect(status().isForbidden());
    }

    @Test
    void getAllCourses_ShouldReturnUnauthorized_WhenNotAuthenticated() throws Exception {
        mockMvc.perform(get("/courses"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void getCourseById_ShouldReturnCourse_WhenCourseExistsAndTeacherRole() throws Exception {
        when(courseQueryService.findCourseById(1L)).thenReturn(course);

        mockMvc.perform(get("/courses/1")
                        .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_teacher"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value(course.getTitle()));
    }

    @Test
    void getCourseById_ShouldReturnCourse_WhenCourseExistsAndAdminRole() throws Exception {
        when(courseQueryService.findCourseById(1L)).thenReturn(course);

        mockMvc.perform(get("/courses/1")
                        .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_admin"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value(course.getTitle()));
    }

    @Test
    void getCourseById_ShouldReturnNotFound_WhenCourseDoesNotExist() throws Exception {
        when(courseQueryService.findCourseById(1L)).thenThrow(new ResourceNotFoundException());

        mockMvc.perform(get("/courses/1")
                        .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_teacher"))))
                .andExpect(status().isNotFound());
    }

    @Test
    void getCourseById_ShouldReturnForbidden_WhenUser() throws Exception {
        mockMvc.perform(get("/courses/1")
                        .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_user"))))
                .andExpect(status().isForbidden());
    }

    @Test
    void getCourseById_ShouldReturnUnauthenticated_WhenIsNotAuthenticated() throws Exception {
        mockMvc.perform(get("/courses/1"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    public void testCreateCourse_ShouldReturnCreatedStatus() throws Exception {
        Course createdCourse = Mockito.mock(Course.class);
        when(createdCourse.getId()).thenReturn(1L);

        // Mock
        when(createdCourse.getTitle()).thenReturn(courseDTO.title());
        when(createdCourse.getDescription()).thenReturn(courseDTO.description());
        when(createdCourse.getTeacher()).thenReturn("teacher123");

        // Mock
        when(courseService.createCourse(any(), any()))
                .thenReturn(createdCourse);

        mockMvc.perform(post("/courses")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(courseDTO))
                        .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_teacher"))))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", "/courses/1"))
                .andExpect(jsonPath("$.title").value(courseDTO.title()))
                .andExpect(jsonPath("$.teacher").value("teacher123"));
    }

    @Test
    public void testCreateCourse_ShouldReturnForbidden_WhenUserIsNotTeacher() throws Exception {
        mockMvc.perform(post("/courses")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(courseDTO))
                        .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_user"))))
                .andExpect(status().isForbidden());
    }

    @Test
    public void testCreateCourse_ShouldReturnBadRequest_WhenTitleIsBlank() throws Exception {
        CourseDTO invalidCourseDTO = new CourseDTO(
                "",
                "Learn Java from scratch",
                "http://example.com/image.jpg",
                Set.of("OOP", "Concurrency"),
                Language.ENGLISH,
                Set.of("Basic Programming Knowledge"),
                Set.of(Language.ENGLISH, Language.SPANISH)
        );

        mockMvc.perform(post("/courses")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidCourseDTO))
                        .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_teacher"))))
                .andExpect(status().isBadRequest());
    }

    @Test
    void testUpdateInfoCourse_ShouldReturnOKStatus() throws Exception {
        Course updatedCourse = Mockito.mock(Course.class);
        when(updatedCourse.getId()).thenReturn(1L);
        when(updatedCourse.getTitle()).thenReturn("Java Programming");
        when(updatedCourse.getDescription()).thenReturn("Learn Java from scratch");

        when(courseService.updateCourse(any(Long.class), any(CourseUpdateDTO.class)))
                .thenReturn(updatedCourse);

        mockMvc.perform(put("/courses/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(courseDTO))
                        .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_teacher")))
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("Java Programming"))
                .andExpect(jsonPath("$.description").value("Learn Java from scratch"));
    }

    @Test
    void testUpdateInfoCourse_ShouldReturnBadRequest_WhenTitleIsBlank() throws Exception {
        // CourseUpdateDTO with title blank
        CourseUpdateDTO invalidCourseUpdateDTO = new CourseUpdateDTO(
                "",
                "Learn Java from scratch",
                "http://example.com/image.jpg",
                Set.of("OOP", "Concurrency"),
                Set.of("Basic Programming Knowledge"),
                Set.of(Language.ENGLISH, Language.SPANISH)
        );

        mockMvc.perform(put("/courses/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidCourseUpdateDTO))
                        .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_teacher")))
                )
                .andExpect(status().isBadRequest());
    }

    @Test
    void testUpdateInfoCourse_ShouldReturnForbidden_WhenUserIsNotTeacher() throws Exception {
        mockMvc.perform(put("/courses/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}")
                        .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_user")))
                )
                .andExpect(status().isForbidden());
    }


    @Test
    void testUpdateInfoCourse_ShouldReturnBadRequest_WhenCourseIsPublished() throws Exception {
        // Giả lập hành vi của UpdateCourseUseCase ném ra InputInvalidException
        Mockito.doThrow(new InputInvalidException("Cannot update a published course."))
                .when(courseService).updateCourse(any(), any());

        mockMvc.perform(put("/courses/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(courseDTO))
                        .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_teacher")))
                )
                .andExpect(status().isBadRequest());
    }


    @Test
    void deleteCourse_ValidCourseId_ShouldReturnNoContent() throws Exception {
        Mockito.doNothing().when(courseService).deleteCourse(1L);

        mockMvc.perform(delete("/courses/1")
                        .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_teacher"))))
                .andExpect(status().isNoContent());
    }

    @Test
    void deleteCourse_CourseNotFound_ShouldReturnNotFound() throws Exception {
        Mockito.doThrow(new ResourceNotFoundException()).when(courseService).deleteCourse(1L);

        mockMvc.perform(delete("/courses/1")
                        .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_teacher"))))
                .andExpect(status().isNotFound());
    }

    @Test
    void deleteCourse_UserNotTeacher_ShouldReturnForbidden() throws Exception {
        mockMvc.perform(delete("/courses/1")
                        .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_user"))))
                .andExpect(status().isForbidden());
    }

    @Test
    void deleteCourse_CourseAlreadyPublished_ShouldReturnBadRequest() throws Exception {
        Mockito.doThrow(new InputInvalidException("Cannot delete a published course.")).when(courseService).deleteCourse(1L);

        mockMvc.perform(delete("/courses/1")
                        .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_teacher"))))
                .andExpect(status().isBadRequest());
    }

    @Test
    void deleteForceCourse_ValidCourseId_ShouldReturnNoContent() throws Exception {
        Mockito.doNothing().when(courseService).deleteCourseForce(1L);

        mockMvc.perform(delete("/courses/1?force=true")
                        .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_teacher")))
                )
                .andExpect(status().isNoContent());
    }

    @Test
    void changePrice_ValidCourseIdAndPrice_ShouldReturnUpdatedCourse() throws Exception {
        Course updatedCourse = Mockito.mock(Course.class);
        when(updatedCourse.getId()).thenReturn(1L);
        when(courseService.updatePrice(1L, Money.of(100, Currencies.VND))).thenReturn(updatedCourse);

        String body = objectMapper.writeValueAsString(new UpdatePriceDTO(Money.of(100, Currencies.VND)));

        mockMvc.perform(put("/courses/1/update-price")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body)
                        .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_admin"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L));
    }

    @Test
    void changePrice_CourseNotFound_ShouldReturnNotFound() throws Exception {
        Mockito.doThrow(new ResourceNotFoundException()).when(courseService).updatePrice(1L, Money.of(100, Currencies.VND));

        String body = objectMapper.writeValueAsString(new UpdatePriceDTO(Money.of(100, Currencies.VND)));

        mockMvc.perform(put("/courses/1/update-price")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body)
                        .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_admin"))))
                .andExpect(status().isNotFound());
    }

    @Test
    void changePrice_UserNotAdmin_ShouldReturnForbidden() throws Exception {
        String body = objectMapper.writeValueAsString(new UpdatePriceDTO(Money.of(100, Currencies.VND)));
        mockMvc.perform(put("/courses/1/update-price")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body)
                        .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_teacher"))))
                .andExpect(status().isForbidden());
    }

    @Test
    void changePrice_CourseAlreadyPublished_ShouldReturnBadRequest() throws Exception {
        Mockito.doThrow(new InputInvalidException("Cannot change price of a published course.")).when(courseService).updatePrice(1L, Money.of(100, Currencies.VND));

        String body = objectMapper.writeValueAsString(new UpdatePriceDTO(Money.of(100, Currencies.VND)));

        mockMvc.perform(put("/courses/1/update-price")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body)
                        .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_admin")))
                )
                .andExpect(status().isBadRequest());
    }

    @Test
    void changePrice_NewPriceInvalid_ShouldReturnBadRequest() throws Exception {
        String body = objectMapper.writeValueAsString(new UpdatePriceDTO(null));

        mockMvc.perform(put("/courses/1/update-price")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body)
                        .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_admin")))
                )
                .andExpect(status().isBadRequest());
    }

    @Test
    void assignTeacher_ValidCourseIdAndTeacher_ShouldReturnOk() throws Exception {
        Course updatedCourse = Mockito.mock(Course.class);
        when(updatedCourse.getId()).thenReturn(1L);
        when(courseService.assignTeacher(1L, "NewTeacher")).thenReturn(updatedCourse);

        String body = objectMapper.writeValueAsString(new AssignTeacherDTO("NewTeacher"));

        mockMvc.perform(put("/courses/1/assign-teacher")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body)
                        .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_admin"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L));
    }

    @Test
    void assignTeacher_CourseNotFound_ShouldReturnNotFound() throws Exception {
        Mockito.doThrow(new ResourceNotFoundException()).when(courseService).assignTeacher(1L, "NewTeacher");
        String body = objectMapper.writeValueAsString(new AssignTeacherDTO("NewTeacher"));

        mockMvc.perform(put("/courses/1/assign-teacher")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body)
                        .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_admin"))))
                .andExpect(status().isNotFound());
    }

    @Test
    void assignTeacher_UserNotAdmin_ShouldReturnForbidden() throws Exception {
        String body = objectMapper.writeValueAsString(new AssignTeacherDTO("NewTeacher"));
        mockMvc.perform(put("/courses/1/assign-teacher")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body)
                        .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_teacher"))))
                .andExpect(status().isForbidden());
    }

    @Test
    void assignTeacher_NullTeacher_ShouldReturnBadRequest() throws Exception {
        String body = objectMapper.writeValueAsString(new AssignTeacherDTO(null));
        mockMvc.perform(put("/courses/1/assign-teacher")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body)
                        .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_admin"))))
                .andExpect(status().isBadRequest());
    }

    @Test
    void assignTeacher_CoursePublished_ShouldReturnBadRequest() throws Exception {
        Mockito.doThrow(new InputInvalidException("Cannot assign teacher to a published course.")).when(courseService).assignTeacher(1L, "NewTeacher");
        String body = objectMapper.writeValueAsString(new AssignTeacherDTO("NewTeacher"));

        mockMvc.perform(put("/courses/1/assign-teacher")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body)
                        .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_admin")))
                )
                .andExpect(status().isBadRequest());
    }


    @Test
    void requestPublish_shouldReturnOk_whenValidPublishRequest() throws Exception {
        CourseRequestDTO courseRequestDTO = TestFactory.createDefaultCourseRequestDTOPublish();
        String body = objectMapper.writeValueAsString(courseRequestDTO);

        mockMvc.perform(post("/courses/1/requests")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body)
                        .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_admin"))))
                .andExpect(status().isOk());
    }

    @Test
    void requestPublish_shouldReturn400_whenServiceThrows() throws Exception {
        CourseRequestDTO courseRequestDTO = TestFactory.createDefaultCourseRequestDTOPublish();
        String body = objectMapper.writeValueAsString(courseRequestDTO);

        Mockito.doThrow(new InputInvalidException("some thing err"))
                .when(courseService).requestPublish(1L, courseRequestDTO);

        mockMvc.perform(post("/courses/1/requests")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body)
                        .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_admin")))
                )
                .andExpect(status().isBadRequest());
    }

    @Test
    void requestPublish_shouldReturnOk_whenValidPublishRequestAndRoleIsTeacher() throws Exception {
        CourseRequestDTO courseRequestDTO = TestFactory.createDefaultCourseRequestDTOPublish();
        String body = objectMapper.writeValueAsString(courseRequestDTO);

        mockMvc.perform(post("/courses/1/requests")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body)
                        .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_teacher"))))
                .andExpect(status().isOk());
    }

    @Test
    void requestUnPublish_shouldReturnOk_whenValidUnPublishRequest() throws Exception {
        CourseRequestDTO courseRequestDTO = TestFactory.createDefaultCourseRequestDTOUnPublish();
        String body = objectMapper.writeValueAsString(courseRequestDTO);

        mockMvc.perform(post("/courses/1/requests")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body)
                        .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_admin"))))
                .andExpect(status().isOk());
    }

    @Test
    void requestUnPublish_shouldReturn400_whenServiceThrows() throws Exception {
        CourseRequestDTO courseRequestDTO = TestFactory.createDefaultCourseRequestDTOUnPublish();
        String body = objectMapper.writeValueAsString(courseRequestDTO);

        Mockito.doThrow(new InputInvalidException("some thing err"))
                .when(courseService).requestUnpublish(1L, courseRequestDTO);

        mockMvc.perform(post("/courses/1/requests")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body)
                        .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_admin")))
                )
                .andExpect(status().isBadRequest());
    }

    @Test
    void requestUnPublish_shouldReturnOk_whenValidUnPublishRequestAndRoleIsTeacher() throws Exception {
        CourseRequestDTO courseRequestDTO = TestFactory.createDefaultCourseRequestDTOUnPublish();
        String body = objectMapper.writeValueAsString(courseRequestDTO);

        mockMvc.perform(post("/courses/1/requests")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body)
                        .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_teacher"))))
                .andExpect(status().isOk());
    }

    @Test
    void approvePublish_shouldReturnOk_whenValidApprovePublishRequest() throws Exception {
        CourseRequestApproveDTO approveDTO = TestFactory.createDefaultCourseRequestApproveDTOPublish();

        mockMvc.perform(put("/courses/1/requests/1/approve")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(approveDTO))
                        .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_admin")))
                )
                .andExpect(status().isOk());
    }

    @Test
    void approvePublish_shouldReturn400_whenServiceThrows() throws Exception {
        CourseRequestApproveDTO approveDTO = TestFactory.createDefaultCourseRequestApproveDTOPublish();

        Mockito.doThrow(new InputInvalidException("some thing err"))
                .when(courseService).approvePublish(any(), any(), any());

        mockMvc.perform(put("/courses/1/requests/1/approve")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(approveDTO))
                        .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_admin")))
                )
                .andExpect(status().isBadRequest());
    }

    @Test
    void approvePublish_shouldReturn400_whenInvalidData() throws Exception {
        CourseRequestApproveDTO approveDTO = new CourseRequestApproveDTO(null, "", "");

        mockMvc.perform(put("/courses/1/requests/1/approve")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(approveDTO))
                        .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_admin")))
                )
                .andExpect(status().isBadRequest());
    }

    @Test
    void approvePublish_shouldReturn403_whenRoleIsTeacher() throws Exception {
        CourseRequestApproveDTO approveDTO = TestFactory.createDefaultCourseRequestApproveDTOPublish();

        mockMvc.perform(put("/courses/1/requests/1/approve")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(approveDTO))
                        .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_teacher")))
                )
                .andExpect(status().isForbidden());
    }

    @Test
    void rejectPublish_shouldReturnOk_whenValidRejectPublishRequest() throws Exception {
        CourseRequestRejectDTO rejectDTO = TestFactory.createDefaultCourseRequestRejectDTOPublish();

        mockMvc.perform(put("/courses/1/requests/1/reject")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(rejectDTO))
                        .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_admin")))
                )
                .andExpect(status().isOk());
    }

    @Test
    void rejectPublish_shouldReturn400_whenServiceThrows() throws Exception {
        CourseRequestRejectDTO rejectDTO = TestFactory.createDefaultCourseRequestRejectDTOPublish();

        Mockito.doThrow(new InputInvalidException("some thing err"))
                .when(courseService).rejectPublish(any(), any(), any());

        mockMvc.perform(put("/courses/1/requests/1/reject")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(rejectDTO))
                        .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_admin")))
                )
                .andExpect(status().isBadRequest());
    }

    @Test
    void rejectPublish_shouldReturn400_whenInvalidData() throws Exception {
        CourseRequestRejectDTO rejectDTO = new CourseRequestRejectDTO(null, "", "");

        mockMvc.perform(put("/courses/1/requests/1/reject")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(rejectDTO))
                        .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_admin")))
                )
                .andExpect(status().isBadRequest());
    }

    @Test
    void rejectPublish_shouldReturn403_whenRolesTeacher() throws Exception {
        CourseRequestRejectDTO rejectDTO = TestFactory.createDefaultCourseRequestRejectDTOPublish();

        mockMvc.perform(put("/courses/1/requests/1/reject")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(rejectDTO))
                        .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_teacher")))
                )
                .andExpect(status().isForbidden());
    }


    @Test
    void addSection_ValidCourseIdAndSection_ReturnsUpdatedCourse() throws Exception {
        long expectedSectionId = 1L;
        when(courseService.addSection(any(Long.class), any(CourseSectionDTO.class)))
                .thenReturn(expectedSectionId);

        CourseSectionDTO sectionDTO = new CourseSectionDTO("Billie Jean [4K] 30th Anniversary, 2001");

        mockMvc.perform(post("/courses/1/sections")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(sectionDTO))
                        .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_teacher"))))
                .andExpect(status().isOk())
                .andExpect(content().string(expectedSectionId + ""));
    }

    @Test
    void addSection_CourseNotFound_ThrowsException() throws Exception {
        Mockito.doThrow(new ResourceNotFoundException()).when(courseService).addSection(any(Long.class), any(CourseSectionDTO.class));

        CourseSectionDTO sectionDTO = new CourseSectionDTO("Billie Jean [4K] 30th Anniversary, 2001");

        mockMvc.perform(post("/courses/1/sections")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(sectionDTO))
                        .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_teacher"))))
                .andExpect(status().isNotFound());
    }


    @Test
    void addSection_SectionWithBlankTitle_ThrowsException() throws Exception {
        Mockito.doThrow(new InputInvalidException("Section with the same title already exists."))
                .when(courseService).addSection(any(Long.class), any(CourseSectionDTO.class));

        CourseSectionDTO sectionDTO = new CourseSectionDTO("");

        mockMvc.perform(post("/courses/1/sections")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(sectionDTO))
                        .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_teacher"))))
                .andExpect(status().isBadRequest());
    }

    @Test
    void addSection_LessonWithBlankTitle_ThrowsException() throws Exception {
        Mockito.doThrow(new InputInvalidException("Section with the same title already exists."))
                .when(courseService).addSection(any(Long.class), any(CourseSectionDTO.class));

        CourseSectionDTO sectionDTO = new CourseSectionDTO("Billie Jean [4K] 30th Anniversary, 2001");

        mockMvc.perform(post("/courses/1/sections")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(sectionDTO))
                        .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_teacher"))))
                .andExpect(status().isBadRequest());
    }

    @Test
    void addSection_UserNotTeacher_ShouldReturnForbidden() throws Exception {

        mockMvc.perform(post("/courses/1/sections")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(null))
                        .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_user")))
                )
                .andExpect(status().isForbidden());
    }


    @Test
    void updateSectionInfo_ValidCourseIdAndSectionIdAndTitle_UpdatesSection() throws Exception {
        doNothing().when(courseService).updateSectionInfo(any(Long.class), any(Long.class), any(String.class));

        UpdateSectionDTO updateSectionDTO = new UpdateSectionDTO("NewTitle");

        mockMvc.perform(put("/courses/1/sections/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateSectionDTO))
                        .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_teacher"))))
                .andExpect(status().isOk());
    }

    @Test
    void updateSectionInfo_CourseNotFound_ThrowsException() throws Exception {
        Mockito.doThrow(new ResourceNotFoundException()).when(courseService).updateSectionInfo(any(Long.class), any(Long.class), any(String.class));

        UpdateSectionDTO updateSectionDTO = new UpdateSectionDTO("NewTitle");

        mockMvc.perform(put("/courses/1/sections/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateSectionDTO))
                        .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_teacher"))))
                .andExpect(status().isNotFound());
    }

    @Test
    void updateSectionInfo_SectionNotFound_ThrowsException() throws Exception {
        Mockito.doThrow(new ResourceNotFoundException()).when(courseService).updateSectionInfo(any(Long.class), any(Long.class), any(String.class));

        UpdateSectionDTO updateSectionDTO = new UpdateSectionDTO("NewTitle");

        mockMvc.perform(put("/courses/1/sections/999")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateSectionDTO))
                        .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_teacher"))))
                .andExpect(status().isNotFound());
    }

    @Test
    void updateSectionInfo_BlankTitle_ThrowsException() throws Exception {
        UpdateSectionDTO updateSectionDTO = new UpdateSectionDTO("");

        mockMvc.perform(put("/courses/1/sections/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateSectionDTO))
                        .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_teacher"))))
                .andExpect(status().isBadRequest());
    }

    @Test
    void updateSectionInfo_UserNotTeacher_ShouldReturnForbidden() throws Exception {
        UpdateSectionDTO updateSectionDTO = new UpdateSectionDTO("NewTitle");

        mockMvc.perform(put("/courses/1/sections/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateSectionDTO))
                        .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_user"))))
                .andExpect(status().isForbidden());
    }

    @Test
    void deleteSection_ValidCourseIdAndSectionId_RemovesSection() throws Exception {
        doNothing().when(courseService).removeSection(1L, 2L);

        mockMvc.perform(delete("/courses/1/sections/2")
                        .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_teacher"))))
                .andExpect(status().isNoContent());
    }

    @Test
    void deleteSection_CourseNotFound_ThrowsException() throws Exception {
        Mockito.doThrow(new ResourceNotFoundException()).when(courseService).removeSection(1L, 2L);

        mockMvc.perform(delete("/courses/1/sections/2")
                        .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_admin"))))
                .andExpect(status().isNotFound());
    }

    @Test
    void deleteSection_UserNotTeacher_ShouldReturnForbidden() throws Exception {
        mockMvc.perform(delete("/courses/1/sections/2")
                        .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_user"))))
                .andExpect(status().isForbidden());
    }

    @Test
    void deleteSection_PublishedCourse_ThrowsException() throws Exception {
        Mockito.doThrow(new InputInvalidException("Cannot delete section from a published course.")).when(courseService).removeSection(1L, 2L);

        mockMvc.perform(delete("/courses/1/sections/2")
                        .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_teacher"))))
                .andExpect(status().isBadRequest());
    }

    @Test
    void addLesson_ValidCourseIdAndSectionId_AddsLesson() throws Exception {
        LessonDTO lessonDTO = new LessonDTO("LessonTitle", Lesson.Type.TEXT, "https://example.com");
        when(courseService.addLesson(any(), any(), any()))
                .thenReturn(1L);

        mockMvc.perform(post("/courses/1/sections/2/lessons")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(lessonDTO))
                        .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_teacher"))))
                .andExpect(status().isOk())
                .andExpect(content().string("1"));
    }

    @Test
    void addLesson_CourseNotFound_ThrowsException() throws Exception {
        LessonDTO lessonDTO = new LessonDTO("LessonTitle", Lesson.Type.TEXT, "https://example.com");

        Mockito.doThrow(new ResourceNotFoundException()).when(courseService).addLesson(any(), any(), any());

        mockMvc.perform(post("/courses/1/sections/2/lessons")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(lessonDTO))
                        .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_teacher"))))
                .andExpect(status().isNotFound());
    }

    @Test
    void addLesson_SectionNotFound_ThrowsException() throws Exception {
        LessonDTO lessonDTO = new LessonDTO("LessonTitle", Lesson.Type.TEXT, "https://example.com");
        Mockito.doThrow(new ResourceNotFoundException()).when(courseService).addLesson(any(), any(), any());

        mockMvc.perform(post("/courses/1/sections/999/lessons")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(lessonDTO))
                        .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_teacher"))))
                .andExpect(status().isNotFound());
    }

    @Test
    void addLesson_UserNotTeacher_ShouldReturnForbidden() throws Exception {
        LessonDTO lessonDTO = new LessonDTO("LessonTitle", Lesson.Type.TEXT, "https://example.com");

        mockMvc.perform(post("/courses/1/sections/2/lessons")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(lessonDTO))
                        .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_user"))))
                .andExpect(status().isForbidden());
    }

    @Test
    void addLesson_PublishedCourse_ThrowsException() throws Exception {
        Mockito.doThrow(new InputInvalidException("Cannot add lesson to a published course."))
                .when(courseService).addLesson(any(), any(), any());

        LessonDTO lessonDTO = new LessonDTO("LessonTitle", Lesson.Type.TEXT, "https://example.com");

        mockMvc.perform(post("/courses/1/sections/2/lessons")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(lessonDTO))
                        .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_teacher"))))
                .andExpect(status().isBadRequest());
    }

    @Test
    void updateLesson_ValidCourseIdAndSectionIdAndLessonId_UpdatesLesson() throws Exception {
        LessonDTO lessonDTO = new LessonDTO("UpdatedLessonTitle", Lesson.Type.TEXT, "https://example.com/updated");
        doNothing().when(courseService).updateLesson(1L, 2L, 3L, lessonDTO.toLesson());

        mockMvc.perform(put("/courses/1/sections/2/lessons/3")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(lessonDTO))
                        .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_teacher"))))
                .andExpect(status().isOk());
    }

    @Test
    void updateLesson_CourseNotFound_ThrowsException() throws Exception {
        LessonDTO lessonDTO = new LessonDTO("UpdatedLessonTitle", Lesson.Type.TEXT, "https://example.com/updated");
        Mockito.doThrow(new ResourceNotFoundException()).when(courseService).updateLesson(any(), any(), any(), any());

        mockMvc.perform(put("/courses/1/sections/2/lessons/3")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(lessonDTO))
                        .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_teacher"))))
                .andExpect(status().isNotFound());
    }

    @Test
    void updateLesson_SectionNotFound_ThrowsException() throws Exception {
        LessonDTO lessonDTO = new LessonDTO("UpdatedLessonTitle", Lesson.Type.TEXT, "https://example.com/updated");
        Mockito.doThrow(new ResourceNotFoundException()).when(courseService).updateLesson(any(), any(), any(), any());

        mockMvc.perform(put("/courses/1/sections/999/lessons/3")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(lessonDTO))
                        .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_teacher"))))
                .andExpect(status().isNotFound());
    }

    @Test
    void updateLesson_LessonNotFound_ThrowsException() throws Exception {
        LessonDTO lessonDTO = new LessonDTO("UpdatedLessonTitle", Lesson.Type.TEXT, "https://example.com/updated");
        Mockito.doThrow(new ResourceNotFoundException()).when(courseService).updateLesson(any(), any(), any(), any());

        mockMvc.perform(put("/courses/1/sections/2/lessons/999")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(lessonDTO))
                        .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_teacher"))))
                .andExpect(status().isNotFound());
    }

    @Test
    void updateLesson_UserNotTeacher_ShouldReturnForbidden() throws Exception {
        LessonDTO lessonDTO = new LessonDTO("UpdatedLessonTitle", Lesson.Type.TEXT, "https://example.com/updated");

        mockMvc.perform(put("/courses/1/sections/2/lessons/3")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(lessonDTO))
                        .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_user"))))
                .andExpect(status().isForbidden());
    }

    @Test
    void updateLesson_PublishedCourse_ThrowsException() throws Exception {
        LessonDTO lessonDTO = new LessonDTO("UpdatedLessonTitle", Lesson.Type.TEXT, "https://example.com/updated");
        Mockito.doThrow(new InputInvalidException("Cannot update lesson in a published course."))
                .when(courseService).updateLesson(any(), any(), any(), any());

        mockMvc.perform(put("/courses/1/sections/2/lessons/3")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(lessonDTO))
                        .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_teacher"))))
                .andExpect(status().isBadRequest());
    }

    @Test
    void deleteLesson_ValidCourseIdAndSectionIdAndLessonId_RemovesLesson() throws Exception {
        doNothing().when(courseService).removeLesson(1L, 2L, 3L);

        mockMvc.perform(delete("/courses/1/sections/2/lessons/3")
                        .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_teacher"))))
                .andExpect(status().isNoContent());
    }

    @Test
    void deleteLesson_ValidCourseIdAndSectionIdAndLessonId_RemovesLesson2() throws Exception {
        doNothing().when(courseService).removeLesson(1L, 2L, 3L);

        mockMvc.perform(delete("/courses/1/sections/2/lessons/3")
                        .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_admin"))))
                .andExpect(status().isNoContent());
    }

    @Test
    void deleteLesson_CourseNotFound_ThrowsException() throws Exception {
        Mockito.doThrow(new ResourceNotFoundException()).when(courseService).removeLesson(1L, 2L, 3L);

        mockMvc.perform(delete("/courses/1/sections/2/lessons/3")
                        .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_teacher"))))
                .andExpect(status().isNotFound());
    }

    @Test
    void deleteLesson_SectionNotFound_ThrowsException() throws Exception {
        Mockito.doThrow(new ResourceNotFoundException()).when(courseService).removeLesson(1L, 999L, 3L);

        mockMvc.perform(delete("/courses/1/sections/999/lessons/3")
                        .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_teacher"))))
                .andExpect(status().isNotFound());
    }

    @Test
    void deleteLesson_LessonNotFound_ThrowsException() throws Exception {
        Mockito.doThrow(new ResourceNotFoundException()).when(courseService).removeLesson(1L, 2L, 999L);

        mockMvc.perform(delete("/courses/1/sections/2/lessons/999")
                        .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_teacher"))))
                .andExpect(status().isNotFound());
    }

    @Test
    void deleteLesson_UserNotTeacher_ShouldReturnForbidden() throws Exception {
        mockMvc.perform(delete("/courses/1/sections/2/lessons/3")
                        .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_user"))))
                .andExpect(status().isForbidden());
    }

    @Test
    void deleteLesson_PublishedCourse_ThrowsException() throws Exception {
        Mockito.doThrow(new InputInvalidException("Cannot delete lesson from a published course."))
                .when(courseService).removeLesson(1L, 2L, 3L);

        mockMvc.perform(delete("/courses/1/sections/2/lessons/3")
                        .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_teacher"))))
                .andExpect(status().isBadRequest());
    }

}
