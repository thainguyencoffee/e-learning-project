package com.elearning.course.web;

import com.elearning.common.config.JacksonCustomizations;
import com.elearning.common.config.SecurityConfig;
import com.elearning.common.exception.InputInvalidException;
import com.elearning.common.exception.ResourceNotFoundException;
import com.elearning.course.application.dto.CourseDTO;
import com.elearning.course.application.dto.CourseSectionDTO;
import com.elearning.course.application.dto.CourseUpdateDTO;
import com.elearning.course.application.dto.LessonDTO;
import com.elearning.course.application.impl.CourseServiceImpl;
import com.elearning.course.domain.Course;
import com.elearning.course.domain.Language;
import com.elearning.course.domain.Lesson;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.javamoney.moneta.Money;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Set;

import static org.mockito.ArgumentMatchers.any;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(CourseController.class)
@Import({SecurityConfig.class, JacksonCustomizations.class})
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

    @Test
    void testUpdateInfoCourse_ShouldReturnOKStatus() throws Exception {
        // Giả lập khóa học được cập nhật
        Course updatedCourse = Mockito.mock(Course.class);
        Mockito.when(updatedCourse.getId()).thenReturn(1L);  // Giả lập ID của khóa học
        Mockito.when(updatedCourse.getTitle()).thenReturn("Java Programming");  // Giả lập tiêu đề của khóa học
        Mockito.when(updatedCourse.getDescription()).thenReturn("Learn Java from scratch");  // Giả lập mô tả của khóa học

        // Giả lập hành vi của UpdateCourseUseCase trả về đối tượng khóa học đã cập nhật
        Mockito.when(courseService.updateCourse(any(Long.class), any(CourseUpdateDTO.class)))
                .thenReturn(updatedCourse);

        // Thực thi HTTP PUT request với JWT
        mockMvc.perform(put("/courses/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(courseDTO))
                        .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_teacher")))
                )
                .andExpect(status().isOk())  // Kiểm tra phản hồi 200 OK
                .andExpect(jsonPath("$.title").value("Java Programming"))  // Kiểm tra giá trị trả về
                .andExpect(jsonPath("$.description").value("Learn Java from scratch"));
    }

    @Test
    void testUpdateInfoCourse_ShouldReturnBadRequest_WhenTitleIsBlank() throws Exception {
        // CourseUpdateDTO với tiêu đề trống
        CourseUpdateDTO invalidCourseUpdateDTO = new CourseUpdateDTO(
                "",
                "Learn Java from scratch",
                "http://example.com/image.jpg",
                Set.of("OOP", "Concurrency"),
                Set.of("Basic Programming Knowledge"),
                Set.of(Language.ENGLISH, Language.SPANISH)
        );

        // Thực thi HTTP PUT request với dữ liệu không hợp lệ
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
                .when(courseService).updateCourse(1L, new CourseUpdateDTO("Java Programming", "Learn Java from scratch", "http://example.com/image.jpg", Set.of("OOP", "Concurrency"), Set.of("Basic Programming Knowledge"), Set.of(Language.ENGLISH, Language.SPANISH)));

        // Thực thi HTTP PUT request với JWT
        mockMvc.perform(put("/courses/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(courseDTO))
                        .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_teacher")))
                )
                .andExpect(status().isBadRequest());  // Kiểm tra phản hồi 400 Bad Request
    }


    @Test
    void deleteCourse_ValidCourseId_ShouldReturnNoContent() throws Exception {
        // Chuẩn bị hành vi của deleteCourse
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
    void changePrice_ValidCourseIdAndPrice_ShouldReturnUpdatedCourse() throws Exception {
        Course updatedCourse = Mockito.mock(Course.class);
        Mockito.when(updatedCourse.getId()).thenReturn(1L);
        Mockito.when(courseService.updatePrice(1L, Money.of(100, "USD"))).thenReturn(updatedCourse);

        String body = objectMapper.writeValueAsString(new UpdatePriceDTO(Money.of(100, "USD")));

        mockMvc.perform(put("/courses/1/update-price")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body)
                        .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_admin"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L));
    }

    @Test
    void changePrice_CourseNotFound_ShouldReturnNotFound() throws Exception {
        Mockito.doThrow(new ResourceNotFoundException()).when(courseService).updatePrice(1L, Money.of(100, "USD"));

        String body = objectMapper.writeValueAsString(new UpdatePriceDTO(Money.of(100, "USD")));

        mockMvc.perform(put("/courses/1/update-price")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body)
                        .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_admin"))))
                .andExpect(status().isNotFound());
    }

    @Test
    void changePrice_UserNotAdmin_ShouldReturnForbidden() throws Exception {
        String body = objectMapper.writeValueAsString(new UpdatePriceDTO(Money.of(100, "USD")));
        mockMvc.perform(put("/courses/1/update-price")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body)
                        .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_teacher"))))
                .andExpect(status().isForbidden());
    }

    @Test
    void changePrice_CourseAlreadyPublished_ShouldReturnBadRequest() throws Exception {
        Mockito.doThrow(new InputInvalidException("Cannot change price of a published course.")).when(courseService).updatePrice(1L, Money.of(100, "USD"));

        String body = objectMapper.writeValueAsString(new UpdatePriceDTO(Money.of(100, "USD")));

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
        Mockito.when(updatedCourse.getId()).thenReturn(1L);
        Mockito.when(courseService.assignTeacher(1L, "NewTeacher")).thenReturn(updatedCourse);

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
    void applyDiscount_ValidCourseIdAndDiscount_ShouldReturnOk() throws Exception {
        Course updatedCourse = Mockito.mock(Course.class);
        Mockito.when(updatedCourse.getId()).thenReturn(1L);
        String discountCode = "DISCOUNT25";
        Mockito.when(courseService.applyDiscount(1L, discountCode)).thenReturn(updatedCourse);

        String body = objectMapper.writeValueAsString(new ApplyDiscountDTO(discountCode));

        mockMvc.perform(post("/courses/1/apply-discount")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body)
                        .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_admin"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L));
    }

    @Test
    void applyDiscount_CourseNotFound_ShouldReturnNotFound() throws Exception {
        String discountCode = "DISCOUNT25";
        Mockito.doThrow(new ResourceNotFoundException()).when(courseService).applyDiscount(1L, discountCode);

        String body = objectMapper.writeValueAsString(new ApplyDiscountDTO(discountCode));

        mockMvc.perform(post("/courses/1/apply-discount")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body)
                        .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_admin"))))
                .andExpect(status().isNotFound());
    }

    @Test
    void applyDiscount_UserNotAdmin_ShouldReturnForbidden() throws Exception {
        String discountCode = "DISCOUNT25";
        String body = objectMapper.writeValueAsString(new ApplyDiscountDTO(discountCode));

        mockMvc.perform(post("/courses/1/apply-discount")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body)
                        .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_teacher"))))
                .andExpect(status().isForbidden());
    }

    @Test
    void applyDiscount_InvalidDiscountId_ShouldReturnBadRequest() throws Exception {
        String body = objectMapper.writeValueAsString(new ApplyDiscountDTO(null));

        mockMvc.perform(post("/courses/1/apply-discount")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body)
                        .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_admin"))))
                .andExpect(status().isBadRequest());
    }

    @Test
    void addSection_ValidCourseIdAndSection_ReturnsUpdatedCourse() throws Exception {
        Course updatedCourse = Mockito.mock(Course.class);
        Mockito.when(courseService.addSection(any(Long.class), any(CourseSectionDTO.class)))
                .thenReturn(updatedCourse);

        CourseSectionDTO sectionDTO = new CourseSectionDTO("SectionTitle", Set.of(new LessonDTO("LessonTitle", Lesson.Type.TEXT, "https://example.com", null)));

        mockMvc.perform(post("/courses/1/sections")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(sectionDTO))
                        .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_teacher"))))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", "/courses/1"))
                .andExpect(jsonPath("$.id").value(updatedCourse.getId()));
    }

    @Test
    void addSection_CourseNotFound_ThrowsException() throws Exception {
        Mockito.doThrow(new ResourceNotFoundException()).when(courseService).addSection(any(Long.class), any(CourseSectionDTO.class));

        CourseSectionDTO sectionDTO = new CourseSectionDTO("SectionTitle", Set.of(new LessonDTO("LessonTitle", Lesson.Type.TEXT, "https://example.com", null)));

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

        CourseSectionDTO sectionDTO = new CourseSectionDTO("", Set.of(new LessonDTO("LessonTitle", Lesson.Type.TEXT, "https://example.com", null)));

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

        CourseSectionDTO sectionDTO = new CourseSectionDTO("Section", Set.of(new LessonDTO("", Lesson.Type.TEXT, "https://example.com", null)));

        mockMvc.perform(post("/courses/1/sections")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(sectionDTO))
                        .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_teacher"))))
                .andExpect(status().isBadRequest());
    }

    @Test
    void addSection_SectionWithoutLessons_ThrowsException() throws Exception {
        CourseSectionDTO sectionDTO = new CourseSectionDTO("SectionTitle", Set.of());

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

}
