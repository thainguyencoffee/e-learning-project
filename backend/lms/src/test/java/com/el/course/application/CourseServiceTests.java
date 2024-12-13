package com.el.course.application;

import com.el.TestFactory;
import com.el.common.Currencies;
import com.el.common.RolesBaseUtil;
import com.el.common.exception.AccessDeniedException;
import com.el.common.exception.InputInvalidException;
import com.el.common.exception.ResourceNotFoundException;
import com.el.course.application.impl.CourseServiceImpl;
import com.el.course.domain.*;
import com.el.course.web.dto.*;
import com.el.discount.application.DiscountService;
import org.javamoney.moneta.Money;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.el.course.web.dto.QuestionDTO.AnswerOptionDTO;

import javax.money.MonetaryAmount;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class CourseServiceTests {

    @Mock
    private CourseRepository courseRepository;

    @Mock
    private DiscountService discountService;

    @Mock
    private EnsureEnrollmentCompleted ensureEnrollmentCompleted;

    @Mock
    private CourseQueryService courseQueryService;

    @Mock
    private RolesBaseUtil rolesBaseUtil;

    @InjectMocks
    private CourseServiceImpl courseService;

    private CourseDTO courseDTO;
    private CourseUpdateDTO courseUpdateDTO;
    private Course course;
    private Course courseWithSections;
    private Course courseForPublish;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        // Giả lập CourseDTO với dữ liệu mẫu
        courseDTO = TestFactory.createDefaultCourseDTO();
        courseUpdateDTO = TestFactory.createDefaultCourseUpdateDTO();
        course = TestFactory.createDefaultCourse();
        courseWithSections = spy(TestFactory.createCourseWithSections());

        courseForPublish = spy(new Course(
                courseDTO.title(),
                courseDTO.description(),
                courseDTO.thumbnailUrl(),
                courseDTO.benefits(),
                courseDTO.language(),
                courseDTO.prerequisites(),
                courseDTO.subtitles(),
                "teacher123"
        ));

        CourseSection section = spy(new CourseSection("Billie Jean [4K] 30th Anniversary, 2001"));
        when(section.getId()).thenReturn(1L);
        courseForPublish.addSection(section);
        courseForPublish.addLessonToSection(1L, new Lesson("Lesson 1", Lesson.Type.TEXT, "https://example.com/lesson1"));

        // (mock) course must have quizzes for changePrice
        when(courseForPublish.isNoneQuizzes()).thenReturn(false);
        when(courseWithSections.isNoneQuizzes()).thenReturn(false);


        // update validate updatePrice, Course.validSections return true
        courseForPublish.changePrice(Money.of(20000, Currencies.VND));

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
        when(courseQueryService.findCourseById(1L, false)).thenReturn(course);
        when(courseRepository.save(any(Course.class))).thenReturn(course);
        // Mock canEdit method
        when(rolesBaseUtil.isAdmin()).thenReturn(true);

        // Act
        courseService.updateCourse(1L, courseUpdateDTO);

        // Verify
        verify(courseQueryService, times(1)).findCourseById(1L, false);
        verify(courseRepository, times(1)).save(any(Course.class));
    }

    @Test
    void testUpdateInfoCourse_ShouldThrowException_WhenCourseNotFound() {
        // Mock
        when(courseQueryService.findCourseById(1L, false)).thenThrow(new ResourceNotFoundException());
        // Mock canEdit method
        when(rolesBaseUtil.isAdmin()).thenReturn(false);
        when(rolesBaseUtil.getCurrentPreferredUsernameFromJwt()).thenReturn("teacher123");

        assertThrows(ResourceNotFoundException.class, () -> {
            courseService.updateCourse(1L, courseUpdateDTO);
        });

        // Verify
        verify(courseRepository, never()).save(any(Course.class));
    }

    @Test
    void testUpdateInfoCourse_ShouldThrowException_WhenNotPermission() {
        // Mock
        when(courseQueryService.findCourseById(1L, false)).thenReturn(course);
        // Mock canEdit method
        when(rolesBaseUtil.isAdmin()).thenReturn(false);
        when(rolesBaseUtil.getCurrentPreferredUsernameFromJwt()).thenReturn("otherTeacher"); // course's is teacher123

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
        when(courseQueryService.findCourseById(1L, false)).thenReturn(courseMock);
        doReturn(true).when(courseMock).isPublishedAndNotUnpublishedOrDelete();
//        doReturn(false).when(courseMock).isNotPublishedOrDeleted();
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
        when(courseQueryService.findCourseById(1L, false)).thenReturn(course);
        // Mock canEdit method
        when(rolesBaseUtil.isAdmin()).thenReturn(true);

        courseService.deleteCourse(1L);
        verify(courseRepository, times(1)).save(course);
        assertTrue(course.isDeleted());
    }

    @Test
    void deleteCourse_CourseNotFound_ThrowsException() {
        when(courseQueryService.findCourseById(1L, false)).thenThrow(new ResourceNotFoundException());
        // Mock canEdit method
        when(rolesBaseUtil.isAdmin()).thenReturn(true);

        assertThrows(ResourceNotFoundException.class, () -> courseService.deleteCourse(1L));
        verify(courseRepository, never()).save(any(Course.class));
    }

    @Test
    void deleteCourse_AlreadyDeletedCourse_ThrowsException() {
        course.delete();
        when(courseQueryService.findCourseById(1L, false)).thenReturn(course);
        // Mock canEdit method
        when(rolesBaseUtil.isAdmin()).thenReturn(true);

        assertThrows(InputInvalidException.class, () -> courseService.deleteCourse(1L));
        verify(courseRepository, never()).save(any(Course.class));
    }

    @Test
    void deleteCourse_PublishedCourse_ThrowsException() {
        Course courseMock = spy(course);
        when(courseQueryService.findCourseById(1L, false)).thenReturn(courseMock);
        // Mock canEdit method
        when(rolesBaseUtil.isAdmin()).thenReturn(true);
//        doReturn(false).when(courseMock).isNotPublishedOrDeleted();
        doReturn(true).when(courseMock).isPublishedAndNotUnpublishedOrDelete();

        assertThrows(InputInvalidException.class, () -> courseService.deleteCourse(1L));
        verify(courseRepository, never()).save(any(Course.class));
    }

    @Test
    void deleteCourse_ThrowException_WhenNotPermission() {
        when(courseQueryService.findCourseById(1L, false)).thenReturn(course);
        // Mock canEdit method
        when(rolesBaseUtil.isAdmin()).thenReturn(false);
        when(rolesBaseUtil.getCurrentPreferredUsernameFromJwt()).thenReturn("otherTeacher");

        assertThrows(AccessDeniedException.class, () -> courseService.deleteCourse(1L));
        verify(courseRepository, never()).save(any(Course.class));
    }

    @Test
    void updatePrice_ValidCourseIdAndPrice_UpdatesPrice() {
        when(courseQueryService.findCourseById(1L, false)).thenReturn(courseWithSections);

        MonetaryAmount newPrice = Money.of(100, Currencies.USD);
        courseService.updatePrice(1L, newPrice);
        verify(courseRepository, times(1)).save(courseWithSections);
        assertEquals(newPrice, courseWithSections.getPrice());
    }

    @Test
    void updatePrice_CourseNotFound_ThrowsException() {
        when(courseQueryService.findCourseById(1L, false)).thenThrow(new ResourceNotFoundException());

        MonetaryAmount newPrice = Money.of(100, Currencies.USD);
        assertThrows(ResourceNotFoundException.class, () -> courseService.updatePrice(1L, newPrice));
        verify(courseRepository, never()).save(any(Course.class));
    }

    @Test
    void updatePrice_NegativePrice_ThrowsException() {
        when(courseQueryService.findCourseById(1L, false)).thenReturn(course);

        MonetaryAmount negativePrice = Money.of(-100, Currencies.VND);
        assertThrows(InputInvalidException.class, () -> courseService.updatePrice(1L, negativePrice));
        verify(courseRepository, never()).save(any(Course.class));
    }

    @Test
    void updatePrice_CoursePublished_ThrowsException() {
        Course courseMock = spy(course);
        when(courseQueryService.findCourseById(1L, false)).thenReturn(courseMock);
        doReturn(false).when(courseMock).isNotPublishedOrDeleted();

        MonetaryAmount newPrice = Money.of(100, Currencies.VND);
        assertThrows(InputInvalidException.class, () -> courseService.updatePrice(1L, newPrice));
        verify(courseRepository, never()).save(any(Course.class));
    }

    @Test
    void assignTeacher_ValidCourseIdAndTeacher_AssignsTeacher() {
        when(rolesBaseUtil.isAdmin()).thenReturn(true);
        when(courseQueryService.findCourseById(1L, false)).thenReturn(course);

        courseService.assignTeacher(1L, "NewTeacher");
        verify(courseRepository, times(1)).save(course);
        assertEquals("NewTeacher", course.getTeacher());
    }

    @Test
    void assignTeacher_NotAdmin_ThrowsException() {
        when(rolesBaseUtil.isAdmin()).thenReturn(false);

        assertThrows(AccessDeniedException.class, () -> courseService.assignTeacher(1L, "NewTeacher"));
        verify(courseRepository, never()).save(any(Course.class));
    }

    @Test
    void assignTeacher_CourseNotFound_ThrowsException() {
        when(rolesBaseUtil.isAdmin()).thenReturn(true);
        when(courseQueryService.findCourseById(1L, false)).thenThrow(new ResourceNotFoundException());

        assertThrows(ResourceNotFoundException.class, () -> courseService.assignTeacher(1L, "NewTeacher"));
        verify(courseRepository, never()).save(any(Course.class));
    }

    @Test
    void assignTeacher_NullTeacher_ThrowsException() {
        when(courseQueryService.findCourseById(1L, false)).thenReturn(course);
        when(rolesBaseUtil.isAdmin()).thenReturn(true);

        assertThrows(InputInvalidException.class, () -> courseService.assignTeacher(1L, null));
        verify(courseRepository, never()).save(any(Course.class));
    }

    @Test
    void assignTeacher_PublishedCourse_ThrowsException() {
        Course courseMock = spy(course);
        when(courseQueryService.findCourseById(1L, false)).thenReturn(courseMock);
        when(rolesBaseUtil.isAdmin()).thenReturn(true);

        doReturn(true).when(courseMock).isPublishedAndNotUnpublishedOrDelete();

        assertThrows(InputInvalidException.class, () -> courseService.assignTeacher(1L, "NewTeacher"));
        verify(courseRepository, never()).save(any(Course.class));
    }


    @Test
    void addCourseSection_ShouldAddCourseSection() {
        when(courseQueryService.findCourseById(1L, false)).thenReturn(course);
        // Mock canEdit method
        when(rolesBaseUtil.isAdmin()).thenReturn(true);

        when(courseRepository.save(any(Course.class))).thenAnswer(invocation -> invocation.getArgument(0));
        CourseSectionDTO courseSectionDTO = new CourseSectionDTO("Billie Jean [4K] 30th Anniversary, 2001");

        // Act
        courseService.addSection(1L, courseSectionDTO);

        // Verify
        verify(courseQueryService, times(1)).findCourseById(1L, false);
        verify(courseRepository, times(1)).save(course);
    }

    @Test
    void addCourseSection_ShouldThrowException_WhenCourseNotFound() {
        when(courseQueryService.findCourseById(1L, false)).thenThrow(new ResourceNotFoundException());
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
        when(courseQueryService.findCourseById(1L, false)).thenReturn(course);
        // Mock canEdit method
        when(rolesBaseUtil.isAdmin()).thenReturn(false);
        when(rolesBaseUtil.getCurrentPreferredUsernameFromJwt()).thenReturn("otherTeacher");

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
        when(courseQueryService.findCourseById(1L, false)).thenReturn(course);
        // Mock canEdit method
        when(rolesBaseUtil.isAdmin()).thenReturn(true);

        when(courseRepository.save(any(Course.class))).thenAnswer(invocation -> invocation.getArgument(0));
        doNothing().when(course).updateSection(2L, "New Section 1");

        // Act: Act
        courseService.updateSectionInfo(1L, 2L, "New Section 1");

        // Assert: Xác minh rằng các phương thức cần thiết đã được gọi đúng
        verify(courseQueryService, times(1)).findCourseById(1L, false); // Xác minh lấy course
        verify(course, times(1)).updateSection(2L, "New Section 1"); // Xác minh cập nhật thông tin section
        verify(courseRepository, times(1)).save(course); // Xác minh lưu lại course
    }

    @Test
    void updateSectionInfo_ShouldThrowException_WhenCourseNotFound() {
        when(courseQueryService.findCourseById(1L, false)).thenThrow(new ResourceNotFoundException());
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
        when(courseQueryService.findCourseById(1L, false)).thenReturn(courseMock);
        // Mock canEdit method
        when(rolesBaseUtil.isAdmin()).thenReturn(true);
//        doReturn(false).when(courseMock).isNotPublishedOrDeleted();
        doReturn(true).when(courseMock).isPublishedAndNotUnpublishedOrDelete();

        // Act & Assert: Assert
        assertThrows(InputInvalidException.class, () -> {
            courseService.updateSectionInfo(1L, 2L, "New Section 1");
        });

        // Assert: Verify
        verify(courseRepository, never()).save(any(Course.class));
    }

    @Test
    void updateSectionInfo_ShouldThrowException_WhenNotPermission() {
        when(courseQueryService.findCourseById(1L, false)).thenReturn(course);
        // Mock canEdit method
        when(rolesBaseUtil.isAdmin()).thenReturn(false);
        when(rolesBaseUtil.getCurrentPreferredUsernameFromJwt()).thenReturn("otherTeacher");

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
        when(courseQueryService.findCourseById(1L, false)).thenReturn(course);
        // Mock canEdit method
        when(rolesBaseUtil.isAdmin()).thenReturn(true);
        doNothing().when(course).removeSection(2L);

        courseService.removeSection(1L, 2L);

        verify(courseQueryService, times(1)).findCourseById(1L, false);
        verify(course, times(1)).removeSection(2L);
        verify(courseRepository, times(1)).save(course);
    }

    @Test
    void removeSection_CourseNotFound_ThrowsException() {
        when(courseQueryService.findCourseById(1L, false)).thenThrow(new ResourceNotFoundException());
        // Mock canEdit method
        when(rolesBaseUtil.isAdmin()).thenReturn(true);

        assertThrows(ResourceNotFoundException.class, () -> courseService.removeSection(1L, 2L));

        verify(courseRepository, never()).save(any(Course.class));
    }

    @Test
    void removeSection_SectionNotFound_ThrowsException() {
        Course course = spy(this.course);
        when(courseQueryService.findCourseById(1L, false)).thenReturn(course);
        // Mock canEdit method
        when(rolesBaseUtil.isAdmin()).thenReturn(true);
        doThrow(new ResourceNotFoundException()).when(course).removeSection(999L);

        assertThrows(ResourceNotFoundException.class, () -> courseService.removeSection(1L, 999L));

        verify(courseRepository, never()).save(any(Course.class));
    }

    @Test
    void removeSection_PublishedCourse_ThrowsException() {
        Course course = spy(this.course);
        when(courseQueryService.findCourseById(1L, false)).thenReturn(course);
        // Mock canEdit method
        when(rolesBaseUtil.isAdmin()).thenReturn(true);
        doReturn(true).when(course).isPublishedAndNotUnpublishedOrDelete();
//        doReturn(false).when(course).isNotPublishedOrDeleted();

        assertThrows(InputInvalidException.class, () -> courseService.removeSection(1L, 2L));

        verify(courseRepository, never()).save(any(Course.class));
    }

    @Test
    void removeSection_ShouldThrowException_WhenNotPermission() {
        when(courseQueryService.findCourseById(1L, false)).thenReturn(course);
        // Mock canEdit method
        when(rolesBaseUtil.isAdmin()).thenReturn(false);
        when(rolesBaseUtil.getCurrentPreferredUsernameFromJwt()).thenReturn("otherTeacher");

        assertThrows(AccessDeniedException.class, () -> courseService.removeSection(1L, 2L));

        verify(courseRepository, never()).save(any(Course.class));
    }

    @Test
    void addLesson_ValidCourseIdAndSectionId_AddsLesson() {
        Lesson lesson = new Lesson("LessonTitle", Lesson.Type.TEXT, "https://www.example.com");
        Course course = spy(this.course);
        when(courseQueryService.findCourseById(1L, false)).thenReturn(course);
        // Mock canEdit method
        when(rolesBaseUtil.isAdmin()).thenReturn(true);
        doNothing().when(course).addLessonToSection(2L, lesson);

        courseService.addLesson(1L, 2L, lesson);

        verify(courseQueryService, times(1)).findCourseById(1L, false);
        verify(course, times(1)).addLessonToSection(2L, lesson);
        verify(courseRepository, times(1)).save(course);
    }

    @Test
    void addLesson_CourseNotFound_ThrowsException() {
        when(courseQueryService.findCourseById(1L, false)).thenThrow(new ResourceNotFoundException());
        // Mock canEdit method
        when(rolesBaseUtil.isAdmin()).thenReturn(true);
        Lesson lesson = new Lesson("LessonTitle", Lesson.Type.TEXT, "https://www.example.com");

        assertThrows(ResourceNotFoundException.class, () -> courseService.addLesson(1L, 2L, lesson));

        verify(courseRepository, never()).save(any(Course.class));
    }

    @Test
    void addLesson_SectionNotFound_ThrowsException() {
        Course course = spy(this.course);
        when(courseQueryService.findCourseById(1L, false)).thenReturn(course);
        // Mock canEdit method
        when(rolesBaseUtil.isAdmin()).thenReturn(true);
        doThrow(new ResourceNotFoundException()).when(course).addLessonToSection(any(Long.class), any(Lesson.class));
        Lesson lesson = new Lesson("LessonTitle", Lesson.Type.TEXT, "https://www.example.com");

        assertThrows(ResourceNotFoundException.class, () -> courseService.addLesson(1L, 999L, lesson));

        verify(courseRepository, never()).save(any(Course.class));
    }

    @Test
    void addLesson_PublishedCourse_ThrowsException() {
        Course course = spy(this.course);
        when(courseQueryService.findCourseById(1L, false)).thenReturn(course);
        // Mock canEdit method
        when(rolesBaseUtil.isAdmin()).thenReturn(true);
//        doReturn(false).when(course).isNotPublishedOrDeleted();
        doReturn(true).when(course).isPublishedAndNotUnpublishedOrDelete();
        Lesson lesson = new Lesson("LessonTitle", Lesson.Type.TEXT, "https://www.example.com");

        assertThrows(InputInvalidException.class, () -> courseService.addLesson(1L, 2L, lesson));

        verify(courseRepository, never()).save(any(Course.class));
    }

    @Test
    void addLesson_ShouldThrowException_WhenNotPermission() {
        when(courseQueryService.findCourseById(1L, false)).thenReturn(course);
        // Mock canEdit method
        when(rolesBaseUtil.isAdmin()).thenReturn(false);
        when(rolesBaseUtil.getCurrentPreferredUsernameFromJwt()).thenReturn("otherTeacher");
        Lesson lesson = new Lesson("LessonTitle", Lesson.Type.TEXT, "https://www.example.com");

        assertThrows(AccessDeniedException.class, () -> courseService.addLesson(1L, 2L, lesson));

        verify(courseRepository, never()).save(any(Course.class));
    }

    @Test
    void updateLesson_ValidCourseIdAndSectionIdAndLessonId_UpdatesLesson() {
        Course course = spy(this.course);
        Lesson updatedLesson = new Lesson("UpdatedLessonTitle", Lesson.Type.TEXT, "https://www.example.com/updated");
        when(courseQueryService.findCourseById(1L, false)).thenReturn(course);
        // Mock canEdit method
        when(rolesBaseUtil.isAdmin()).thenReturn(true);
        doNothing().when(course).updateLessonInSection(2L, 3L, updatedLesson);

        courseService.updateLesson(1L, 2L, 3L, updatedLesson);

        verify(courseQueryService, times(1)).findCourseById(1L, false);
        verify(course, times(1)).updateLessonInSection(2L, 3L, updatedLesson);
        verify(courseRepository, times(1)).save(course);
    }

    @Test
    void updateLesson_CourseNotFound_ThrowsException() {
        Lesson updatedLesson = new Lesson("UpdatedLessonTitle", Lesson.Type.TEXT, "https://www.example.com/updated");
        when(courseQueryService.findCourseById(1L, false)).thenThrow(new ResourceNotFoundException());
        // Mock canEdit method
        when(rolesBaseUtil.isAdmin()).thenReturn(true);

        assertThrows(ResourceNotFoundException.class, () -> courseService.updateLesson(1L, 2L, 3L, updatedLesson));

        verify(courseRepository, never()).save(any(Course.class));
    }

    @Test
    void updateLesson_SectionNotFound_ThrowsException() {
        Course course = spy(this.course);
        Lesson updatedLesson = new Lesson("UpdatedLessonTitle", Lesson.Type.TEXT, "https://www.example.com/updated");
        when(courseQueryService.findCourseById(1L, false)).thenReturn(course);
        // Mock canEdit method
        when(rolesBaseUtil.isAdmin()).thenReturn(true);
        doThrow(new ResourceNotFoundException()).when(course).updateLessonInSection(999L, 3L, updatedLesson);

        assertThrows(ResourceNotFoundException.class, () -> courseService.updateLesson(1L, 999L, 3L, updatedLesson));

        verify(courseRepository, never()).save(any(Course.class));
    }

    @Test
    void updateLesson_ThrowException_WhenNotPermission() {
        when(courseQueryService.findCourseById(1L, false)).thenReturn(course);
        // Mock canEdit method
        when(rolesBaseUtil.isAdmin()).thenReturn(false);
        when(rolesBaseUtil.getCurrentPreferredUsernameFromJwt()).thenReturn("otherTeacher");
        Lesson updatedLesson = new Lesson("UpdatedLessonTitle", Lesson.Type.TEXT, "https://www.example.com/updated");

        assertThrows(AccessDeniedException.class, () -> courseService.updateLesson(1L, 2L, 3L, updatedLesson));

        verify(courseRepository, never()).save(any(Course.class));
    }

    @Test
    void removeLesson_ValidCourseIdAndSectionIdAndLessonId_RemovesLesson() {
        Course course = spy(this.course);
        when(courseQueryService.findCourseById(1L, false)).thenReturn(course);
        // Mock canEdit method
        when(rolesBaseUtil.isAdmin()).thenReturn(true);
        doNothing().when(course).removeLessonFromSection(2L, 3L);

        courseService.removeLesson(1L, 2L, 3L);

        verify(courseQueryService, times(1)).findCourseById(1L, false);
        verify(course, times(1)).removeLessonFromSection(2L, 3L);
        verify(courseRepository, times(1)).save(course);
    }

    @Test
    void removeLesson_CourseNotFound_ThrowsException() {
        when(courseQueryService.findCourseById(1L, false)).thenThrow(new ResourceNotFoundException());
        // Mock canEdit method
        when(rolesBaseUtil.isAdmin()).thenReturn(true);

        assertThrows(ResourceNotFoundException.class, () -> courseService.removeLesson(1L, 2L, 3L));

        verify(courseRepository, never()).save(any(Course.class));
    }

    @Test
    void removeLesson_SectionNotFound_ThrowsException() {
        Course course = spy(this.course);
        when(courseQueryService.findCourseById(1L, false)).thenReturn(course);
        // Mock canEdit method
        when(rolesBaseUtil.isAdmin()).thenReturn(true);
        doThrow(new ResourceNotFoundException()).when(course).removeLessonFromSection(999L, 3L);

        assertThrows(ResourceNotFoundException.class, () -> courseService.removeLesson(1L, 999L, 3L));

        verify(courseRepository, never()).save(any(Course.class));
    }

    @Test
    void removeLesson_LessonNotFound_ThrowsException() {
        Course course = spy(this.course);
        when(courseQueryService.findCourseById(1L, false)).thenReturn(course);
        // Mock canEdit method
        when(rolesBaseUtil.isAdmin()).thenReturn(true);
        doThrow(new ResourceNotFoundException()).when(course).removeLessonFromSection(2L, 999L);

        assertThrows(ResourceNotFoundException.class, () -> courseService.removeLesson(1L, 2L, 999L));

        verify(courseRepository, never()).save(any(Course.class));
    }

    @Test
    void removeLesson_PublishedCourse_ThrowsException() {
        Course course = spy(this.course);
        when(courseQueryService.findCourseById(1L, false)).thenReturn(course);
        // Mock canEdit method
        when(rolesBaseUtil.isAdmin()).thenReturn(true);
        doReturn(true).when(course).isPublishedAndNotUnpublishedOrDelete();
//        doReturn(false).when(course).isNotPublishedOrDeleted();

        assertThrows(InputInvalidException.class, () -> courseService.removeLesson(1L, 2L, 3L));

        verify(courseRepository, never()).save(any(Course.class));
    }

    @Test
    void requestPublish_shouldAddRequest_whenValidRequest() {
        CourseRequestDTO courseRequestDTO = TestFactory.createDefaultCourseRequestDTOPublish();
        Course course = spy(this.course);
        when(courseQueryService.findCourseById(1L, false)).thenReturn(course);
        doNothing().when(course).requestPublish(any(CourseRequest.class));

        courseService.requestPublish(1L, courseRequestDTO);

        verify(courseQueryService, times(1)).findCourseById(1L, false);
        verify(course, times(1)).requestPublish(any(CourseRequest.class));
        verify(courseRepository, times(1)).save(course);
    }

    @Test
    void requestPublish_shouldThrowException_whenCourseNotFound() {
        CourseRequestDTO courseRequestDTO = TestFactory.createDefaultCourseRequestDTOPublish();
        when(courseQueryService.findCourseById(1L, false)).thenThrow(new ResourceNotFoundException());

        assertThrows(ResourceNotFoundException.class, () -> courseService.requestPublish(1L, courseRequestDTO));

        verify(courseRepository, never()).save(any(Course.class));
    }

    @Test
    void requestPublish_shouldThrowException_whenBusinessLogicThrow() {
        CourseRequestDTO courseRequestDTO = TestFactory.createDefaultCourseRequestDTOPublish();
        Course course = spy(this.course);
        when(courseQueryService.findCourseById(1L, false)).thenReturn(course);
        doThrow(new InputInvalidException("something err")).when(course).requestPublish(any(CourseRequest.class));

        assertThrows(InputInvalidException.class, () -> courseService.requestPublish(1L, courseRequestDTO));

        verify(courseRepository, never()).save(any(Course.class));
    }

    @Test
    void requestUnpublish_shouldAddRequest_whenValidRequest() {
        CourseRequestDTO courseRequestDTO = TestFactory.createDefaultCourseRequestDTOUnPublish();
        Course course = spy(this.course);
        when(courseQueryService.findCourseById(1L, false)).thenReturn(course);
        doNothing().when(course).requestUnpublish(any(CourseRequest.class));

        courseService.requestUnpublish(1L, courseRequestDTO);

        verify(courseQueryService, times(1)).findCourseById(1L, false);
        verify(course, times(1)).requestUnpublish(any(CourseRequest.class));
        verify(courseRepository, times(1)).save(course);
    }

    @Test
    void requestUnpublish_shouldThrowException_whenCourseNotFound() {
        CourseRequestDTO courseRequestDTO = TestFactory.createDefaultCourseRequestDTOUnPublish();
        when(courseQueryService.findCourseById(1L, false)).thenThrow(new ResourceNotFoundException());

        assertThrows(ResourceNotFoundException.class, () -> courseService.requestUnpublish(1L, courseRequestDTO));

        verify(courseRepository, never()).save(any(Course.class));
    }

    @Test
    void requestUnpublish_shouldThrowException_whenBusinessLogicThrow() {
        CourseRequestDTO courseRequestDTO = TestFactory.createDefaultCourseRequestDTOUnPublish();
        Course course = spy(this.course);
        when(courseQueryService.findCourseById(1L, false)).thenReturn(course);
        doThrow(new InputInvalidException("something err")).when(course).requestUnpublish(any(CourseRequest.class));

        assertThrows(InputInvalidException.class, () -> courseService.requestUnpublish(1L, courseRequestDTO));

        verify(courseRepository, never()).save(any(Course.class));
    }

    @Test
    void approvePublish_shouldApprovePublishRequest_whenValidRequest() {
        CourseRequestResolveDTO resolveDTO = TestFactory.createDefaultCourseRequestResolveDTO();
        Course course = spy(this.course);
        when(courseQueryService.findCourseById(1L, false)).thenReturn(course);
        doNothing().when(course).approvePublish(anyLong(), anyString(), anyString());

        courseService.approvePublish(1L, 2L, resolveDTO);

        verify(courseQueryService, times(1)).findCourseById(1L, false);
        verify(course, times(1)).approvePublish(2L, resolveDTO.resolvedBy(), resolveDTO.message());
        verify(courseRepository, times(1)).save(course);
    }

    @Test
    void approvePublish_shouldThrowException_whenCourseNotFound() {
        CourseRequestResolveDTO resolveDTO = TestFactory.createDefaultCourseRequestResolveDTO();
        when(courseQueryService.findCourseById(1L, false)).thenThrow(new ResourceNotFoundException());

        assertThrows(ResourceNotFoundException.class, () -> courseService.approvePublish(1L, 2L, resolveDTO));

        verify(courseRepository, never()).save(any(Course.class));
    }

    @Test
    void approvePublish_shouldThrowException_whenBusinessLogicThrow() {
        CourseRequestResolveDTO resolveDTO = TestFactory.createDefaultCourseRequestResolveDTO();
        Course course = spy(this.course);
        when(courseQueryService.findCourseById(1L, false)).thenReturn(course);
        doThrow(new InputInvalidException("something err")).when(course).approvePublish(anyLong(), anyString(), anyString());

        assertThrows(InputInvalidException.class, () -> courseService.approvePublish(1L, 2L, resolveDTO));

        verify(courseRepository, never()).save(any(Course.class));
    }

    @Test
    void rejectPublish_shouldRejectPublishRequest_whenValidRequest() {
        CourseRequestResolveDTO resolveDTO = TestFactory.createDefaultCourseRequestResolveDTO();
        Course course = spy(this.course);
        when(courseQueryService.findCourseById(1L, false)).thenReturn(course);
        doNothing().when(course).rejectPublish(anyLong(), anyString(), anyString());

        courseService.rejectPublish(1L, 2L, resolveDTO);

        verify(courseQueryService, times(1)).findCourseById(1L, false);
        verify(course, times(1)).rejectPublish(2L, resolveDTO.resolvedBy(), resolveDTO.message());
        verify(courseRepository, times(1)).save(course);
    }

    @Test
    void rejectPublish_shouldThrowException_whenCourseNotFound() {
        CourseRequestResolveDTO resolveDTO = TestFactory.createDefaultCourseRequestResolveDTO();
        when(courseQueryService.findCourseById(1L, false)).thenThrow(new ResourceNotFoundException());

        assertThrows(ResourceNotFoundException.class, () -> courseService.rejectPublish(1L, 2L, resolveDTO));

        verify(courseRepository, never()).save(any(Course.class));
    }

    @Test
    void rejectPublish_shouldThrowException_whenBusinessLogicThrow() {
        CourseRequestResolveDTO resolveDTO = TestFactory.createDefaultCourseRequestResolveDTO();
        Course course = spy(this.course);
        when(courseQueryService.findCourseById(1L, false)).thenReturn(course);
        doThrow(new InputInvalidException("something err")).when(course).rejectPublish(anyLong(), anyString(), anyString());

        assertThrows(InputInvalidException.class, () -> courseService.rejectPublish(1L, 2L, resolveDTO));

        verify(courseRepository, never()).save(any(Course.class));
    }

    @Test
    void approveUnPublish_shouldApproveUnPublishRequest_whenValidRequest() {
        CourseRequestResolveDTO resolveDTO = TestFactory.createDefaultCourseRequestResolveDTO();
        Course course = spy(this.course);
        when(courseQueryService.findCourseById(1L, false)).thenReturn(course);
        doNothing().when(course).approveUnpublish(anyLong(), anyString(), anyString());

        courseService.approveUnpublish(1L, 2L, resolveDTO);

        verify(courseQueryService, times(1)).findCourseById(1L, false);
        verify(course, times(1)).approveUnpublish(2L, resolveDTO.resolvedBy(), resolveDTO.message());
        verify(courseRepository, times(1)).save(course);
    }

    @Test
    void approveUnPublish_shouldThrowException_whenCourseNotFound() {
        CourseRequestResolveDTO resolveDTO = TestFactory.createDefaultCourseRequestResolveDTO();
        when(courseQueryService.findCourseById(1L, false)).thenThrow(new ResourceNotFoundException());

        assertThrows(ResourceNotFoundException.class, () -> courseService.approveUnpublish(1L, 2L, resolveDTO));

        verify(courseRepository, never()).save(any(Course.class));
    }

    @Test
    void approveUnPublish_shouldThrowException_whenBusinessLogicThrow() {
        CourseRequestResolveDTO resolveDTO = TestFactory.createDefaultCourseRequestResolveDTO();
        Course course = spy(this.course);
        when(courseQueryService.findCourseById(1L, false)).thenReturn(course);
        doThrow(new InputInvalidException("something err")).when(course).approveUnpublish(anyLong(), anyString(), anyString());

        assertThrows(InputInvalidException.class, () -> courseService.approveUnpublish(1L, 2L, resolveDTO));

        verify(courseRepository, never()).save(any(Course.class));
    }

    @Test
    void rejectUnPublish_shouldRejectUnPublishRequest_whenValidRequest() {
        CourseRequestResolveDTO resolveDTO = TestFactory.createDefaultCourseRequestResolveDTO();
        Course course = spy(this.course);
        when(courseQueryService.findCourseById(1L, false)).thenReturn(course);
        doNothing().when(course).rejectUnpublish(anyLong(), anyString(), anyString());

        courseService.rejectUnpublish(1L, 2L, resolveDTO);

        verify(courseQueryService, times(1)).findCourseById(1L, false);
        verify(course, times(1)).rejectUnpublish(any(), any(), any());
        verify(courseRepository, times(1)).save(course);
    }

    @Test
    void rejectUnPublish_shouldThrowException_whenCourseNotFound() {
        CourseRequestResolveDTO resolveDTO = TestFactory.createDefaultCourseRequestResolveDTO();
        when(courseQueryService.findCourseById(1L, false)).thenThrow(new ResourceNotFoundException());

        assertThrows(ResourceNotFoundException.class, () -> courseService.rejectUnpublish(1L, 2L, resolveDTO));

        verify(courseRepository, never()).save(any(Course.class));
    }

    @Test
    void rejectUnPublish_shouldThrowException_whenBusinessLogicThrow() {
        CourseRequestResolveDTO resolveDTO = TestFactory.createDefaultCourseRequestResolveDTO();
        Course course = spy(this.course);
        when(courseQueryService.findCourseById(1L, false)).thenReturn(course);
        doThrow(new InputInvalidException("something err")).when(course).rejectUnpublish(anyLong(), anyString(), anyString());

        assertThrows(InputInvalidException.class, () -> courseService.rejectUnpublish(1L, 2L, resolveDTO));

        verify(courseRepository, never()).save(any(Course.class));
    }

    // Post
    @Test
    void addPost_ValidCourseId_AddsPost() {
        Course spy = spy(course);
        doNothing().when(spy).addPost(any());
        when(courseQueryService.findCourseById(1L, false)).thenReturn(spy);
        when(rolesBaseUtil.getCurrentPreferredUsernameFromJwt()).thenReturn(TestFactory.teacher);
        when(rolesBaseUtil.getCurrentUserInfoFromJwt()).thenReturn(TestFactory.createDefaultUserInfo());

        CoursePostDTO coursePostDTO = new CoursePostDTO("Post content", Set.of("http://example.com/1", "http://example.com/2"));
        courseService.addPost(1L, coursePostDTO);

        verify(courseRepository, times(1)).save(spy);
    }

    @Test
    void addPost_CourseNotFound_ThrowsException() {
        when(courseQueryService.findCourseById(1L, false)).thenThrow(new ResourceNotFoundException());
        when(rolesBaseUtil.getCurrentPreferredUsernameFromJwt()).thenReturn(TestFactory.teacher);
        when(rolesBaseUtil.getCurrentUserInfoFromJwt()).thenReturn(TestFactory.createDefaultUserInfo());

        CoursePostDTO coursePostDTO = new CoursePostDTO("Post content", Set.of("http://example.com/1", "http://example.com/2"));

        assertThrows(ResourceNotFoundException.class, () -> courseService.addPost(1L, coursePostDTO));
        verify(courseRepository, never()).save(any(Course.class));
    }


    @Test
    void updatePost_ValidCourseIdAndPostId_UpdatesPost() {
        Course spy = spy(course);
        doNothing().when(spy).updatePost(any(), any(), any());

        when(courseQueryService.findCourseById(1L, false)).thenReturn(spy);
        when(rolesBaseUtil.getCurrentPreferredUsernameFromJwt()).thenReturn(TestFactory.teacher);
        when(rolesBaseUtil.getCurrentUserInfoFromJwt()).thenReturn(TestFactory.createDefaultUserInfo());

        CoursePostDTO coursePostDTO = new CoursePostDTO("Updated content", Set.of("http://example.com/3"));
        courseService.updatePost(1L, 1L, coursePostDTO);

        verify(courseRepository, times(1)).save(spy);
    }

    @Test
    void updatePost_CourseNotFound_ThrowsException() {
        when(courseQueryService.findCourseById(1L, false)).thenThrow(new ResourceNotFoundException());
        when(rolesBaseUtil.getCurrentPreferredUsernameFromJwt()).thenReturn(TestFactory.teacher);
        when(rolesBaseUtil.getCurrentUserInfoFromJwt()).thenReturn(TestFactory.createDefaultUserInfo());

        CoursePostDTO coursePostDTO = new CoursePostDTO("Updated content", Set.of("http://example.com/3"));

        assertThrows(ResourceNotFoundException.class, () -> courseService.updatePost(1L, 1L, coursePostDTO));
        verify(courseRepository, never()).save(any(Course.class));
    }

    @Test
    void deletePost_ValidCourseIdAndPostId_DeletesPost() {
        Course spy = spy(course);
        doNothing().when(spy).deletePost(any());

        when(courseQueryService.findCourseById(1L, false)).thenReturn(spy);
        when(rolesBaseUtil.getCurrentPreferredUsernameFromJwt()).thenReturn(TestFactory.teacher);
        when(rolesBaseUtil.getCurrentUserInfoFromJwt()).thenReturn(TestFactory.createDefaultUserInfo());

        courseService.deletePost(1L, 1L);

        verify(courseRepository, times(1)).save(spy);
    }

    @Test
    void deletePost_CourseNotFound_ThrowsException() {
        when(courseQueryService.findCourseById(1L, false)).thenThrow(new ResourceNotFoundException());
        when(rolesBaseUtil.getCurrentPreferredUsernameFromJwt()).thenReturn(TestFactory.teacher);
        when(rolesBaseUtil.getCurrentUserInfoFromJwt()).thenReturn(TestFactory.createDefaultUserInfo());

        assertThrows(ResourceNotFoundException.class, () -> courseService.deletePost(1L, 1L));
        verify(courseRepository, never()).save(any(Course.class));
    }

    @Test
    void restorePost_ValidCourseIdAndPostId_RestoresPost() {
        Course spy = spy(course);
        doNothing().when(spy).restorePost(any());

        when(courseQueryService.findCourseById(1L, false)).thenReturn(spy);
        when(rolesBaseUtil.getCurrentPreferredUsernameFromJwt()).thenReturn(TestFactory.teacher);
        when(rolesBaseUtil.getCurrentUserInfoFromJwt()).thenReturn(TestFactory.createDefaultUserInfo());

        courseService.restorePost(1L, 1L);

        verify(courseRepository, times(1)).save(spy);
    }

    @Test
    void restorePost_CourseNotFound_ThrowsException() {
        when(courseQueryService.findCourseById(1L, false)).thenThrow(new ResourceNotFoundException());
        when(rolesBaseUtil.getCurrentPreferredUsernameFromJwt()).thenReturn(TestFactory.teacher);
        when(rolesBaseUtil.getCurrentUserInfoFromJwt()).thenReturn(TestFactory.createDefaultUserInfo());

        assertThrows(ResourceNotFoundException.class, () -> courseService.restorePost(1L, 1L));
        verify(courseRepository, never()).save(any(Course.class));
    }

    @Test
    void deleteForcePost_ValidCourseIdAndPostId_ForceDeletesPost() {
        Course spy = spy(course);
        doNothing().when(spy).forceDeletePost(any());

        when(courseQueryService.findCourseById(1L, false)).thenReturn(spy);
        when(rolesBaseUtil.getCurrentPreferredUsernameFromJwt()).thenReturn(TestFactory.teacher);
        when(rolesBaseUtil.getCurrentUserInfoFromJwt()).thenReturn(TestFactory.createDefaultUserInfo());

        courseService.deleteForcePost(1L, 1L);

        verify(courseRepository, times(1)).save(spy);
    }

    @Test
    void addQuizToSection_ValidCourseIdAndSectionId_AddsQuiz() {
        Course spy = spy(course);
        doNothing().when(spy).addQuizToSection(anyLong(), any());
        when(courseQueryService.findCourseById(1L, false)).thenReturn(spy);

        QuizDTO quizDTO = new QuizDTO("Quiz Title", "Quiz Description", 1L, 50);
        courseService.addQuizToSection(1L, 1L, quizDTO);

        verify(courseRepository, times(1)).save(spy);
    }

    @Test
    void addQuizToSection_CourseNotFound_ThrowsException() {
        when(courseQueryService.findCourseById(1L, false)).thenThrow(new ResourceNotFoundException());

        QuizDTO quizDTO = new QuizDTO("Quiz Title", "Quiz Description", 1L, 50);

        assertThrows(ResourceNotFoundException.class, () -> courseService.addQuizToSection(1L, 1L, quizDTO));
        verify(courseRepository, never()).save(any(Course.class));
    }

    @Test
    void addQuizToSection_SectionNotFound_ThrowsException() {
        Course spy = spy(course);
        doThrow(new ResourceNotFoundException()).when(spy).addQuizToSection(anyLong(), any());
        when(courseQueryService.findCourseById(1L, false)).thenReturn(spy);

        QuizDTO quizDTO = new QuizDTO("Quiz Title", "Quiz Description", 1L, 50);

        assertThrows(ResourceNotFoundException.class, () -> courseService.addQuizToSection(1L, 999L, quizDTO));
        verify(courseRepository, never()).save(any(Course.class));
    }

    @Test
    void addQuizToSection_PublishedCourse_ThrowsException() {
        Course spy = spy(course);
//        doReturn(false).when(spy).isNotPublishedOrDeleted();
        doReturn(true).when(spy).isPublishedAndNotUnpublishedOrDelete();
        when(courseQueryService.findCourseById(1L, false)).thenReturn(spy);

        QuizDTO quizDTO = new QuizDTO("Quiz Title", "Quiz Description", 1L, 50);

        String msg = assertThrows(InputInvalidException.class, () -> courseService.addQuizToSection(1L, 1L, quizDTO)).getMessage();
        assertEquals("Cannot add a quiz to a published course.", msg);
        verify(courseRepository, never()).save(any(Course.class));
    }

    @Test
    void updateQuiz_ValidCourseIdAndSectionIdAndQuizId_UpdatesQuiz() {
        Course course = spy(this.course);
        QuizUpdateDTO quizUpdateDTO = new QuizUpdateDTO("Updated Title", "Updated Description", 85);

        when(courseQueryService.findCourseById(1L, false)).thenReturn(course);
        doNothing().when(course).updateQuizInSection(2L, 3L, quizUpdateDTO.title(), quizUpdateDTO.description(), 85);

        courseService.updateQuiz(1L, 2L, 3L, quizUpdateDTO);

        verify(courseQueryService, times(1)).findCourseById(1L, false);
        verify(course, times(1)).updateQuizInSection(2L, 3L, quizUpdateDTO.title(), quizUpdateDTO.description(), 85);
        verify(courseRepository, times(1)).save(course);
    }

    @Test
    void updateQuiz_CourseNotFound_ThrowsException() {
        when(courseQueryService.findCourseById(1L, false)).thenThrow(new ResourceNotFoundException());

        QuizUpdateDTO quizUpdateDTO = new QuizUpdateDTO("Updated Title", "Updated Description", 85);

        assertThrows(ResourceNotFoundException.class, () -> courseService.updateQuiz(1L, 2L, 3L, quizUpdateDTO));
        verify(courseRepository, never()).save(any(Course.class));
    }

    @Test
    void updateQuiz_SectionNotFound_ThrowsException() {
        Course course = spy(this.course);
        QuizUpdateDTO quizUpdateDTO = new QuizUpdateDTO("Updated Title", "Updated Description", 85);

        when(courseQueryService.findCourseById(1L, false)).thenReturn(course);
        doThrow(new ResourceNotFoundException()).when(course).updateQuizInSection(999L, 3L,
                quizUpdateDTO.title(), quizUpdateDTO.description(), 85);

        assertThrows(ResourceNotFoundException.class, () -> courseService.updateQuiz(1L, 999L, 3L, quizUpdateDTO));
        verify(courseRepository, never()).save(any(Course.class));
    }

    @Test
    void updateQuiz_QuizNotFound_ThrowsException() {
        Course course = spy(this.course);
        QuizUpdateDTO quizUpdateDTO = new QuizUpdateDTO("Updated Title", "Updated Description", 85);

        when(courseQueryService.findCourseById(1L, false)).thenReturn(course);
        doThrow(new ResourceNotFoundException()).when(course).updateQuizInSection(2L, 999L,
                quizUpdateDTO.title(), quizUpdateDTO.description(), 85);

        assertThrows(ResourceNotFoundException.class, () -> courseService.updateQuiz(1L, 2L, 999L, quizUpdateDTO));
        verify(courseRepository, never()).save(any(Course.class));
    }

    @Test
    void updateQuiz_PublishedCourse_ThrowsException() {
        Course course = spy(this.course);
        QuizUpdateDTO quizUpdateDTO = new QuizUpdateDTO("Updated Title", "Updated Description", 85);

        when(courseQueryService.findCourseById(1L, false)).thenReturn(course);
        doReturn(true).when(course).isPublishedAndNotUnpublishedOrDelete();

        assertThrows(InputInvalidException.class, () -> courseService.updateQuiz(1L, 2L, 3L, quizUpdateDTO));
        verify(courseRepository, never()).save(any(Course.class));
    }


    @Test
    void deleteQuiz_ValidCourseIdAndSectionIdAndQuizId_DeletesQuiz() {
        Course course = spy(this.course);
        when(courseQueryService.findCourseById(1L, false)).thenReturn(course);
        doNothing().when(course).deleteQuizFromSection(2L, 3L);

        courseService.deleteQuiz(1L, 2L, 3L);

        verify(courseQueryService, times(1)).findCourseById(1L, false);
        verify(course, times(1)).deleteQuizFromSection(2L, 3L);
        verify(courseRepository, times(1)).save(course);
    }

    @Test
    void deleteQuiz_CourseNotFound_ThrowsException() {
        when(courseQueryService.findCourseById(1L, false)).thenThrow(new ResourceNotFoundException());

        assertThrows(ResourceNotFoundException.class, () -> courseService.deleteQuiz(1L, 2L, 3L));
        verify(courseRepository, never()).save(any(Course.class));
    }

    @Test
    void deleteQuiz_PublishedCourse_ThrowsException() {
        Course course = spy(this.course);
        when(courseQueryService.findCourseById(1L, false)).thenReturn(course);
        doReturn(true).when(course).isPublishedAndNotUnpublishedOrDelete();
//        doReturn(false).when(course).isNotPublishedOrDeleted();

        assertThrows(InputInvalidException.class, () -> courseService.deleteQuiz(1L, 2L, 3L));
        verify(courseRepository, never()).save(any(Course.class));
    }

    @Test
    void restoreQuiz_ValidCourseIdAndSectionIdAndQuizId_RestoresQuiz() {
        Course course = spy(this.course);
        when(courseQueryService.findCourseById(1L, false)).thenReturn(course);
        doNothing().when(course).restoreQuizInSection(2L, 3L);

        courseService.restoreQuiz(1L, 2L, 3L);

        verify(courseQueryService, times(1)).findCourseById(1L, false);
        verify(course, times(1)).restoreQuizInSection(2L, 3L);
        verify(courseRepository, times(1)).save(course);
    }

    @Test
    void restoreQuiz_CourseNotFound_ThrowsException() {
        when(courseQueryService.findCourseById(1L, false)).thenThrow(new ResourceNotFoundException());

        assertThrows(ResourceNotFoundException.class, () -> courseService.restoreQuiz(1L, 2L, 3L));
        verify(courseRepository, never()).save(any(Course.class));
    }

    @Test
    void deleteForceQuiz_ValidCourseIdAndSectionIdAndQuizId_ForceDeletesQuiz() {
        Course course = spy(this.course);
        when(courseQueryService.findCourseById(1L, false)).thenReturn(course);
        doNothing().when(course).forceDeleteQuizFromSection(2L, 3L);

        courseService.deleteForceQuiz(1L, 2L, 3L);

        verify(courseQueryService, times(1)).findCourseById(1L, false);
        verify(course, times(1)).forceDeleteQuizFromSection(2L, 3L);
        verify(courseRepository, times(1)).save(course);
    }

    @Test
    void deleteForceQuiz_CourseNotFound_ThrowsException() {
        when(courseQueryService.findCourseById(1L, false)).thenThrow(new ResourceNotFoundException());

        assertThrows(ResourceNotFoundException.class, () -> courseService.deleteForceQuiz(1L, 2L, 3L));
        verify(courseRepository, never()).save(any(Course.class));
    }

    @Test
    void addQuestionToQuiz_ValidCourseIdAndSectionIdAndQuizId_AddsQuestion() {
        Course spy = spy(course);
        doNothing().when(spy).addQuestionToQuizInSection(anyLong(), anyLong(), any());
        when(courseQueryService.findCourseById(1L, false)).thenReturn(spy);

        QuestionDTO questionDTO = new QuestionDTO("Question content", QuestionType.SINGLE_CHOICE,
                Set.of(new AnswerOptionDTO("Option 1", true), new AnswerOptionDTO("Option 2", false)), 1, null);
        courseService.addQuestionToQuiz(1L, 1L, 1L, questionDTO);

        verify(courseRepository, times(1)).save(spy);
    }

    @Test
    void addQuestionToQuiz_CourseNotFound_ThrowsException() {
        when(courseQueryService.findCourseById(1L, false)).thenThrow(new ResourceNotFoundException());

        QuestionDTO questionDTO = new QuestionDTO("Question content", QuestionType.SINGLE_CHOICE,
                Set.of(new AnswerOptionDTO("Option 1", true), new AnswerOptionDTO("Option 2", false)), 1, null);

        assertThrows(ResourceNotFoundException.class, () -> courseService.addQuestionToQuiz(1L, 1L, 1L, questionDTO));
        verify(courseRepository, never()).save(any(Course.class));
    }

    @Test
    void updateQuestion_ValidCourseIdAndSectionIdAndQuizIdAndQuestionId_UpdatesQuestion() {
        Course course = spy(this.course);

        when(courseQueryService.findCourseById(1L, false)).thenReturn(course);
        doNothing().when(course).updateQuestionInQuizInSection(anyLong(), anyLong(), any(), any(Question.class));

        QuestionDTO questionDTO = new QuestionDTO("Question content", QuestionType.SINGLE_CHOICE,
                Set.of(new AnswerOptionDTO("Option 1", true), new AnswerOptionDTO("Option 2", false)), 1, null);

        courseService.updateQuestion(1L, 2L, 3L, 4L, questionDTO);

        verify(courseQueryService, times(1)).findCourseById(1L, false);
        verify(courseRepository, times(1)).save(course);
    }

    @Test
    void updateQuestion_CourseNotFound_ThrowsException() {
        when(courseQueryService.findCourseById(1L, false)).thenThrow(new ResourceNotFoundException());

        QuestionDTO questionDTO = new QuestionDTO("Question content", QuestionType.SINGLE_CHOICE,
                Set.of(new AnswerOptionDTO("Option 1", true), new AnswerOptionDTO("Option 2", false)), 1, null);

        assertThrows(ResourceNotFoundException.class, () -> courseService.updateQuestion(1L, 2L, 3L, 4L, questionDTO));
        verify(courseRepository, never()).save(any(Course.class));
    }

    @Test
    void deleteQuestion_ValidCourseIdAndSectionIdAndQuizIdAndQuestionId_DeletesQuestion() {
        Course course = spy(this.course);
        when(courseQueryService.findCourseById(1L, false)).thenReturn(course);
        doNothing().when(course).deleteQuestionFromQuizInSection(2L, 3L, 4L);

        courseService.deleteQuestion(1L, 2L, 3L, 4L);

        verify(courseQueryService, times(1)).findCourseById(1L, false);
        verify(course, times(1)).deleteQuestionFromQuizInSection(2L, 3L, 4L);
        verify(courseRepository, times(1)).save(course);
    }

    @Test
    void deleteQuestion_CourseNotFound_ThrowsException() {
        when(courseQueryService.findCourseById(1L, false)).thenThrow(new ResourceNotFoundException());

        assertThrows(ResourceNotFoundException.class, () -> courseService.deleteQuestion(1L, 2L, 3L, 4L));
        verify(courseRepository, never()).save(any(Course.class));
    }


}
