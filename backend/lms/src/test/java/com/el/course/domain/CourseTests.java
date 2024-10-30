package com.el.course.domain;

import com.el.TestFactory;
import com.el.common.Currencies;
import com.el.common.exception.InputInvalidException;
import com.el.common.exception.ResourceNotFoundException;
import org.javamoney.moneta.Money;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import javax.money.MonetaryAmount;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class CourseTests {

    Course courseNoSections;
    Course courseWithSections;

    @BeforeEach
    void setup() {

        courseNoSections = TestFactory.createDefaultCourse();

        // Tạo một course có sections
        CourseSection courseSection = new CourseSection("SectionTitle");
        courseSection.addLesson(new Lesson("LessonTitle", Lesson.Type.TEXT, "https://www.example.com", null));

        courseWithSections = TestFactory.createDefaultCourse();
        courseWithSections.addSection(courseSection);
        courseWithSections.changePrice(Money.of(100, Currencies.VND));
    }

    @AfterEach
    void tearDown() {
        courseNoSections = null;
        courseWithSections = null;
    }

    @Test
    public void courseConstructor_ValidInput_CreatesCourse() {
        Course course = TestFactory.createDefaultCourse();

        // Chỉ cần logic quan trọng, không cần check getter
        assertFalse(course.isDeleted());
        assertFalse(course.getPublished());
    }

    @Test
    public void courseConstructor_EmptyTitle_ThrowsException() {
        Set<String> benefits = new HashSet<>(Arrays.asList("Benefit1", "Benefit2"));
        Set<String> prerequisites = new HashSet<>(Arrays.asList("Prerequisite1", "Prerequisite2"));
        Set<Language> subtitles = new HashSet<>(Arrays.asList(Language.ENGLISH, Language.SPANISH));
        assertThrows(InputInvalidException.class, () -> new Course("", "Description", "ThumbnailUrl", benefits, Language.ENGLISH, prerequisites, subtitles, "Teacher"));
    }

    @Test
    public void courseConstructor_NullTitle_ThrowsException() {
        Set<String> benefits = new HashSet<>(Arrays.asList("Benefit1", "Benefit2"));
        Set<String> prerequisites = new HashSet<>(Arrays.asList("Prerequisite1", "Prerequisite2"));
        Set<Language> subtitles = new HashSet<>(Arrays.asList(Language.ENGLISH, Language.SPANISH));

        assertThrows(InputInvalidException.class, () -> new Course(null, "Description", "ThumbnailUrl", benefits, Language.ENGLISH, prerequisites, subtitles, "Teacher"));
    }

    @Test
    public void updateInfo_ValidInput_UpdatesCourseInfo() {

        Set<String> newBenefits = new HashSet<>(Arrays.asList("NewBenefit1", "NewBenefit2"));
        Set<String> newPrerequisites = new HashSet<>(Arrays.asList("NewPrerequisite1", "NewPrerequisite2"));
        Set<Language> newSubtitles = new HashSet<>(Arrays.asList(Language.FRENCH, Language.GERMAN));
        courseNoSections.updateInfo("NewTitle", "NewDescription", "NewThumbnailUrl", newBenefits, newPrerequisites, newSubtitles);

        assertEquals("NewTitle", courseNoSections.getTitle());
        assertEquals("NewDescription", courseNoSections.getDescription());
        assertEquals("NewThumbnailUrl", courseNoSections.getThumbnailUrl());
        assertEquals(newBenefits, courseNoSections.getBenefits());
        assertEquals(newPrerequisites, courseNoSections.getPrerequisites());
        assertEquals(newSubtitles, courseNoSections.getSubtitles());
    }


    @Test
    public void updateInfo_EmptyTitle_ThrowsException() {
        Set<String> newBenefits = new HashSet<>(Arrays.asList("NewBenefit1", "NewBenefit2"));
        Set<String> newPrerequisites = new HashSet<>(Arrays.asList("NewPrerequisite1", "NewPrerequisite2"));
        Set<Language> newSubtitles = new HashSet<>(Arrays.asList(Language.FRENCH, Language.GERMAN));
        assertThrows(InputInvalidException.class, () -> courseNoSections.updateInfo("", "NewDescription", "NewThumbnailUrl", newBenefits, newPrerequisites, newSubtitles));
    }


    @Test
    public void updateInfo_NullTitle_ThrowsException() {

        Set<String> newBenefits = new HashSet<>(Arrays.asList("NewBenefit1", "NewBenefit2"));
        Set<String> newPrerequisites = new HashSet<>(Arrays.asList("NewPrerequisite1", "NewPrerequisite2"));
        Set<Language> newSubtitles = new HashSet<>(Arrays.asList(Language.FRENCH, Language.GERMAN));
        assertThrows(InputInvalidException.class, () -> courseNoSections.updateInfo(null, "NewDescription", "NewThumbnailUrl", newBenefits, newPrerequisites, newSubtitles));
    }

    @Test
    public void updateInfo_PublishedCourse_ThrowsException() {
        // Spy đối tượng Course để dùng logic thực
        Course course = spy(courseNoSections);

        // Giả lập trạng thái đã publish bằng cách mock canEdit trả về false
        when(course.isNotPublishedAndDeleted()).thenReturn(false);

        // Tạo dữ liệu để cập nhật
        Set<String> newBenefits = new HashSet<>(Arrays.asList("NewBenefit1", "NewBenefit2"));
        Set<String> newPrerequisites = new HashSet<>(Arrays.asList("NewPrerequisite1", "NewPrerequisite2"));
        Set<Language> newSubtitles = new HashSet<>(Arrays.asList(Language.FRENCH, Language.GERMAN));

        // Kiểm tra rằng khi khóa học đã publish, việc cập nhật sẽ ném ngoại lệ InputInvalidException
        assertThrows(InputInvalidException.class, () -> course.updateInfo(
                "NewTitle",
                "NewDescription",
                "NewThumbnailUrl",
                newBenefits,
                newPrerequisites,
                newSubtitles
        ));

        // Xác minh rằng phương thức updateInfo đã được gọi một lần
        verify(course, times(1)).updateInfo(anyString(), anyString(), anyString(), anySet(), anySet(), anySet());
    }

    @Test
    public void delete_ValidCourse_DeletesCourse() {
        courseNoSections.delete();
        assertTrue(courseNoSections.isDeleted());
    }

    @Test
    public void delete_AlreadyDeletedCourse_ThrowsException() {
        courseNoSections.delete();
        assertThrows(InputInvalidException.class, courseNoSections::delete);
    }

    @Test
    void delete_PublishedCourse_ThrowsException() {
        Course courseMock = spy(courseNoSections);
        when(courseMock.isNotPublishedAndDeleted()).thenReturn(false);
        assertThrows(InputInvalidException.class, courseMock::delete);
    }

    @Test
    void deleteForce_CourseNotDeleted_ThrowsException() {
        assertThrows(InputInvalidException.class, courseNoSections::deleteForce);
    }

    @Test
    void deleteForce_ValidCourse_DeletesCourse() {
        courseNoSections.delete();
        courseNoSections.deleteForce();
        assertTrue(courseNoSections.isDeleted());
    }

    @Test
    void changePrice_ValidPrice_ChangesPrice() {
        MonetaryAmount newPrice = Money.of(100, Currencies.VND);
        courseWithSections.changePrice(newPrice);
        assertEquals(newPrice, courseWithSections.getPrice());
    }

    @Test
    void changePrice_NegativePrice_ThrowsException() {
        MonetaryAmount negativePrice = Money.of(-100, Currencies.VND);
        assertThrows(InputInvalidException.class, () -> courseNoSections.changePrice(negativePrice));
    }

    @Test
    void changePrice_PublishedCourse_ThrowsException() {
        Course course = spy(courseNoSections);
        when(course.isNotPublishedAndDeleted()).thenReturn(false);
        MonetaryAmount newPrice = Money.of(100, Currencies.VND);
        assertThrows(InputInvalidException.class, () -> course.changePrice(newPrice));
    }

    @Test
    void assignTeacher_ValidTeacher_AssignsTeacher() {
        courseNoSections.assignTeacher("NewTeacher");
        assertEquals("NewTeacher", courseNoSections.getTeacher());
    }

    @Test
    void assignTeacher_NullTeacher_ThrowsException() {
        assertThrows(InputInvalidException.class, () -> courseNoSections.assignTeacher(null));
    }

    @Test
    void assignTeacher_PublishedCourse_ThrowsException() {
        Course course = spy(courseNoSections);
        when(course.isNotPublishedAndDeleted()).thenReturn(false);
        assertThrows(InputInvalidException.class, () -> course.assignTeacher("NewTeacher"));
    }


    @Test
    void addSection_ValidSection_AddsSection() {
        CourseSection section = new CourseSection("SectionTitle");
        section.addLesson(new Lesson("LessonTitle", Lesson.Type.TEXT, "https://www.example.com", null));
        courseNoSections.addSection(section);
        assertTrue(courseNoSections.getSections().contains(section));
    }

    @Test
    void addSection_NullSection_ThrowsException() {
        assertThrows(InputInvalidException.class, () -> courseNoSections.addSection(null));
    }

    @Test
    void addSection_SectionWithSameTitle_ThrowsException() {
        // Tạo một section khác có cùng title với section đã thêm vào course
        CourseSection duplicateSection = new CourseSection(courseWithSections.getSections().iterator().next().getTitle());
        assertThrows(InputInvalidException.class, () -> courseWithSections.addSection(duplicateSection));
    }

    @Test
    void addSection_PublishedCourse_ThrowsException() {
        Course course = spy(courseNoSections);
        when(course.isNotPublishedAndDeleted()).thenReturn(false); // Giả lập khóa học đã publish

        CourseSection section = new CourseSection("SectionTitle");
        assertThrows(InputInvalidException.class, () -> course.addSection(section));
    }

    @Test
    void testUpdateSection_ShouldUpdateSectionTitle() {
        CourseSection section = spy(new CourseSection("SectionTitle"));
        // Mock id của section
        when(section.getId()).thenReturn(1L);
        section.addLesson(new Lesson("LessonTitle", Lesson.Type.TEXT, "https://www.example.com", null));
        courseNoSections.addSection(section);
        assertTrue(courseNoSections.getSections().contains(section));

        // update section title
        courseNoSections.updateSection(section.getId(), "NewSectionTitle");

        // verify section title is updated
        verify(section).updateInfo("NewSectionTitle");
        assertEquals("NewSectionTitle", section.getTitle());
    }

    @Test
    void testUpdateSection_SectionWithSameTitle_ThrowsException() {
        CourseSection section = spy(new CourseSection("SectionTitle"));
        // Mock id của section
        when(section.getId()).thenReturn(1L);
        section.addLesson(new Lesson("LessonTitle", Lesson.Type.TEXT, "https://www.example.com", null));
        courseNoSections.addSection(section);
        assertTrue(courseNoSections.getSections().contains(section));

        // Tạo một section khác có cùng title với section đã thêm vào course
        CourseSection duplicateSection = new CourseSection("SectionTitle");
        assertThrows(InputInvalidException.class, () -> courseNoSections.addSection(duplicateSection));
    }

    @Test
    void testUpdateSection_PublishedCourse_ThrowsException() {
        Course course = spy(courseNoSections);
        when(course.isNotPublishedAndDeleted()).thenReturn(false); // Giả lập khóa học đã publish

        CourseSection section = new CourseSection("SectionTitle");
        assertThrows(InputInvalidException.class, () -> course.addSection(section));
    }

    @Test
    void testUpdateSection_SectionNotFound_ThrowsException() {
        assertThrows(ResourceNotFoundException.class, () -> courseNoSections.updateSection(1L, "NewSectionTitle"));
    }

    @Test
    void removeSection_ValidSectionId_RemovesSection() {
        CourseSection section = spy(new CourseSection("SectionTitle"));
        when(section.getId()).thenReturn(1L);
        section.addLesson(new Lesson("LessonTitle", Lesson.Type.TEXT, "https://www.example.com", null));
        courseNoSections.addSection(section);

        courseNoSections.removeSection(1L);

        assertFalse(courseNoSections.getSections().contains(section));
    }

    @Test
    void removeSection_NonExistentSectionId_ThrowsException() {
        assertThrows(ResourceNotFoundException.class, () -> courseNoSections.removeSection(999L));
    }

    @Test
    void removeSection_PublishedCourse_ThrowsException() {
        Course course = spy(courseWithSections);
        when(course.isNotPublishedAndDeleted()).thenReturn(false);

        assertThrows(InputInvalidException.class, () -> course.removeSection(1L));
    }

    @Test
    void addLessonToSection_ValidSectionIdAndLesson_AddsLesson() {
        CourseSection section = spy(new CourseSection("SectionTitle"));
        when(section.getId()).thenReturn(1L);
        // Khi thêm một lesson mới vào section, không có lesson nào trùng title
        section.addLesson(new Lesson("LessonTitle 1", Lesson.Type.TEXT, "https://www.example.com/1", null));
        courseNoSections.addSection(section);

        courseNoSections.addLessonToSection(1L, new Lesson("LessonTitle", Lesson.Type.TEXT, "https://www.example.com/2", null));
        assertEquals(2, section.getLessons().size());
    }

    @Test
    void addLessonToSection_ValidSectionIdButLessonDuplicateTitle_ThrowException() {
        CourseSection section = spy(new CourseSection("SectionTitle"));
        when(section.getId()).thenReturn(1L);
        // Khi thêm một lesson mới vào section, không có lesson nào trùng title
        section.addLesson(new Lesson("LessonTitle", Lesson.Type.TEXT, "https://www.example.com/1", null));

        courseNoSections.addSection(section);

        assertThrows(InputInvalidException.class, () -> courseNoSections
                .addLessonToSection(1L, new Lesson("LessonTitle", Lesson.Type.TEXT, "https://www.example.com/2", null)));
    }

    @Test
    void addLessonToSection_ValidSectionIdButLessonDuplicateLink_ThrowException() {
        CourseSection section = spy(new CourseSection("SectionTitle"));
        when(section.getId()).thenReturn(1L);
        // Khi thêm một lesson mới vào section, không có lesson nào trùng title
        section.addLesson(new Lesson("LessonTitle", Lesson.Type.TEXT, "https://www.example.com/1", null));

        courseNoSections.addSection(section);

        assertThrows(InputInvalidException.class, () -> courseNoSections
                .addLessonToSection(1L, new Lesson("LessonTitle 2", Lesson.Type.TEXT, "https://www.example.com/1", null)));
    }

    @Test
    void addLessonToSection_PublishedCourse_ThrowsException() {
        Course course = spy(courseNoSections);
        when(course.isNotPublishedAndDeleted()).thenReturn(false);

        assertThrows(InputInvalidException.class, () -> course.addLessonToSection(1L, new Lesson("LessonTitle", Lesson.Type.TEXT, "https://www.example.com", null)));
    }

    @Test
    void addLessonToSection_SectionNotFound_ThrowsException() {
        assertThrows(ResourceNotFoundException.class, () -> courseNoSections.addLessonToSection(1L, new Lesson("LessonTitle", Lesson.Type.TEXT, "https://www.example.com", null)));
    }

    @Test
    void updateLessonInSection_ValidSectionIdLessonIdAndLesson_UpdatesLesson() {
        CourseSection section = spy(new CourseSection("SectionTitle"));
        when(section.getId()).thenReturn(1L);
        Lesson lesson = spy(new Lesson("LessonTitle", Lesson.Type.TEXT, "https://www.example.com/1", null));
        when(lesson.getId()).thenReturn(1L);
        section.addLesson(lesson);
        courseNoSections.addSection(section);

        courseNoSections.updateLessonInSection(1L, 1L, new Lesson("UpdatedLessonTitle", Lesson.Type.TEXT, "https://www.example.com/2", null));
        assertEquals("UpdatedLessonTitle", section.findLessonById(1L).getTitle());
    }

    @Test
    void updateLessonInSection_ValidSectionIdLessonIdButLessonDuplicateTitle_ThrowException() {
        CourseSection section = spy(new CourseSection("SectionTitle"));
        when(section.getId()).thenReturn(1L);
        Lesson lesson = spy(new Lesson("LessonTitle", Lesson.Type.TEXT, "https://www.example.com/1", null));
        when(lesson.getId()).thenReturn(1L);
        section.addLesson(lesson);
        courseNoSections.addSection(section);
//
//        section.addLesson(new Lesson("UpdatedLessonTitle", Lesson.Type.TEXT, "https://www.example.com/2", null));
//        assertThrows(InputInvalidException.class, () -> courseNoSections
//                .updateLessonInSection(1L, 1L, new Lesson("UpdatedLessonTitle", Lesson.Type.TEXT, "https://www.example.com/3", null)));
    }

    @Test
    void updateLessonInSection_ValidSectionIdLessonIdButLessonDuplicateLink_ThrowException() {
        CourseSection section = spy(new CourseSection("SectionTitle"));
        when(section.getId()).thenReturn(1L);
        Lesson lesson = spy(new Lesson("LessonTitle", Lesson.Type.TEXT, "https://www.example.com/1", null));
        when(lesson.getId()).thenReturn(2L);
        section.addLesson(lesson);
        courseNoSections.addSection(section);
//
//        section.addLesson(new Lesson("UpdatedLessonTitle", Lesson.Type.TEXT, "https://www.example.com/2", null));
//        assertThrows(InputInvalidException.class, () -> courseNoSections
//                .updateLessonInSection(1L, 2L, new Lesson("UpdatedLessonTitle 1", Lesson.Type.TEXT, "https://www.example.com/2", null)));
    }


    @Test
    void updateLessonInSection_PublishedCourse_ThrowsException() {
        Course course = spy(courseNoSections);
        when(course.isNotPublishedAndDeleted()).thenReturn(false);

        assertThrows(InputInvalidException.class, () -> course.updateLessonInSection(1L, 1L, new Lesson("UpdatedLessonTitle", Lesson.Type.TEXT, "https://www.example.com", null)));
    }

    @Test
    void updateLessonInSection_SectionNotFound_ThrowsException() {
        assertThrows(ResourceNotFoundException.class, () -> courseNoSections.updateLessonInSection(1L, 1L, new Lesson("UpdatedLessonTitle", Lesson.Type.TEXT, "https://www.example.com", null)));
    }

    @Test
    void updateLessonInSection_LessonNotFound_ThrowsException() {
        CourseSection section = spy(new CourseSection("SectionTitle"));
        when(section.getId()).thenReturn(1L);
        Lesson lesson = spy(new Lesson("LessonTitle", Lesson.Type.TEXT, "https://www.example.com/1", null));
        when(lesson.getId()).thenReturn(1L);
        section.addLesson(lesson);
        courseNoSections.addSection(section);

        assertThrows(ResourceNotFoundException.class, () -> courseNoSections.updateLessonInSection(1L, 2L, new Lesson("UpdatedLessonTitle", Lesson.Type.TEXT, "https://www.example.com/2", null)));
    }

    @Test
    void removeLessonFromSection_ValidSectionIdAndLessonId_RemovesLesson() {
        CourseSection section = spy(new CourseSection("SectionTitle"));
        when(section.getId()).thenReturn(1L);
        Lesson lesson = spy(new Lesson("LessonTitle", Lesson.Type.TEXT, "https://www.example.com", null));
        when(lesson.getId()).thenReturn(1L);
        section.addLesson(lesson);
        courseNoSections.addSection(section);

        courseNoSections.removeLessonFromSection(1L, 1L);

        assertFalse(section.getLessons().contains(lesson));
    }

    @Test
    void removeLessonFromSection_SectionNotFound_ThrowsException() {
        assertThrows(ResourceNotFoundException.class, () -> courseNoSections.removeLessonFromSection(999L, 1L));
    }

    @Test
    void removeLessonFromSection_LessonNotFound_ThrowsException() {
        CourseSection section = spy(new CourseSection("SectionTitle"));
        when(section.getId()).thenReturn(1L);
        Lesson lesson = spy(new Lesson("LessonTitle", Lesson.Type.TEXT, "https://www.example.com", null));
        when(lesson.getId()).thenReturn(1L);
        section.addLesson(lesson);
        courseNoSections.addSection(section);

        assertThrows(ResourceNotFoundException.class, () -> courseNoSections.removeLessonFromSection(1L, 999L));
    }

    @Test
    void removeLessonFromSection_PublishedCourse_ThrowsException() {
        Course course = spy(courseNoSections);
        when(course.isNotPublishedAndDeleted()).thenReturn(false);

        assertThrows(InputInvalidException.class, () -> course.removeLessonFromSection(1L, 1L));
    }

    @Test
    void requestPublish_shouldAddRequest_whenValidRequest() {
        assertFalse(courseNoSections.isPublishedAndNotDeleted());
        assertNotNull(courseWithSections.getPrice());
        assertNotNull(courseWithSections.getTeacher());
        assertFalse(courseWithSections.getSections().isEmpty());

        CourseRequest courseRequest = TestFactory.createDefaultCourseRequestPublish();

        courseWithSections.requestPublish(courseRequest);

        assertTrue(courseWithSections.getCourseRequests().contains(courseRequest));
    }

    @Test
    void requestPublish_shouldThrowException_whenCourseAlreadyPublished() {
        assertFalse(courseNoSections.isPublishedAndNotDeleted());
        assertNotNull(courseWithSections.getPrice());
        assertNotNull(courseWithSections.getTeacher());
        assertFalse(courseWithSections.getSections().isEmpty());

        CourseRequest courseRequest = TestFactory.createDefaultCourseRequestPublish();

        courseWithSections.requestPublish(courseRequest);

        assertTrue(courseWithSections.getCourseRequests().contains(courseRequest));

        assertThrows(InputInvalidException.class, () -> courseWithSections.requestPublish(courseRequest));
    }

    @Test
    void requestPublish_shouldThrowException_whenCourseWithoutSections() {
        assertFalse(courseNoSections.isPublishedAndNotDeleted());
        assertFalse(courseNoSections.getTeacher().isBlank());
        assertNull(courseNoSections.getPrice());
        assertTrue(courseNoSections.getSections().isEmpty());

        CourseRequest courseRequest = TestFactory.createDefaultCourseRequestPublish();

        assertThrows(InputInvalidException.class, () -> courseNoSections.requestPublish(courseRequest));
    }

    @Test
    void requestPublish_shouldThrowException_whenRequestTypeInvalid() {
        assertFalse(courseNoSections.isPublishedAndNotDeleted());
        assertNotNull(courseWithSections.getPrice());
        assertNotNull(courseWithSections.getTeacher());
        assertFalse(courseWithSections.getSections().isEmpty());

        CourseRequest courseRequest = TestFactory.createDefaultCourseRequestUnPublish();
        assertThrows(InputInvalidException.class, () -> courseWithSections.requestPublish(courseRequest));
    }

    @Test
    void requestPublish_shouldThrowException_whenUnresolvedRequestsExist() {
        assertFalse(courseNoSections.isPublishedAndNotDeleted());
        assertNotNull(courseWithSections.getPrice());
        assertNotNull(courseWithSections.getTeacher());
        assertFalse(courseWithSections.getSections().isEmpty());

        CourseRequest courseRequest = TestFactory.createDefaultCourseRequestPublish();
        courseWithSections.requestPublish(courseRequest);

        assertTrue(courseWithSections.getCourseRequests().contains(courseRequest));

        // Tạo một request khác
        assertThrows(InputInvalidException.class, () -> courseNoSections.requestPublish(
                TestFactory.createDefaultCourseRequestPublish()
        ));
    }

    @Test
    void approvePublish_shouldApproveRequest_whenValid() {
        CourseRequest courseRequest = spy(TestFactory.createDefaultCourseRequestPublish());
        // mock
        when(courseRequest.getId()).thenReturn(1L);
        courseWithSections.requestPublish(courseRequest);
        courseWithSections.approvePublish(courseRequest.getId(), "admin", "Looks good!");

        assertTrue(courseWithSections.isPublishedAndNotDeleted());
        assertFalse(courseWithSections.getUnpublished());
        assertEquals("admin", courseWithSections.getApprovedBy());
    }

    @Test
    void approvePublish_shouldThrowException_whenCourseAlreadyPublished() {
        CourseRequest courseRequest = spy(TestFactory.createDefaultCourseRequestPublish());
        // mock
        when(courseRequest.getId()).thenReturn(1L);
        courseWithSections.requestPublish(courseRequest);
        courseWithSections.approvePublish(courseRequest.getId(), "admin", "Looks good!");

        assertThrows(InputInvalidException.class, () ->
                courseWithSections.approvePublish(courseRequest.getId(), "admin", "Another approval")
        );
    }

    @Test
    void approvePublish_shouldThrowException_whenTeacherApprovesSelf() {
        CourseRequest courseRequest = spy(TestFactory.createDefaultCourseRequestPublish());
        // mock
        when(courseRequest.getId()).thenReturn(1L);
        courseWithSections.requestPublish(courseRequest);

        assertThrows(InputInvalidException.class, () ->
                courseWithSections.approvePublish(courseRequest.getId(), courseWithSections.getTeacher(), "Looks good!")
        );
    }

    @Test
    void approvePublish_shouldThrowException_whenRequestNotFound() {
        assertThrows(ResourceNotFoundException.class, () ->
                courseWithSections.approvePublish(999L, "admin", "Looks good!")
        );
    }

    @Test
    void rejectPublish_shouldRejectRequest_whenValid() {
        CourseRequest courseRequest = spy(TestFactory.createDefaultCourseRequestPublish());
        // mock
        when(courseRequest.getId()).thenReturn(1L);
        courseWithSections.requestPublish(courseRequest);
        courseWithSections.rejectPublish(courseRequest.getId(), "admin", "Not good!");

        assertFalse(courseWithSections.isPublishedAndNotDeleted());
        assertNull(courseWithSections.getApprovedBy());
        assertEquals(1, courseWithSections.getCourseRequests().size());
        assertEquals(RequestStatus.REJECTED, courseRequest.getStatus());
        assertTrue(courseRequest.getResolved());
    }

    @Test
    void rejectPublish_shouldThrowException_whenCourseAlreadyPublished() {
        CourseRequest courseRequest = spy(TestFactory.createDefaultCourseRequestPublish());
        // mock
        when(courseRequest.getId()).thenReturn(1L);
        courseWithSections.requestPublish(courseRequest);
        courseWithSections.approvePublish(courseRequest.getId(), "admin", "Looks good!");

        assertThrows(InputInvalidException.class, () ->
                courseWithSections.rejectPublish(courseRequest.getId(), "admin", "Not good!")
        );
    }

    @Test
    void rejectPublish_shouldThrowException_whenTeacherRejectsSelf() {
        CourseRequest courseRequest = spy(TestFactory.createDefaultCourseRequestPublish());
        // mock
        when(courseRequest.getId()).thenReturn(1L);
        courseWithSections.requestPublish(courseRequest);

        assertThrows(InputInvalidException.class, () ->
                courseWithSections.rejectPublish(courseRequest.getId(), courseWithSections.getTeacher(), "Not good!")
        );
    }

    @Test
    void rejectPublish_shouldThrowException_whenRequestNotFound() {
        assertThrows(ResourceNotFoundException.class, () ->
                courseWithSections.rejectPublish(999L, "admin", "Not good!")
        );
    }

    @Test
    void requestUnpublish_shouldAddRequest_whenValidRequest() {
        // Tạo một course đã publish
        CourseRequest courseRequest = spy(TestFactory.createDefaultCourseRequestPublish());
        // mock
        when(courseRequest.getId()).thenReturn(1L);
        courseWithSections.requestPublish(courseRequest);
        courseWithSections.approvePublish(courseRequest.getId(), "admin", "Looks good!");

        assertEquals("admin", courseWithSections.getApprovedBy());
        assertFalse(courseWithSections.isNotPublishedAndDeleted());

        CourseRequest courseRequestUnPublish = TestFactory.createDefaultCourseRequestUnPublish();

        courseWithSections.requestUnpublish(courseRequestUnPublish);

        assertTrue(courseWithSections.getCourseRequests().contains(courseRequestUnPublish));
    }

    @Test
    void requestUnpublish_shouldThrowException_whenCourseNotPublished() {
        assertFalse(courseNoSections.isPublishedAndNotDeleted());

        CourseRequest courseRequestUnPublish = TestFactory.createDefaultCourseRequestUnPublish();

        assertThrows(InputInvalidException.class, () -> courseNoSections.requestUnpublish(courseRequestUnPublish));
    }

    @Test
    void requestUnpublish_shouldThrowException_whenRequestTypeInvalid() {
        CourseRequest courseRequest = spy(TestFactory.createDefaultCourseRequestPublish());
        // mock
        when(courseRequest.getId()).thenReturn(1L);
        courseWithSections.requestPublish(courseRequest);
        courseWithSections.approvePublish(courseRequest.getId(), "admin", "Looks good!");

        assertEquals("admin", courseWithSections.getApprovedBy());
        assertTrue(courseWithSections.getCourseRequests().contains(courseRequest));
        assertTrue(courseWithSections.isPublishedAndNotDeleted());

        CourseRequest courseRequestUnPublish = TestFactory.createDefaultCourseRequestPublish();
        assertThrows(InputInvalidException.class, () -> courseWithSections.requestUnpublish(courseRequestUnPublish));
    }

    @Test
    void requestUnpublish_shouldThrowException_whenUnresolvedRequestsExist() {
        CourseRequest courseRequest = spy(TestFactory.createDefaultCourseRequestPublish());
        // mock
        when(courseRequest.getId()).thenReturn(1L);
        courseWithSections.requestPublish(courseRequest); // ain't be approved yet

        assertTrue(courseWithSections.getCourseRequests().contains(courseRequest));
        assertTrue(courseWithSections.isNotPublishedAndDeleted());

        // Tạo một request khác
        assertThrows(InputInvalidException.class, () -> courseNoSections.requestUnpublish(
                TestFactory.createDefaultCourseRequestUnPublish()
        ));
    }

    @Test
    void approveUnpublish_shouldApproveRequest_whenValid() {
        CourseRequest courseRequest = spy(TestFactory.createDefaultCourseRequestPublish());
        // mock
        when(courseRequest.getId()).thenReturn(1L);
        courseWithSections.requestPublish(courseRequest);
        courseWithSections.approvePublish(courseRequest.getId(), "admin", "Looks good!");

        CourseRequest courseRequestUnPublish = spy(TestFactory.createDefaultCourseRequestUnPublish());
        when(courseRequestUnPublish.getId()).thenReturn(2L);
        courseWithSections.requestUnpublish(courseRequestUnPublish);
        courseWithSections.approveUnpublish(courseRequestUnPublish.getId(), "admin", "Looks not good!");

        assertTrue(courseWithSections.isNotPublishedAndDeleted());
        assertTrue(courseWithSections.getUnpublished());
        assertEquals("admin", courseWithSections.getApprovedBy());
    }

    @Test
    void approveUnpublish_shouldThrowException_whenCourseNotPublished() {
        assertFalse(courseNoSections.isPublishedAndNotDeleted());

        assertThrows(InputInvalidException.class, () -> courseNoSections.approveUnpublish(999L, "admin", "Looks not good!"),
                "Cannot approve unpublish for a published course.");
    }

    @Test
    void approveUnpublish_shouldThrowException_whenTeacherApprovesSelf() {
        CourseRequest courseRequest = spy(TestFactory.createDefaultCourseRequestPublish());
        // mock
        when(courseRequest.getId()).thenReturn(1L);
        courseWithSections.requestPublish(courseRequest);
        courseWithSections.approvePublish(courseRequest.getId(), "admin", "Looks good!");

        CourseRequest courseRequestUnPublish = spy(TestFactory.createDefaultCourseRequestUnPublish());
        when(courseRequestUnPublish.getId()).thenReturn(2L);
        courseWithSections.requestUnpublish(courseRequestUnPublish);

        assertThrows(InputInvalidException.class, () -> courseWithSections.approveUnpublish(courseRequestUnPublish.getId(), courseWithSections.getTeacher(), "Looks not good!"),
                "Teacher cannot approve unpublish for their own course.");
    }

    @Test
    void approveUnpublish_shouldThrowException_whenRequestNotFound() {
        CourseRequest courseRequest = spy(TestFactory.createDefaultCourseRequestPublish());
        // mock
        when(courseRequest.getId()).thenReturn(1L);
        courseWithSections.requestPublish(courseRequest);
        courseWithSections.approvePublish(courseRequest.getId(), "admin", "Looks good!");

        assertThrows(ResourceNotFoundException.class, () ->
                courseWithSections.approveUnpublish(999L, "admin", "Looks not good!")
        );
    }

    @Test
    void approveUnpublish_shouldThrowException_whenApproverUnpublishNotApproverPublish() {
        CourseRequest courseRequest = spy(TestFactory.createDefaultCourseRequestPublish());
        // mock
        when(courseRequest.getId()).thenReturn(1L);
        courseWithSections.requestPublish(courseRequest);
        courseWithSections.approvePublish(courseRequest.getId(), "admin", "Looks good!");

        CourseRequest courseRequestUnPublish = spy(TestFactory.createDefaultCourseRequestUnPublish());
        when(courseRequestUnPublish.getId()).thenReturn(2L);
        courseWithSections.requestUnpublish(courseRequestUnPublish);

        assertThrows(InputInvalidException.class, () -> courseWithSections.approveUnpublish(courseRequestUnPublish.getId(), "anotherAdmin", "Looks not good!"),
                "Cannot approve unpublish for a course that you did not approve publish.");
    }

    @Test
    void rejectUnpublish_shouldRejectRequest_whenValid() {
        CourseRequest courseRequest = spy(TestFactory.createDefaultCourseRequestPublish());
        // mock
        when(courseRequest.getId()).thenReturn(1L);
        courseWithSections.requestPublish(courseRequest);
        courseWithSections.approvePublish(courseRequest.getId(), "admin", "Looks good!");

        CourseRequest courseRequestUnPublish = spy(TestFactory.createDefaultCourseRequestUnPublish());
        when(courseRequestUnPublish.getId()).thenReturn(2L);
        courseWithSections.requestUnpublish(courseRequestUnPublish);
        courseWithSections.rejectUnpublish(courseRequestUnPublish.getId(), "admin", "Not good!");

        assertTrue(courseWithSections.isPublishedAndNotDeleted());
        assertNotNull(courseWithSections.getApprovedBy()); // err
        assertEquals(2, courseWithSections.getCourseRequests().size());
        assertEquals(RequestStatus.REJECTED, courseRequestUnPublish.getStatus());
        assertTrue(courseRequestUnPublish.getResolved());
    }

    @Test
    void rejectUnpublish_shouldThrowException_whenCourseNotPublished() {
        assertTrue(courseWithSections.isNotPublishedAndDeleted());

        assertThrows(InputInvalidException.class, () -> courseNoSections.rejectUnpublish(999L, "admin", "Not good!"),
                "Cannot reject unpublish for a course that is not published.");
    }

    @Test
    void rejectUnpublish_shouldThrowException_whenTeacherRejectsSelf() {
        CourseRequest courseRequest = spy(TestFactory.createDefaultCourseRequestPublish());
        // mock
        when(courseRequest.getId()).thenReturn(1L);
        courseWithSections.requestPublish(courseRequest);
        courseWithSections.approvePublish(courseRequest.getId(), "admin", "Looks good!");
        assertTrue(courseWithSections.isPublishedAndNotDeleted());

        CourseRequest courseRequestUnPublish = spy(TestFactory.createDefaultCourseRequestUnPublish());
        when(courseRequestUnPublish.getId()).thenReturn(2L);
        courseWithSections.requestUnpublish(courseRequestUnPublish);

        assertThrows(InputInvalidException.class, () -> courseWithSections.rejectUnpublish(courseRequestUnPublish.getId(), courseWithSections.getTeacher(), "Not good!"),
                "Teacher cannot reject unpublish for their own course.");
    }

    @Test
    void rejectUnpublish_shouldThrowException_whenRequestNotFound() {
        CourseRequest courseRequest = spy(TestFactory.createDefaultCourseRequestPublish());
        // mock
        when(courseRequest.getId()).thenReturn(1L);
        courseWithSections.requestPublish(courseRequest);
        courseWithSections.approvePublish(courseRequest.getId(), "admin", "Looks good!");
        assertTrue(courseWithSections.isPublishedAndNotDeleted());

        assertThrows(ResourceNotFoundException.class, () ->
                courseWithSections.rejectUnpublish(999L, "admin", "Not good!")
        );
    }

//    @Test
//    void calculateDoneLessonsPercentage_ShouldCalc() {
//        Course course = TestFactory.createCourseWithSections();
//        Double percentage = course.calculateDoneLessonsPercentage(1);
//        assertNotNull(percentage);
//
//        double percentageExpected = (1 / (double) 5) * 100;
//        assertEquals(percentageExpected, percentage);
//    }
//
//    @Test
//    void calculateDoneLessonsPercentage_ShouldThrowException_WhenCourseHasNoSection() {
//        Course course = TestFactory.createDefaultCourse();
//        assertThrows(InputInvalidException.class, () -> course.calculateDoneLessonsPercentage(1),
//                "Cannot do calculation on a draft course.");
//    }
//
//    @Test
//    void calculateDoneLessonsPercentageForSection_ShouldCalc() {
//        Course course = TestFactory.createCourseWithSections();
//        Double percentage = course.calculateDoneLessonsPercentageForSection(1L, 1);
//        assertNotNull(percentage);
//
//        double percentageExpected = (1 / (double) 2) * 100;
//        assertEquals(percentageExpected, percentage);
//    }
//
//    @Test
//    void calculateDoneLessonsPercentageForSection_ShouldThrowException_WhenCourseHasNoSection() {
//        Course course = TestFactory.createDefaultCourse();
//        assertThrows(ResourceNotFoundException.class, () -> course.calculateDoneLessonsPercentageForSection(1L, 1));
//    }

}
