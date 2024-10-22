package com.el.course.application;

import com.el.TestFactory;
import com.el.common.Currencies;
import com.el.common.RolesBaseUtil;
import com.el.common.exception.AccessDeniedException;
import com.el.common.exception.InputInvalidException;
import com.el.common.exception.ResourceNotFoundException;
import com.el.course.application.dto.CourseDTO;
import com.el.course.application.dto.CourseSectionDTO;
import com.el.course.application.dto.CourseUpdateDTO;
import com.el.course.application.impl.CourseServiceImpl;
import com.el.course.domain.*;
import com.el.discount.application.DiscountService;
import org.javamoney.moneta.Money;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import javax.money.MonetaryAmount;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class CourseServiceTests {

    @Mock
    private CourseRepository courseRepository;

    @Mock
    private DiscountService discountService;

    @Mock
    private CourseQueryService courseQueryService;

    @Mock
    private RolesBaseUtil rolesBaseUtil;

    @InjectMocks
    private CourseServiceImpl courseService;

    private CourseDTO courseDTO;
    private CourseUpdateDTO courseUpdateDTO;
    private Course course;
    private Course courseForPublish;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        // Giả lập CourseDTO với dữ liệu mẫu
        courseDTO = TestFactory.createDefaultCourseDTO();

        courseUpdateDTO = TestFactory.createDefaultCourseUpdateDTO();

        course = TestFactory.createDefaultCourse();

        courseForPublish = new Course(
                courseDTO.title(),
                courseDTO.description(),
                courseDTO.thumbnailUrl(),
                courseDTO.benefits(),
                courseDTO.language(),
                courseDTO.prerequisites(),
                courseDTO.subtitles(),
                "teacher123"
        );
        courseForPublish.changePrice(Money.of(100, Currencies.VND));
        CourseSection section = new CourseSection("Billie Jean [4K] 30th Anniversary, 2001");
        section.addLesson(new Lesson("Lesson 1", Lesson.Type.TEXT, "https://example.com/lesson1", null));
        courseForPublish.addSection(section);
    }

    @Test
    void testCreateCourse_ShouldCreateAndSaveCourse() {
        // Arrange
        when(courseRepository.save(any(Course.class))).thenReturn(course);

        // Act
        courseService.createCourse("teacher123", courseDTO);

        // Verify
        verify(courseRepository, times(1)).save(any(Course.class));
    }

    @Test
    void testUpdateInfoCourse_ShouldUpdateCourseInfo() {
        // Mock
        when(courseQueryService.findCourseById(1L)).thenReturn(course);
        when(courseRepository.save(any(Course.class))).thenReturn(course);
        // Mock canEdit method
        when(rolesBaseUtil.isAdmin()).thenReturn(true);

        // Act
        courseService.updateCourse(1L, courseUpdateDTO);

        // Verify
        verify(courseQueryService, times(1)).findCourseById(1L);
        verify(courseRepository, times(1)).save(any(Course.class));
    }

    @Test
    void testUpdateInfoCourse_ShouldThrowException_WhenCourseNotFound() {
        // Mock
        when(courseQueryService.findCourseById(1L)).thenThrow(new ResourceNotFoundException());
        // Mock canEdit method
        when(rolesBaseUtil.isAdmin()).thenReturn(false);
        when(rolesBaseUtil.getCurrentSubjectFromJwt()).thenReturn("teacher123");

        assertThrows(ResourceNotFoundException.class, () -> {
            courseService.updateCourse(1L, courseUpdateDTO);
        });

        // Verify
        verify(courseRepository, never()).save(any(Course.class));
    }

    @Test
    void testUpdateInfoCourse_ShouldThrowException_WhenNotPermission() {
        // Mock
        when(courseQueryService.findCourseById(1L)).thenReturn(course);
        // Mock canEdit method
        when(rolesBaseUtil.isAdmin()).thenReturn(false);
        when(rolesBaseUtil.getCurrentSubjectFromJwt()).thenReturn("otherTeacher"); // course's is teacher123

        assertThrows(AccessDeniedException.class, () -> {
            courseService.updateCourse(1L, courseUpdateDTO);
        });

        // Verify
        verify(courseRepository, never()).save(any(Course.class));
    }

    @Test
    void testUpdateInfoCourse_ShouldThrowException_WhenCoursePublished() {
        // Mock
        Course courseMock = spy(course);
        when(courseQueryService.findCourseById(1L)).thenReturn(courseMock);
        doReturn(false).when(courseMock).canEdit();
        // Mock canEdit method
        when(rolesBaseUtil.isAdmin()).thenReturn(true);

        // Assert
        assertThrows(InputInvalidException.class, () -> {
            courseService.updateCourse(1L, courseUpdateDTO);
        });

        // Verify
        verify(courseRepository, never()).save(any(Course.class));
    }

    @Test
    void deleteCourse_ValidCourseId_DeletesCourse() {
        when(courseQueryService.findCourseById(1L)).thenReturn(course);
        // Mock canEdit method
        when(rolesBaseUtil.isAdmin()).thenReturn(true);

        courseService.deleteCourse(1L);
        verify(courseRepository, times(1)).save(course);
        assertTrue(course.isDeleted());
    }

    @Test
    void deleteCourse_CourseNotFound_ThrowsException() {
        when(courseQueryService.findCourseById(1L)).thenThrow(new ResourceNotFoundException());
        // Mock canEdit method
        when(rolesBaseUtil.isAdmin()).thenReturn(true);

        assertThrows(ResourceNotFoundException.class, () -> courseService.deleteCourse(1L));
        verify(courseRepository, never()).save(any(Course.class));
    }

    @Test
    void deleteCourse_AlreadyDeletedCourse_ThrowsException() {
        course.delete();
        when(courseQueryService.findCourseById(1L)).thenReturn(course);
        // Mock canEdit method
        when(rolesBaseUtil.isAdmin()).thenReturn(true);

        assertThrows(InputInvalidException.class, () -> courseService.deleteCourse(1L));
        verify(courseRepository, never()).save(any(Course.class));
    }

    @Test
    void deleteCourse_PublishedCourse_ThrowsException() {
        Course courseMock = spy(course);
        when(courseQueryService.findCourseById(1L)).thenReturn(courseMock);
        // Mock canEdit method
        when(rolesBaseUtil.isAdmin()).thenReturn(true);
        doReturn(false).when(courseMock).canEdit();

        assertThrows(InputInvalidException.class, () -> courseService.deleteCourse(1L));
        verify(courseRepository, never()).save(any(Course.class));
    }

    @Test
    void deleteCourse_ThrowException_WhenNotPermission() {
        when(courseQueryService.findCourseById(1L)).thenReturn(course);
        // Mock canEdit method
        when(rolesBaseUtil.isAdmin()).thenReturn(false);
        when(rolesBaseUtil.getCurrentSubjectFromJwt()).thenReturn("otherTeacher");

        assertThrows(AccessDeniedException.class, () -> courseService.deleteCourse(1L));
        verify(courseRepository, never()).save(any(Course.class));
    }

    @Test
    void updatePrice_ValidCourseIdAndPrice_UpdatesPrice() {
        when(courseQueryService.findCourseById(1L)).thenReturn(course);

        MonetaryAmount newPrice = Money.of(100, Currencies.VND);
        courseService.updatePrice(1L, newPrice);
        verify(courseRepository, times(1)).save(course);
        assertEquals(newPrice, course.getPrice());
    }

    @Test
    void updatePrice_CourseNotFound_ThrowsException() {
        when(courseQueryService.findCourseById(1L)).thenThrow(new ResourceNotFoundException());

        MonetaryAmount newPrice = Money.of(100, Currencies.VND);
        assertThrows(ResourceNotFoundException.class, () -> courseService.updatePrice(1L, newPrice));
        verify(courseRepository, never()).save(any(Course.class));
    }

    @Test
    void updatePrice_NegativePrice_ThrowsException() {
        when(courseQueryService.findCourseById(1L)).thenReturn(course);

        MonetaryAmount negativePrice = Money.of(-100, Currencies.VND);
        assertThrows(InputInvalidException.class, () -> courseService.updatePrice(1L, negativePrice));
        verify(courseRepository, never()).save(any(Course.class));
    }

    @Test
    void updatePrice_CoursePublished_ThrowsException() {
        Course courseMock = spy(course);
        when(courseQueryService.findCourseById(1L)).thenReturn(courseMock);
        doReturn(false).when(courseMock).canEdit();

        MonetaryAmount newPrice = Money.of(100, Currencies.VND);
        assertThrows(InputInvalidException.class, () -> courseService.updatePrice(1L, newPrice));
        verify(courseRepository, never()).save(any(Course.class));
    }

    @Test
    void assignTeacher_ValidCourseIdAndTeacher_AssignsTeacher() {
        when(courseQueryService.findCourseById(1L)).thenReturn(course);

        courseService.assignTeacher(1L, "NewTeacher");
        verify(courseRepository, times(1)).save(course);
        assertEquals("NewTeacher", course.getTeacher());
    }

    @Test
    void assignTeacher_CourseNotFound_ThrowsException() {
        when(courseQueryService.findCourseById(1L)).thenThrow(new ResourceNotFoundException());

        assertThrows(ResourceNotFoundException.class, () -> courseService.assignTeacher(1L, "NewTeacher"));
        verify(courseRepository, never()).save(any(Course.class));
    }

    @Test
    void assignTeacher_NullTeacher_ThrowsException() {
        when(courseQueryService.findCourseById(1L)).thenReturn(course);

        assertThrows(InputInvalidException.class, () -> courseService.assignTeacher(1L, null));
        verify(courseRepository, never()).save(any(Course.class));
    }

    @Test
    void assignTeacher_PublishedCourse_ThrowsException() {
        Course courseMock = spy(course);
        when(courseQueryService.findCourseById(1L)).thenReturn(courseMock);

        doReturn(false).when(courseMock).canEdit();

        assertThrows(InputInvalidException.class, () -> courseService.assignTeacher(1L, "NewTeacher"));
        verify(courseRepository, never()).save(any(Course.class));
    }

    @Test
    void publishCourse_ValidCourseIdAndApprovedBy_PublishesCourse() {
        when(courseQueryService.findCourseById(1L)).thenReturn(courseForPublish);

        courseService.publishCourse(1L, "Admin");

        verify(courseRepository, times(1)).save(courseForPublish);
    }

    @Test
    void publishCourse_CourseNotFound_ThrowsException() {
        when(courseQueryService.findCourseById(1L)).thenThrow(new ResourceNotFoundException());

        assertThrows(ResourceNotFoundException.class, () -> courseService.publishCourse(1L, "Admin"));

        verify(courseRepository, never()).save(any(Course.class));
    }

    @Test
    void applyDiscount_ShouldApplyDiscountSuccessfully() {
        // Mock và discountService
        when(courseQueryService.findCourseById(any())).thenReturn(course);
        course.changePrice(Money.of(100, Currencies.VND));

        MonetaryAmount discountedPrice = Money.of(80, Currencies.VND);
        when(discountService.calculateDiscount(any(), any())).thenReturn(discountedPrice);
        when(courseRepository.save(any(Course.class))).thenReturn(course);

        // Act
        courseService.applyDiscount(1L, "DISCOUNT_10");

        // Xác minh rằng courseRepository.findById, discountService.calculateDiscount và courseRepository.save đã được gọi
        verify(courseQueryService, times(1)).findCourseById(any());
        verify(discountService, times(1)).calculateDiscount(any(), any());
        verify(courseRepository, times(1)).save(course);
    }

    @Test
    void applyDiscount_ShouldThrowException_WhenCoursePriceIsNull() {
        when(courseQueryService.findCourseById(1L)).thenReturn(course);
        String discountCode = "DISCOUNT_10";

        // Kiểm tra xem ngoại lệ có được ném ra khi giá của khóa học là null
        assertThrows(InputInvalidException.class, () -> {
            courseService.applyDiscount(1L, discountCode);
        });

        // Verify
        verify(discountService, never()).calculateDiscount(anyString(), any(MonetaryAmount.class));
        verify(courseRepository, never()).save(any(Course.class));
    }

    @Test
    void applyDiscount_ShouldThrowException_WhenDiscountNotFound() {
        when(courseQueryService.findCourseById(1L)).thenReturn(course);

        course.changePrice(Money.of(100, Currencies.VND));
        String discountCode = "DISCOUNT_10";
        when(discountService.calculateDiscount(discountCode, course.getPrice())).thenThrow(ResourceNotFoundException.class);

        // Kiểm tra xem ngoại lệ có được ném ra khi không tìm thấy mã giảm giá
        assertThrows(ResourceNotFoundException.class, () -> {
            courseService.applyDiscount(1L, discountCode);
        });

        // Verify
        verify(courseRepository, never()).save(any(Course.class));
    }

    @Test
    void addCourseSection_ShouldAddCourseSection() {
        when(courseQueryService.findCourseById(1L)).thenReturn(course);
        // Mock canEdit method
        when(rolesBaseUtil.isAdmin()).thenReturn(true);

        when(courseRepository.save(any(Course.class))).thenAnswer(invocation -> invocation.getArgument(0));
        CourseSectionDTO courseSectionDTO = new CourseSectionDTO("Billie Jean [4K] 30th Anniversary, 2001");

        // Act
        Course updatedCourse = courseService.addSection(1L, courseSectionDTO);

        // Verify
        verify(courseQueryService, times(1)).findCourseById(1L);
        verify(courseRepository, times(1)).save(course);

        // Kiểm tra các giá trị trả về
        assertNotNull(updatedCourse);
        assertEquals(1, updatedCourse.getSections().size());
        assertEquals(courseSectionDTO.title(), updatedCourse.getSections().iterator().next().getTitle());
    }

    @Test
    void addCourseSection_ShouldThrowException_WhenCourseNotFound() {
        when(courseQueryService.findCourseById(1L)).thenThrow(new ResourceNotFoundException());
        // Mock canEdit method
        when(rolesBaseUtil.isAdmin()).thenReturn(true);

        CourseSectionDTO courseSectionDTO = new CourseSectionDTO("Billie Jean [4K] 30th Anniversary, 2001");

        // Kiểm tra xem ngoại lệ có được ném ra khi không tìm thấy khóa học
        assertThrows(ResourceNotFoundException.class, () -> {
            courseService.addSection(1L, courseSectionDTO);
        });

        // Verify
        verify(courseRepository, never()).save(any(Course.class));
    }

    @Test
    void addCourseSection_ShouldThrowException_WhenNotPermission() {
        when(courseQueryService.findCourseById(1L)).thenReturn(course);
        // Mock canEdit method
        when(rolesBaseUtil.isAdmin()).thenReturn(false);
        when(rolesBaseUtil.getCurrentSubjectFromJwt()).thenReturn("otherTeacher");

        CourseSectionDTO courseSectionDTO = new CourseSectionDTO("Billie Jean [4K] 30th Anniversary, 2001");

        // Kiểm tra xem ngoại lệ có được ném ra khi không có quyền thêm section
        assertThrows(AccessDeniedException.class, () -> {
            courseService.addSection(1L, courseSectionDTO);
        });

        // Verify
        verify(courseRepository, never()).save(any(Course.class));
    }

    @Test
    void updateSectionInfo_ShouldUpdateSectionInfo() {
        // Arrange: Mock và course
        Course course = spy(this.course);
        when(courseQueryService.findCourseById(1L)).thenReturn(course);
        // Mock canEdit method
        when(rolesBaseUtil.isAdmin()).thenReturn(true);

        when(courseRepository.save(any(Course.class))).thenAnswer(invocation -> invocation.getArgument(0));
        doNothing().when(course).updateSection(2L, "New Section 1");

        // Act: Act
        courseService.updateSectionInfo(1L, 2L, "New Section 1");

        // Assert: Xác minh rằng các phương thức cần thiết đã được gọi đúng
        verify(courseQueryService, times(1)).findCourseById(1L); // Xác minh lấy course
        verify(course, times(1)).updateSection(2L, "New Section 1"); // Xác minh cập nhật thông tin section
        verify(courseRepository, times(1)).save(course); // Xác minh lưu lại course
    }

    @Test
    void updateSectionInfo_ShouldThrowException_WhenCourseNotFound() {
        when(courseQueryService.findCourseById(1L)).thenThrow(new ResourceNotFoundException());
        // Mock canEdit method
        when(rolesBaseUtil.isAdmin()).thenReturn(true);

        // Act & Assert: Kiểm tra xem ngoại lệ có được ném ra khi không tìm thấy khóa học
        assertThrows(ResourceNotFoundException.class, () -> {
            courseService.updateSectionInfo(1L, 2L, "New Section 1");
        });

        // Assert: Verify
        verify(courseRepository, never()).save(any(Course.class));
    }

    @Test
    void updateSectionInfo_CoursePublish_ThrowsException() {
        // Arrange: Mock
        Course courseMock = spy(course);
        when(courseQueryService.findCourseById(1L)).thenReturn(courseMock);
        // Mock canEdit method
        when(rolesBaseUtil.isAdmin()).thenReturn(true);
        doReturn(false).when(courseMock).canEdit();

        // Act & Assert: Assert
        assertThrows(InputInvalidException.class, () -> {
            courseService.updateSectionInfo(1L, 2L, "New Section 1");
        });

        // Assert: Verify
        verify(courseRepository, never()).save(any(Course.class));
    }

    @Test
    void updateSectionInfo_ShouldThrowException_WhenNotPermission() {
        when(courseQueryService.findCourseById(1L)).thenReturn(course);
        // Mock canEdit method
        when(rolesBaseUtil.isAdmin()).thenReturn(false);
        when(rolesBaseUtil.getCurrentSubjectFromJwt()).thenReturn("otherTeacher");

        // Act & Assert: Kiểm tra xem ngoại lệ có được ném ra khi không có quyền cập nhật section
        assertThrows(AccessDeniedException.class, () -> {
            courseService.updateSectionInfo(1L, 2L, "New Section 1");
        });

        // Assert: Verify
        verify(courseRepository, never()).save(any(Course.class));
    }

    @Test
    void removeSection_ValidCourseIdAndSectionId_RemovesSection() {
        Course course = spy(this.course);
        when(courseQueryService.findCourseById(1L)).thenReturn(course);
        // Mock canEdit method
        when(rolesBaseUtil.isAdmin()).thenReturn(true);
        doNothing().when(course).removeSection(2L);

        courseService.removeSection(1L, 2L);

        verify(courseQueryService, times(1)).findCourseById(1L);
        verify(course, times(1)).removeSection(2L);
        verify(courseRepository, times(1)).save(course);
    }

    @Test
    void removeSection_CourseNotFound_ThrowsException() {
        when(courseQueryService.findCourseById(1L)).thenThrow(new ResourceNotFoundException());
        // Mock canEdit method
        when(rolesBaseUtil.isAdmin()).thenReturn(true);

        assertThrows(ResourceNotFoundException.class, () -> courseService.removeSection(1L, 2L));

        verify(courseRepository, never()).save(any(Course.class));
    }

    @Test
    void removeSection_SectionNotFound_ThrowsException() {
        Course course = spy(this.course);
        when(courseQueryService.findCourseById(1L)).thenReturn(course);
        // Mock canEdit method
        when(rolesBaseUtil.isAdmin()).thenReturn(true);
        doThrow(new ResourceNotFoundException()).when(course).removeSection(999L);

        assertThrows(ResourceNotFoundException.class, () -> courseService.removeSection(1L, 999L));

        verify(courseRepository, never()).save(any(Course.class));
    }

    @Test
    void removeSection_PublishedCourse_ThrowsException() {
        Course course = spy(this.course);
        when(courseQueryService.findCourseById(1L)).thenReturn(course);
        // Mock canEdit method
        when(rolesBaseUtil.isAdmin()).thenReturn(true);
        doReturn(false).when(course).canEdit();

        assertThrows(InputInvalidException.class, () -> courseService.removeSection(1L, 2L));

        verify(courseRepository, never()).save(any(Course.class));
    }

    @Test
    void removeSection_ShouldThrowException_WhenNotPermission() {
        when(courseQueryService.findCourseById(1L)).thenReturn(course);
        // Mock canEdit method
        when(rolesBaseUtil.isAdmin()).thenReturn(false);
        when(rolesBaseUtil.getCurrentSubjectFromJwt()).thenReturn("otherTeacher");

        assertThrows(AccessDeniedException.class, () -> courseService.removeSection(1L, 2L));

        verify(courseRepository, never()).save(any(Course.class));
    }

    @Test
    void addLesson_ValidCourseIdAndSectionId_AddsLesson() {
        Lesson lesson = new Lesson("LessonTitle", Lesson.Type.TEXT, "https://www.example.com", null);
        Course course = spy(this.course);
        when(courseQueryService.findCourseById(1L)).thenReturn(course);
        // Mock canEdit method
        when(rolesBaseUtil.isAdmin()).thenReturn(true);
        doNothing().when(course).addLessonToSection(2L, lesson);

        courseService.addLesson(1L, 2L, lesson);

        verify(courseQueryService, times(1)).findCourseById(1L);
        verify(course, times(1)).addLessonToSection(2L, lesson);
        verify(courseRepository, times(1)).save(course);
    }

    @Test
    void addLesson_CourseNotFound_ThrowsException() {
        when(courseQueryService.findCourseById(1L)).thenThrow(new ResourceNotFoundException());
        // Mock canEdit method
        when(rolesBaseUtil.isAdmin()).thenReturn(true);
        Lesson lesson = new Lesson("LessonTitle", Lesson.Type.TEXT, "https://www.example.com", null);

        assertThrows(ResourceNotFoundException.class, () -> courseService.addLesson(1L, 2L, lesson));

        verify(courseRepository, never()).save(any(Course.class));
    }

    @Test
    void addLesson_SectionNotFound_ThrowsException() {
        Course course = spy(this.course);
        when(courseQueryService.findCourseById(1L)).thenReturn(course);
        // Mock canEdit method
        when(rolesBaseUtil.isAdmin()).thenReturn(true);
        doThrow(new ResourceNotFoundException()).when(course).addLessonToSection(any(Long.class), any(Lesson.class));
        Lesson lesson = new Lesson("LessonTitle", Lesson.Type.TEXT, "https://www.example.com", null);

        assertThrows(ResourceNotFoundException.class, () -> courseService.addLesson(1L, 999L, lesson));

        verify(courseRepository, never()).save(any(Course.class));
    }

    @Test
    void addLesson_PublishedCourse_ThrowsException() {
        Course course = spy(this.course);
        when(courseQueryService.findCourseById(1L)).thenReturn(course);
        // Mock canEdit method
        when(rolesBaseUtil.isAdmin()).thenReturn(true);
        doReturn(false).when(course).canEdit();
        Lesson lesson = new Lesson("LessonTitle", Lesson.Type.TEXT, "https://www.example.com", null);

        assertThrows(InputInvalidException.class, () -> courseService.addLesson(1L, 2L, lesson));

        verify(courseRepository, never()).save(any(Course.class));
    }

    @Test
    void addLesson_ShouldThrowException_WhenNotPermission() {
        when(courseQueryService.findCourseById(1L)).thenReturn(course);
        // Mock canEdit method
        when(rolesBaseUtil.isAdmin()).thenReturn(false);
        when(rolesBaseUtil.getCurrentSubjectFromJwt()).thenReturn("otherTeacher");
        Lesson lesson = new Lesson("LessonTitle", Lesson.Type.TEXT, "https://www.example.com", null);

        assertThrows(AccessDeniedException.class, () -> courseService.addLesson(1L, 2L, lesson));

        verify(courseRepository, never()).save(any(Course.class));
    }

    @Test
    void updateLesson_ValidCourseIdAndSectionIdAndLessonId_UpdatesLesson() {
        Course course = spy(this.course);
        Lesson updatedLesson = new Lesson("UpdatedLessonTitle", Lesson.Type.TEXT, "https://www.example.com/updated", null);
        when(courseQueryService.findCourseById(1L)).thenReturn(course);
        // Mock canEdit method
        when(rolesBaseUtil.isAdmin()).thenReturn(true);
        doNothing().when(course).updateLessonInSection(2L, 3L, updatedLesson);

        courseService.updateLesson(1L, 2L, 3L, updatedLesson);

        verify(courseQueryService, times(1)).findCourseById(1L);
        verify(course, times(1)).updateLessonInSection(2L, 3L, updatedLesson);
        verify(courseRepository, times(1)).save(course);
    }

    @Test
    void updateLesson_CourseNotFound_ThrowsException() {
        Lesson updatedLesson = new Lesson("UpdatedLessonTitle", Lesson.Type.TEXT, "https://www.example.com/updated", null);
        when(courseQueryService.findCourseById(1L)).thenThrow(new ResourceNotFoundException());
        // Mock canEdit method
        when(rolesBaseUtil.isAdmin()).thenReturn(true);

        assertThrows(ResourceNotFoundException.class, () -> courseService.updateLesson(1L, 2L, 3L, updatedLesson));

        verify(courseRepository, never()).save(any(Course.class));
    }

    @Test
    void updateLesson_SectionNotFound_ThrowsException() {
        Course course = spy(this.course);
        Lesson updatedLesson = new Lesson("UpdatedLessonTitle", Lesson.Type.TEXT, "https://www.example.com/updated", null);
        when(courseQueryService.findCourseById(1L)).thenReturn(course);
        // Mock canEdit method
        when(rolesBaseUtil.isAdmin()).thenReturn(true);
        doThrow(new ResourceNotFoundException()).when(course).updateLessonInSection(999L, 3L, updatedLesson);

        assertThrows(ResourceNotFoundException.class, () -> courseService.updateLesson(1L, 999L, 3L, updatedLesson));

        verify(courseRepository, never()).save(any(Course.class));
    }

    @Test
    void updateLesson_ThrowException_WhenNotPermission() {
        when(courseQueryService.findCourseById(1L)).thenReturn(course);
        // Mock canEdit method
        when(rolesBaseUtil.isAdmin()).thenReturn(false);
        when(rolesBaseUtil.getCurrentSubjectFromJwt()).thenReturn("otherTeacher");
        Lesson updatedLesson = new Lesson("UpdatedLessonTitle", Lesson.Type.TEXT, "https://www.example.com/updated", null);

        assertThrows(AccessDeniedException.class, () -> courseService.updateLesson(1L, 2L, 3L, updatedLesson));

        verify(courseRepository, never()).save(any(Course.class));
    }

    @Test
    void removeLesson_ValidCourseIdAndSectionIdAndLessonId_RemovesLesson() {
        Course course = spy(this.course);
        when(courseQueryService.findCourseById(1L)).thenReturn(course);
        // Mock canEdit method
        when(rolesBaseUtil.isAdmin()).thenReturn(true);
        doNothing().when(course).removeLessonFromSection(2L, 3L);

        courseService.removeLesson(1L, 2L, 3L);

        verify(courseQueryService, times(1)).findCourseById(1L);
        verify(course, times(1)).removeLessonFromSection(2L, 3L);
        verify(courseRepository, times(1)).save(course);
    }

    @Test
    void removeLesson_CourseNotFound_ThrowsException() {
        when(courseQueryService.findCourseById(1L)).thenThrow(new ResourceNotFoundException());
        // Mock canEdit method
        when(rolesBaseUtil.isAdmin()).thenReturn(true);

        assertThrows(ResourceNotFoundException.class, () -> courseService.removeLesson(1L, 2L, 3L));

        verify(courseRepository, never()).save(any(Course.class));
    }

    @Test
    void removeLesson_SectionNotFound_ThrowsException() {
        Course course = spy(this.course);
        when(courseQueryService.findCourseById(1L)).thenReturn(course);
        // Mock canEdit method
        when(rolesBaseUtil.isAdmin()).thenReturn(true);
        doThrow(new ResourceNotFoundException()).when(course).removeLessonFromSection(999L, 3L);

        assertThrows(ResourceNotFoundException.class, () -> courseService.removeLesson(1L, 999L, 3L));

        verify(courseRepository, never()).save(any(Course.class));
    }

    @Test
    void removeLesson_LessonNotFound_ThrowsException() {
        Course course = spy(this.course);
        when(courseQueryService.findCourseById(1L)).thenReturn(course);
        // Mock canEdit method
        when(rolesBaseUtil.isAdmin()).thenReturn(true);
        doThrow(new ResourceNotFoundException()).when(course).removeLessonFromSection(2L, 999L);

        assertThrows(ResourceNotFoundException.class, () -> courseService.removeLesson(1L, 2L, 999L));

        verify(courseRepository, never()).save(any(Course.class));
    }

    @Test
    void removeLesson_PublishedCourse_ThrowsException() {
        Course course = spy(this.course);
        when(courseQueryService.findCourseById(1L)).thenReturn(course);
        // Mock canEdit method
        when(rolesBaseUtil.isAdmin()).thenReturn(true);
        doReturn(false).when(course).canEdit();

        assertThrows(InputInvalidException.class, () -> courseService.removeLesson(1L, 2L, 3L));

        verify(courseRepository, never()).save(any(Course.class));
    }

}
