package com.el.course.domain;

import com.el.TestFactory;
import com.el.common.Currencies;
import com.el.common.exception.InputInvalidException;
import com.el.common.exception.ResourceNotFoundException;
import com.el.course.application.dto.QuizCalculationResult;
import org.javamoney.moneta.Money;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import javax.money.MonetaryAmount;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class CourseTests {

    Course courseNoSections;
    Course courseWithSections;

    @BeforeEach
    void setup() {

        courseNoSections = TestFactory.createDefaultCourse();

        // Tạo một course có sections
        CourseSection courseSection = spy(new CourseSection("SectionTitle"));
        when(courseSection.getId()).thenReturn(1000L);
        // Update for Course.java line 282
//        courseSection.addLesson(new Lesson("LessonTitle", Lesson.Type.TEXT, "https://www.example.com", null));

        courseWithSections = TestFactory.createDefaultCourse();
        Lesson lesson = new Lesson("LessonTitle", Lesson.Type.TEXT, "https://www.example.com");
        courseWithSections.addSection(courseSection);
        courseWithSections.addLessonToSection(courseSection.getId(), lesson);

        courseWithSections.changePrice(Money.of(1000, Currencies.VND));
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
        MonetaryAmount newPrice = Money.of(1000000000, Currencies.VND);
        courseWithSections.changePrice(newPrice);
        assertEquals(newPrice, courseWithSections.getPrice());
    }

    @Test
    void changePrice_PriceTooBig_ThrowsException() {
        MonetaryAmount newPrice = Money.of(1000001000, Currencies.VND);
        assertThrows(InputInvalidException.class, () -> courseNoSections.changePrice(newPrice));
    }

    @Test
    void changePrice_USDCurrency_ChangesPrice() {
        MonetaryAmount newPrice = Money.of(100, Currencies.USD);
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
    void addSection_FirstSection_ShouldSetOrderIndexToOne() {
        // Khi add section, thì index của section đó được set là index của section cuối + 1
        // Ví dụ mảng index [1, 2, 3, 4] khi add section thì index của section mới là 5 => mảng index mới [1, 2, 3, 4, 5]
        // Tiếp theo, khi xóa section, không cần phải cập nhật lại index của các section còn lại
        // Ví dụ mảng index [1, 2, 3, 4, 5] khi xóa section có index 3 thì mảng index mới là [1, 2, 4, 5]
        CourseSection section = new CourseSection("SectionTitle");
        courseNoSections.addSection(section);
        assertEquals(1, section.getOrderIndex());
        assertTrue(courseNoSections.getSections().contains(section));
    }

    @Test
    void addSection_SecondSection_ShouldSetOrderIndexToTwo() {
        CourseSection section1 = new CourseSection("SectionTitle1");
        CourseSection section2 = new CourseSection("SectionTitle2");

        courseNoSections.addSection(section1);
        courseNoSections.addSection(section2);

        assertEquals(1, section1.getOrderIndex());
        assertEquals(2, section2.getOrderIndex());
        assertTrue(courseNoSections.getSections().contains(section2));
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
        courseNoSections.addSection(section);

        assertEquals(1, courseNoSections.getSections().size());
        assertEquals(1, section.getOrderIndex());

        courseNoSections.removeSection(1L);

        assertFalse(courseNoSections.getSections().contains(section));
    }

    @Test
    void removeSectionAndAddSection_NoNeedUpdateOrderIndex() {
        CourseSection section1 = spy(new CourseSection("SectionTitle1"));
        CourseSection section2 = spy(new CourseSection("SectionTitle2"));
        when(section1.getId()).thenReturn(1L);
        when(section2.getId()).thenReturn(2L);

        courseNoSections.addSection(section1);
        courseNoSections.addSection(section2);

        assertEquals(1, section1.getOrderIndex());
        assertEquals(2, section2.getOrderIndex());

        // Remove section 1
        courseNoSections.removeSection(1L);

        // Add new section
        CourseSection section3 = new CourseSection("SectionTitle3");
        courseNoSections.addSection(section3);

        // Verify order index of section 2 and section 3
        assertEquals(2, section2.getOrderIndex());
        assertEquals(3, section3.getOrderIndex());
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
    void addLessonToSection_FirstLesson_ShouldSetOrderIndexIsOne() {
        CourseSection section = spy(new CourseSection("SectionTitle"));
        when(section.getId()).thenReturn(1L);
        courseNoSections.addSection(section);

        // Add lesson to section
        // Khi thêm một lesson mới vào section, không có lesson nào trùng title
        Lesson lesson = new Lesson("LessonTitle 1", Lesson.Type.TEXT, "https://www.example.com/1");
        courseNoSections.addLessonToSection(section.getId(), lesson);

        assertEquals(1, lesson.getOrderIndex());

        assertEquals(1, section.getLessons().size());
    }

    @Test
    void addLessonToSection_SecondLesson_ShouldSetOrderIndexIsTwo() {
        CourseSection section = spy(new CourseSection("SectionTitle"));
        when(section.getId()).thenReturn(1L);
        courseNoSections.addSection(section);

        // Add lesson to section
        // Khi thêm một lesson mới vào section, không có lesson nào trùng title
        Lesson lesson1 = new Lesson("LessonTitle 1", Lesson.Type.TEXT, "https://www.example.com/1");
        Lesson lesson2 = new Lesson("LessonTitle 2", Lesson.Type.TEXT, "https://www.example.com/2");
        courseNoSections.addLessonToSection(section.getId(), lesson1);
        courseNoSections.addLessonToSection(section.getId(), lesson2);

        assertEquals(1, lesson1.getOrderIndex());
        assertEquals(2, lesson2.getOrderIndex());

        assertEquals(2, section.getLessons().size());
    }

    @Test
    void removeLessonAndAddLesson_NoNeedUpdateOrderIndex() {
        CourseSection section = spy(new CourseSection("SectionTitle"));
        when(section.getId()).thenReturn(1L);
        courseNoSections.addSection(section);

        // Add lesson to section
        Lesson lesson1 = spy(new Lesson("LessonTitle 1", Lesson.Type.TEXT, "https://www.example.com/1"));
        when(lesson1.getId()).thenReturn(1L);
        Lesson lesson2 = spy(new Lesson("LessonTitle 2", Lesson.Type.TEXT, "https://www.example.com/2"));
        when(lesson2.getId()).thenReturn(2L);
        courseNoSections.addLessonToSection(section.getId(), lesson1);
        courseNoSections.addLessonToSection(section.getId(), lesson2);

        assertEquals(1, lesson1.getOrderIndex());
        assertEquals(2, lesson2.getOrderIndex());

        // Remove lesson 1
        courseNoSections.removeLessonFromSection(1L, 1L);

        // Add new lesson
        Lesson lesson3 = new Lesson("LessonTitle 3", Lesson.Type.TEXT, "https://www.example.com/3");
        courseNoSections.addLessonToSection(section.getId(), lesson3);

        // Verify order index of lesson 2 and lesson 3
        assertEquals(2, lesson2.getOrderIndex());
        assertEquals(3, lesson3.getOrderIndex());
    }

    @Test
    void removeLessonAtTailAndAddLesson_NoNeedUpdateOrderIndex() {
        CourseSection section = spy(new CourseSection("SectionTitle"));
        when(section.getId()).thenReturn(1L);
        courseNoSections.addSection(section);

        // Add lesson to section
        Lesson lesson1 = spy(new Lesson("LessonTitle 1", Lesson.Type.TEXT, "https://www.example.com/1"));
        when(lesson1.getId()).thenReturn(1L);
        Lesson lesson2 = spy(new Lesson("LessonTitle 2", Lesson.Type.TEXT, "https://www.example.com/2"));
        when(lesson2.getId()).thenReturn(2L);

        courseNoSections.addLessonToSection(section.getId(), lesson1);
        courseNoSections.addLessonToSection(section.getId(), lesson2);

        assertEquals(1, lesson1.getOrderIndex());
        assertEquals(2, lesson2.getOrderIndex());

        // Remove lesson 1
        courseNoSections.removeLessonFromSection(1L, 2L);

        // Add new lesson
        Lesson lesson3 = new Lesson("LessonTitle 3", Lesson.Type.TEXT, "https://www.example.com/3");
        courseNoSections.addLessonToSection(section.getId(), lesson3);

        // Verify order index of lesson 2 and lesson 3
        assertEquals(1, lesson1.getOrderIndex());
        assertEquals(2, lesson3.getOrderIndex());
    }

    @Test
    void addLessonToSection_ValidSectionIdButLessonDuplicateTitle_ThrowException() {
        // Issue #90
        CourseSection section = spy(new CourseSection("SectionTitle"));
        when(section.getId()).thenReturn(1L);

        courseNoSections.addSection(section);

        // add lesson to section
        courseNoSections.addLessonToSection(1L, new Lesson("LessonTitle", Lesson.Type.TEXT, "https://www.example.com/1"));

        assertThrows(InputInvalidException.class, () -> courseNoSections
                .addLessonToSection(1L, new Lesson("LessonTitle", Lesson.Type.TEXT, "https://www.example.com/2")));
    }

    @Test
    void addLessonToSection_ValidSectionIdButLessonDuplicateLink_ThrowException() {
        CourseSection section = spy(new CourseSection("SectionTitle"));
        when(section.getId()).thenReturn(1L);

        courseNoSections.addSection(section);

        // Add lesson to section
        section.addLesson(new Lesson("LessonTitle", Lesson.Type.TEXT, "https://www.example.com/1"));

        assertThrows(InputInvalidException.class, () -> courseNoSections
                .addLessonToSection(1L, new Lesson("LessonTitle 2", Lesson.Type.TEXT, "https://www.example.com/1")));
    }

    @Test
    void addLessonToSection_PublishedCourse_ThrowsException() {
        Course course = spy(courseNoSections);
        when(course.isNotPublishedAndDeleted()).thenReturn(false);

        assertThrows(InputInvalidException.class, () -> course.addLessonToSection(1L, new Lesson("LessonTitle", Lesson.Type.TEXT, "https://www.example.com")));
    }

    @Test
    void addLessonToSection_SectionNotFound_ThrowsException() {
        assertThrows(ResourceNotFoundException.class, () -> courseNoSections.addLessonToSection(1L, new Lesson("LessonTitle", Lesson.Type.TEXT, "https://www.example.com")));
    }

    @Test
    void updateLessonInSection_ValidSectionIdLessonIdAndLesson_UpdatesLesson() {
        CourseSection section = spy(new CourseSection("SectionTitle"));
        when(section.getId()).thenReturn(1L);
        courseNoSections.addSection(section);

        // Add lesson to section
        Lesson lesson = spy(new Lesson("LessonTitle", Lesson.Type.TEXT, "https://www.example.com/1"));
        when(lesson.getId()).thenReturn(1L);
        courseNoSections.addLessonToSection(1L, lesson);

        courseNoSections.updateLessonInSection(1L, 1L, new Lesson("UpdatedLessonTitle", Lesson.Type.TEXT, "https://www.example.com/2"));
        assertEquals("UpdatedLessonTitle", section.findLessonById(1L).getTitle());
    }

    @Test
    void updateLessonInSection_ValidSectionIdLessonIdButLessonDuplicateTitle_ThrowException() {
        // Tạo CourseSection và thêm vào courseNoSections
        CourseSection section = spy(new CourseSection("SectionTitle"));
        when(section.getId()).thenReturn(1L);
        courseNoSections.addSection(section);

        // Tạo và thêm một Lesson đầu tiên với title và link cố định
        Lesson lesson1 = spy(new Lesson("LessonTitle", Lesson.Type.TEXT, "https://www.example.com/1"));
        when(lesson1.getId()).thenReturn(1L);
        courseNoSections.addLessonToSection(1L, lesson1);

        // Tạo và thêm một Lesson khác với title hoặc link trùng
        Lesson lesson2 = spy(new Lesson("UpdatedLessonTitle", Lesson.Type.TEXT, "https://www.example.com/2"));
        when(lesson2.getId()).thenReturn(2L);
        courseNoSections.addLessonToSection(1L, lesson2);

        // Thử cập nhật lesson1 với title trùng với lesson2
        Lesson updatedLesson = new Lesson("UpdatedLessonTitle", Lesson.Type.TEXT, "https://www.example.com/3");

        assertThrows(InputInvalidException.class, () ->
                courseNoSections.updateLessonInSection(1L, 1L, updatedLesson)
        );
    }


    @Test
    void updateLessonInSection_ValidSectionIdLessonIdButLessonDuplicateLink_ThrowException() {
        CourseSection section = spy(new CourseSection("SectionTitle"));
        when(section.getId()).thenReturn(1L);
        courseNoSections.addSection(section);

        Lesson lesson1 = spy(new Lesson("LessonTitle 1", Lesson.Type.TEXT, "https://www.example.com/1"));
        when(lesson1.getId()).thenReturn(1L);
        courseNoSections.addLessonToSection(1L, lesson1);

        Lesson lesson2 = spy(new Lesson("LessonTitle 2", Lesson.Type.TEXT, "https://www.example.com/2"));
        when(lesson2.getId()).thenReturn(2L);
        courseNoSections.addLessonToSection(1L, lesson2);

        Lesson updatedLesson = new Lesson("LessonTitle 1", Lesson.Type.TEXT, "https://www.example.com/2");

        assertThrows(InputInvalidException.class, () -> courseNoSections
                .updateLessonInSection(1L, 1L, updatedLesson));
    }


    @Test
    void updateLessonInSection_PublishedCourse_ThrowsException() {
        Course course = spy(courseNoSections);
        when(course.isNotPublishedAndDeleted()).thenReturn(false);

        assertThrows(InputInvalidException.class, () -> course.updateLessonInSection(1L, 1L, new Lesson("UpdatedLessonTitle", Lesson.Type.TEXT, "https://www.example.com")));
    }

    @Test
    void updateLessonInSection_SectionNotFound_ThrowsException() {
        assertThrows(ResourceNotFoundException.class, () -> courseNoSections.updateLessonInSection(1L, 1L, new Lesson("UpdatedLessonTitle", Lesson.Type.TEXT, "https://www.example.com")));
    }

    @Test
    void updateLessonInSection_LessonNotFound_ThrowsException() {
        CourseSection section = spy(new CourseSection("SectionTitle"));
        when(section.getId()).thenReturn(1L);
        courseNoSections.addSection(section);

        Lesson lesson = spy(new Lesson("LessonTitle", Lesson.Type.TEXT, "https://www.example.com/1"));
        when(lesson.getId()).thenReturn(1L);
        courseNoSections.addLessonToSection(1L, lesson);

        assertThrows(ResourceNotFoundException.class, () -> courseNoSections.updateLessonInSection(1L, 2L,
                new Lesson("UpdatedLessonTitle", Lesson.Type.TEXT, "https://www.example.com/2")));
    }

    @Test
    void removeLessonFromSection_ValidSectionIdAndLessonId_RemovesLesson() {
        CourseSection section = spy(new CourseSection("SectionTitle"));
        when(section.getId()).thenReturn(1L);
        courseNoSections.addSection(section);

        Lesson lesson = spy(new Lesson("LessonTitle", Lesson.Type.TEXT, "https://www.example.com"));
        when(lesson.getId()).thenReturn(1L);
        courseNoSections.addLessonToSection(1L, lesson);

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
        courseNoSections.addSection(section);

        Lesson lesson = spy(new Lesson("LessonTitle", Lesson.Type.TEXT, "https://www.example.com"));
        when(lesson.getId()).thenReturn(1L);
        courseNoSections.addLessonToSection(1L, lesson);

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


    @Test
    void addPost_ValidPost_AddsPost() {
        Course coursePublished = spy(TestFactory.createDefaultCourse());
        when(coursePublished.isNotPublishedAndDeleted()).thenReturn(false);
        Post postTemplate = TestFactory.createDefaultPost();

        UserInfo info = postTemplate.getInfo();
        assertEquals("thai", info.firstName());
        assertEquals("nguyen", info.lastName());
        assertTrue(postTemplate.getContent() != null && !postTemplate.getContent().isBlank());

        coursePublished.addPost(postTemplate);
        assertEquals(1, coursePublished.getPosts().size());
        assertEquals(postTemplate, coursePublished.getPosts().iterator().next());
    }

    @Test
    void addPost_UnpublishedCourse_ThrowsException() {
        Post defaultPost = TestFactory.createDefaultPost();

        String message = assertThrows(InputInvalidException.class, () -> courseNoSections.addPost(defaultPost)).getMessage();
        assertEquals("Cannot add a post to an unpublished course.", message);
    }

    @Test
    void updatePost_ValidPost_UpdatesPost() {
        Course coursePublished = spy(TestFactory.createDefaultCourse());
        when(coursePublished.isNotPublishedAndDeleted()).thenReturn(false);
        Post postTemplate = spy(TestFactory.createDefaultPost());
        when(postTemplate.getId()).thenReturn(1L);

        UserInfo info = postTemplate.getInfo();
        assertEquals("thai", info.firstName());
        assertEquals("nguyen", info.lastName());
        assertTrue(postTemplate.getContent() != null && !postTemplate.getContent().isBlank());

        coursePublished.addPost(postTemplate);
        assertEquals(1, coursePublished.getPosts().size());


        coursePublished.updatePost(1L, "Updated content", Set.of("http://example.com/1111", "http://example.com/2/update"));
        assertEquals("Updated content", postTemplate.getContent());
        assertEquals(2, postTemplate.getAttachmentUrls().size());
        assertTrue(postTemplate.getAttachmentUrls().containsAll(Set.of("http://example.com/1111", "http://example.com/2/update")));

        coursePublished.updatePost(1L, "Updated content", null);
        assertEquals("Updated content", postTemplate.getContent());
        assertNull(postTemplate.getAttachmentUrls());
    }

    @Test
    void updatePost_UnpublishedCourse_ThrowsException() {
        String message = assertThrows(InputInvalidException.class, () -> courseNoSections.updatePost(1L,
                "Updated content", Set.of("http://example.com/1", "http://example.com/2"))).getMessage();
        assertEquals("Cannot update a post in an unpublished course.", message);
    }


    @Test
    void updatePost_PostNotFound_ThrowsException() {
        Course coursePublished = spy(TestFactory.createDefaultCourse());
        when(coursePublished.isNotPublishedAndDeleted()).thenReturn(false);

        assertThrows(ResourceNotFoundException.class, () -> coursePublished.updatePost(1L,
                "Updated content", Set.of("http://example.com/1", "http://example.com/2")));
    }

    @Test
    void deletePost_ValidPost_DeletesPost() {
        Course coursePublished = spy(TestFactory.createDefaultCourse());
        when(coursePublished.isNotPublishedAndDeleted()).thenReturn(false);
        Post postTemplate = spy(TestFactory.createDefaultPost());
        when(postTemplate.getId()).thenReturn(1L);

        coursePublished.addPost(postTemplate);
        assertEquals(1, coursePublished.getPosts().size());

        coursePublished.deletePost(1L);
        assertTrue(postTemplate.isDeleted());
    }

    @Test
    void deletePost_UnpublishedCourse_ThrowsException() {
        assertThrows(InputInvalidException.class, () -> courseNoSections.deletePost(1L));
    }

    @Test
    void restorePost_DeletedPost_RestoresPost() {
        Course coursePublished = spy(TestFactory.createDefaultCourse());
        when(coursePublished.isNotPublishedAndDeleted()).thenReturn(false);
        Post postTemplate = spy(TestFactory.createDefaultPost());
        when(postTemplate.getId()).thenReturn(1L);

        coursePublished.addPost(postTemplate);
        assertEquals(1, coursePublished.getPosts().size());

        coursePublished.deletePost(1L);
        assertTrue(postTemplate.isDeleted());

        coursePublished.restorePost(1L);
        assertFalse(postTemplate.isDeleted());
    }

    @Test
    void forceDeletePost_DeletedPost_ForceDeletesPost() {
        Course coursePublished = spy(TestFactory.createDefaultCourse());
        when(coursePublished.isNotPublishedAndDeleted()).thenReturn(false);
        Post postTemplate = spy(TestFactory.createDefaultPost());
        when(postTemplate.getId()).thenReturn(1L);

        coursePublished.addPost(postTemplate);
        assertEquals(1, coursePublished.getPosts().size());

        coursePublished.deletePost(1L);
        assertTrue(postTemplate.isDeleted());

        coursePublished.forceDeletePost(1L);
        assertFalse(coursePublished.getPosts().contains(postTemplate));
    }

    private Course prepareSectionInCourseForQuiz(Quiz quiz) {
        // Arrange
        Course course = spy(TestFactory.createDefaultCourse());

        // Add Section and Mock
        CourseSection section = spy(new CourseSection("SectionTitle"));
        when(section.getId()).thenReturn(1L);
        Lesson lesson = spy(new Lesson("LessonTitle", Lesson.Type.TEXT, "https://www.example.com"));
        when(lesson.getId()).thenReturn(quiz.getAfterLessonId()); // sure quiz.getAfterLessonId contain in lessons of section

        // Add Section to Course
        course.addSection(section);
        course.addLessonToSection(1L, lesson);

        return course;
    }

    @Test
    void addQuizToSection_ValidQuiz_AddsQuiz() {
        // Arrange
        Quiz quiz = TestFactory.createDefaultQuiz(1L);
        Course course = prepareSectionInCourseForQuiz(quiz);

        // Act
        course.addQuizToSection(1L, quiz);
        CourseSection section = course.getSections().iterator().next();
        Quiz addedQuiz = section.getQuizzes().iterator().next();

        // Assert
        assertEquals(1, section.getQuizzes().size());
        assertEquals(quiz.getTitle(), addedQuiz.getTitle());
        assertEquals(quiz.getDescription(), addedQuiz.getDescription());
        assertEquals(quiz.getAfterLessonId(), addedQuiz.getAfterLessonId());
        assertEquals(0, addedQuiz.getQuestions().size());
        assertEquals(quiz.getPassScorePercentage(), addedQuiz.getPassScorePercentage());
        assertEquals(0, addedQuiz.getTotalScore());
    }

    @Test
    void addQuizToSection_QuizAfterLessonNotFound_ThrowsException() {
        // Arrange
        Quiz quiz = new Quiz("Quiz Title", "Quiz Description", 999L, 70); // ID không tồn tại
        Course course = spy(TestFactory.createDefaultCourse());

        // Add Section and Mock
        CourseSection section = spy(new CourseSection("SectionTitle"));
        when(section.getId()).thenReturn(1L);
        Lesson lesson = spy(new Lesson("LessonTitle", Lesson.Type.TEXT, "https://www.example.com"));
        when(lesson.getId()).thenReturn(1L); // sure quiz.getAfterLessonId not contain in lessons of section

        // Add Section to Course
        course.addSection(section);
        course.addLessonToSection(1L, lesson);

        // Act & Assert
        assertThrows(ResourceNotFoundException.class, () -> {
            course.addQuizToSection(1L, quiz);
        }, "Should throw ResourceNotFoundException when lesson afterQuizId not found.");
    }

    @Test
    void addQuizToSection_PublishedCourse_ThrowsException() {
        // Arrange
        Quiz quiz = TestFactory.createDefaultQuiz(1L);
        Course course = prepareSectionInCourseForQuiz(quiz);
        when(course.isNotPublishedAndDeleted()).thenReturn(false);

        // Act & Assert
        String msg = assertThrows(InputInvalidException.class, () -> {
            course.addQuizToSection(1L, quiz);
        }, "Should throw InputInvalidException when trying to add quiz to a published course.").getMessage();

        assertEquals("Cannot add a quiz to a published course.", msg);
    }

    @Test
    void addQuizToSection_ExistingQuizTitle_ThrowsException() {
        // Arrange
        Quiz quiz = TestFactory.createDefaultQuiz(1L);
        Course course = prepareSectionInCourseForQuiz(quiz);

        // Act
        course.addQuizToSection(1L, quiz);
        CourseSection section = course.getSections().iterator().next();

        assertEquals(1, section.getQuizzes().size());

        // Arrange for second quiz
        Lesson lesson = spy(new Lesson("Another lesson", Lesson.Type.TEXT, "https://www.example.com/another-lesson"));
        when(lesson.getId()).thenReturn(2L); // sure quiz.getAfterLessonId contain in lessons of section
        course.addLessonToSection(1L, lesson);

        Quiz anotherQuiz = new Quiz(quiz.getTitle(), "Another quiz description", 2L, 80);

        // Assert
        String msg = assertThrows(InputInvalidException.class, () -> {
            course.addQuizToSection(1L, anotherQuiz);
        }, "Should throw InputInvalidException when trying to add quiz with existing title.").getMessage();

        assertEquals("Duplicate quiz title.", msg);
    }


    @Test
    void addQuizToSection_ExistingQuizAfterLesson_ThrowsException() {
        // Arrange
        Quiz quiz1 = TestFactory.createDefaultQuiz(1L);
        Course course = prepareSectionInCourseForQuiz(quiz1);
        course.addQuizToSection(1L, quiz1); // Thêm quiz đầu tiên

        // Tạo quiz thứ hai với afterLessonId trùng với quiz đầu tiên
        Quiz quiz2 = new Quiz("Another Quiz", "Different Description", quiz1.getAfterLessonId(), 70);

        // Act & Assert
        String msg = assertThrows(InputInvalidException.class, () -> {
            course.addQuizToSection(1L, quiz2);
        }, "Should throw InputInvalidException when trying to add a quiz after a lesson that already has a quiz.").getMessage();

        assertEquals("There is already a quiz after the lesson.", msg);
    }

    @Test
    void updateQuizInSection_ValidQuiz_UpdatesQuiz() {
        // Arrange
        Quiz quiz1 = TestFactory.createDefaultQuiz(1L);
        Course course = prepareSectionInCourseForQuiz(quiz1);
        quiz1 = spy(quiz1);
        when(quiz1.getId()).thenReturn(1L);
        course.addQuizToSection(1L, quiz1);

        CourseSection section = course.getSections().iterator().next();
        assertEquals(1, section.getQuizzes().size());

        // Act
        course.updateQuizInSection(1L, 1L,
                "Updated Quiz Title",
                "Updated Quiz Description",
                80);

        // Assert
        Quiz quizAfterUpdate = section.getQuizzes().iterator().next();

        assertEquals("Updated Quiz Title", quizAfterUpdate.getTitle());
        assertEquals("Updated Quiz Description", quizAfterUpdate.getDescription());
        assertEquals(80, quizAfterUpdate.getPassScorePercentage());
    }

    @Test
    void updateQuizInSection_QuizDuplicateTitle_ThrowsException() {
        // Arrange
        Quiz quiz1 = TestFactory.createDefaultQuiz(1L);
        Course course = prepareSectionInCourseForQuiz(quiz1);
        quiz1 = spy(quiz1);
        when(quiz1.getId()).thenReturn(1L);
        course.addQuizToSection(1L, quiz1);

        CourseSection section = course.getSections().iterator().next();
        assertEquals(1, section.getQuizzes().size());

        // Arrange second lesson and quiz
        Lesson lesson = spy(new Lesson("Another lesson", Lesson.Type.TEXT, "https://www.example.com/another-lesson"));
        when(lesson.getId()).thenReturn(2L); // sure quiz.getAfterLessonId contain in lessons of section
        course.addLessonToSection(1L, lesson);

        // Create second quiz and add it to section with lesson 2
        Quiz quiz2 = new Quiz("Quiz 2", "Different Description", 2L, 70);
        quiz2 = spy(quiz2);
        when(quiz2.getId()).thenReturn(2L);
        course.addQuizToSection(1L, quiz2);

        assertEquals(2, section.getQuizzes().size());

        String quiz1Title = quiz1.getTitle();

        // Act & Assert
        String msg = assertThrows(InputInvalidException.class, () -> course.updateQuizInSection(1L, 2L,
                quiz1Title,
                "Updated Quiz Description",
                80), "Should throw InputInvalidException when trying to update quiz with existing title.").getMessage();

        assertEquals("Duplicate quiz title.", msg);
    }

    @Test
    void updateQuizInSection_QuizIdNotFound_ThrowsException() {
        // Arrange
        Quiz quiz1 = TestFactory.createDefaultQuiz(1L);
        Course course = prepareSectionInCourseForQuiz(quiz1);
        quiz1 = spy(quiz1);
        when(quiz1.getId()).thenReturn(1L);
        course.addQuizToSection(1L, quiz1);

        CourseSection section = course.getSections().iterator().next();
        assertEquals(1, section.getQuizzes().size());

        // Act & Assert
        assertThrows(ResourceNotFoundException.class, () -> {
            course.updateQuizInSection(1L, 999L,
                    "Updated Quiz Title",
                    "Updated Quiz Description",
                    80);
        }, "Should throw ResourceNotFoundException when quiz not found.");
    }

    @Test
    void updateQuizInSection_PublishedCourse_ThrowsException() {
        // Arrange
        Quiz quiz1 = TestFactory.createDefaultQuiz(1L);
        Course course = prepareSectionInCourseForQuiz(quiz1);
        quiz1 = spy(quiz1);
        when(quiz1.getId()).thenReturn(1L);
        course.addQuizToSection(1L, quiz1);

        CourseSection section = course.getSections().iterator().next();
        assertEquals(1, section.getQuizzes().size());

        when(course.isNotPublishedAndDeleted()).thenReturn(false);

        // Act & Assert
        String msg = assertThrows(InputInvalidException.class, () -> {
            course.updateQuizInSection(1L, 1L,
                    "Updated Quiz Title",
                    "Updated Quiz Description",
                    80);
        }, "Should throw InputInvalidException when trying to update quiz in a published course.").getMessage();

        assertEquals("Cannot update a quiz in a published course.", msg);
    }

    @Test
    void deleteQuizFromSection_ValidQuiz_DeletesQuiz() {
        // Arrange
        Quiz quiz1 = TestFactory.createDefaultQuiz(1L);
        Course course = prepareSectionInCourseForQuiz(quiz1);
        quiz1 = spy(quiz1);
        when(quiz1.getId()).thenReturn(1L);
        course.addQuizToSection(1L, quiz1);

        CourseSection section = course.getSections().iterator().next();
        assertEquals(1, section.getQuizzes().size());

        // Act
        course.deleteQuizFromSection(1L, 1L);

        // Assert
        assertTrue(quiz1.isDeleted());
    }

    @Test
    void deleteQuizFromSection_PublishedCourse_ThrowsException() {
        // Arrange
        Quiz quiz1 = TestFactory.createDefaultQuiz(1L);
        Course course = prepareSectionInCourseForQuiz(quiz1);
        quiz1 = spy(quiz1);
        when(quiz1.getId()).thenReturn(1L);
        course.addQuizToSection(1L, quiz1);

        CourseSection section = course.getSections().iterator().next();
        assertEquals(1, section.getQuizzes().size());

        when(course.isNotPublishedAndDeleted()).thenReturn(false);

        // Act & Assert
        String msg = assertThrows(InputInvalidException.class, () -> {
            course.deleteQuizFromSection(1L, 1L);
        }, "Should throw InputInvalidException when trying to delete quiz from a published course.").getMessage();

        assertEquals("Cannot delete a quiz from a published course.", msg);
    }

    @Test
    void restoreQuiz_DeletedQuiz_RestoresQuiz() {
        // Arrange
        Quiz quiz1 = TestFactory.createDefaultQuiz(1L);
        Course course = prepareSectionInCourseForQuiz(quiz1);
        quiz1 = spy(quiz1);
        when(quiz1.getId()).thenReturn(1L);
        course.addQuizToSection(1L, quiz1);

        CourseSection section = course.getSections().iterator().next();
        assertEquals(1, section.getQuizzes().size());

        course.deleteQuizFromSection(1L, 1L);
        assertTrue(quiz1.isDeleted());

        // Act
        course.restoreQuizInSection(1L, 1L);

        // Assert
        assertFalse(quiz1.isDeleted());
    }

    @Test
    void restoreQuiz_PublishedCourse_ThrowsException() {
        // Arrange
        Quiz quiz1 = TestFactory.createDefaultQuiz(1L);
        Course course = prepareSectionInCourseForQuiz(quiz1);
        quiz1 = spy(quiz1);
        when(quiz1.getId()).thenReturn(1L);
        course.addQuizToSection(1L, quiz1);

        CourseSection section = course.getSections().iterator().next();
        assertEquals(1, section.getQuizzes().size());

        course.deleteQuizFromSection(1L, 1L);
        assertTrue(quiz1.isDeleted());

        when(course.isNotPublishedAndDeleted()).thenReturn(false);

        // Act & Assert
        String msg = assertThrows(InputInvalidException.class, () -> {
            course.restoreQuizInSection(1L, 1L);
        }, "Should throw InputInvalidException when trying to restore quiz in a published course.").getMessage();

        assertEquals("Cannot restore a quiz in a published course.", msg);
    }

    @Test
    void forceDeleteQuiz_DeletedQuiz_ForceDeletesQuiz() {
        // Arrange
        Quiz quiz1 = TestFactory.createDefaultQuiz(1L);
        Course course = prepareSectionInCourseForQuiz(quiz1);
        quiz1 = spy(quiz1);
        when(quiz1.getId()).thenReturn(1L);
        course.addQuizToSection(1L, quiz1);

        CourseSection section = course.getSections().iterator().next();
        assertEquals(1, section.getQuizzes().size());

        course.deleteQuizFromSection(1L, 1L);
        assertTrue(quiz1.isDeleted());

        // Act
        course.forceDeleteQuizFromSection(1L, 1L);

        // Assert
        assertFalse(section.getQuizzes().contains(quiz1));
    }

    @Test
    void forceDeleteQuiz_PublishedCourse_ThrowsException() {
        // Arrange
        Quiz quiz1 = TestFactory.createDefaultQuiz(1L);
        Course course = prepareSectionInCourseForQuiz(quiz1);
        quiz1 = spy(quiz1);
        when(quiz1.getId()).thenReturn(1L);
        course.addQuizToSection(1L, quiz1);

        CourseSection section = course.getSections().iterator().next();
        assertEquals(1, section.getQuizzes().size());

        course.deleteQuizFromSection(1L, 1L);
        assertTrue(quiz1.isDeleted());

        when(course.isNotPublishedAndDeleted()).thenReturn(false);

        // Act & Assert
        String msg = assertThrows(InputInvalidException.class, () -> {
            course.forceDeleteQuizFromSection(1L, 1L);
        }, "Should throw InputInvalidException when trying to force delete quiz in a published course.").getMessage();

        assertEquals("Cannot force delete a quiz in a published course.", msg);
    }

    @Test
    void addQuestionToQuiz_ValidQuestion_AddsQuestion() {
        // Arrange
        Quiz quiz = TestFactory.createDefaultQuiz(1L);
        Course course = prepareSectionInCourseForQuiz(quiz);
        quiz = spy(quiz);
        when(quiz.getId()).thenReturn(1L);
        course.addQuizToSection(1L, quiz);

        CourseSection section = course.getSections().iterator().next();
        assertEquals(1, section.getQuizzes().size());

        // Act
        Question question = TestFactory.createDefaultQuestion();
        course.addQuestionToQuizInSection(1L, 1L, question);

        // Assert
        Quiz quizAfterAddQuestion = section.getQuizzes().iterator().next();
        assertEquals(1, quizAfterAddQuestion.getQuestions().size());
        assertEquals(question, quizAfterAddQuestion.getQuestions().iterator().next());
        assertEquals(question.getScore(), quizAfterAddQuestion.getTotalScore());
    }

    @Test
    void addQuestionToQuiz_QuizNotFound_ThrowsException() {
        // Arrange
        Quiz quiz = TestFactory.createDefaultQuiz(1L);
        Course course = prepareSectionInCourseForQuiz(quiz);
        quiz = spy(quiz);
        when(quiz.getId()).thenReturn(1L);
        course.addQuizToSection(1L, quiz);

        CourseSection section = course.getSections().iterator().next();
        assertEquals(1, section.getQuizzes().size());

        // Act & Assert
        assertThrows(ResourceNotFoundException.class, () -> {
            Question question = TestFactory.createDefaultQuestion();
            course.addQuestionToQuizInSection(1L, 999L, question);
        }, "Should throw ResourceNotFoundException when quiz not found.");
    }

    @Test
    void addQuestionToQuiz_PublishedCourse_ThrowsException() {
        // Arrange
        Quiz quiz = TestFactory.createDefaultQuiz(1L);
        Course course = prepareSectionInCourseForQuiz(quiz);
        quiz = spy(quiz);
        when(quiz.getId()).thenReturn(1L);
        course.addQuizToSection(1L, quiz);

        CourseSection section = course.getSections().iterator().next();
        assertEquals(1, section.getQuizzes().size());

        when(course.isNotPublishedAndDeleted()).thenReturn(false);

        // Act & Assert
        String msg = assertThrows(InputInvalidException.class, () -> {
            Question question = TestFactory.createDefaultQuestion();
            course.addQuestionToQuizInSection(1L, 1L, question);
        }, "Should throw InputInvalidException when trying to add question to a published course.").getMessage();

        assertEquals("Cannot add a question to a published course.", msg);
    }

    @Test
    void addQuestionToQuiz_ExistingQuestionContent_ThrowsException() {
        // Arrange
        Quiz quiz = TestFactory.createDefaultQuiz(1L);
        Course course = prepareSectionInCourseForQuiz(quiz);
        quiz = spy(quiz);
        when(quiz.getId()).thenReturn(1L);
        course.addQuizToSection(1L, quiz);

        CourseSection section = course.getSections().iterator().next();
        assertEquals(1, section.getQuizzes().size());

        // Add question
        Question question = TestFactory.createDefaultQuestion();
        course.addQuestionToQuizInSection(1L, 1L, question);

        // Act & Assert
        String msg = assertThrows(InputInvalidException.class, () -> {
            Question anotherQuestion = new Question(question.getContent(), QuestionType.SINGLE_CHOICE, 2,
                    Set.of(new AnswerOption("Answer 1", true),
                            new AnswerOption("Answer 2", false),
                            new AnswerOption("Answer 3", false)), null);

            course.addQuestionToQuizInSection(1L, 1L, anotherQuestion);
        }, "Should throw InputInvalidException when trying to add question with existing content.").getMessage();

        assertEquals("Duplicate question content.", msg);
    }

    @Test
    void addQuestionToQuiz_QuestionScoreInvalid_ThrowsException() {
        // Arrange
        Quiz quiz = TestFactory.createDefaultQuiz(1L);
        Course course = prepareSectionInCourseForQuiz(quiz);
        quiz = spy(quiz);
        when(quiz.getId()).thenReturn(1L);
        course.addQuizToSection(1L, quiz);

        CourseSection section = course.getSections().iterator().next();
        assertEquals(1, section.getQuizzes().size());

        // Act & Assert
        String msgForCase1 = assertThrows(InputInvalidException.class, () -> {
            Question question = new Question("Question content 1", QuestionType.SINGLE_CHOICE, -1,
                    Set.of(new AnswerOption("Answer 1", true),
                            new AnswerOption("Answer 2", false),
                            new AnswerOption("Answer 3", false)), null);

            course.addQuestionToQuizInSection(1L, 1L, question);
        }, "Should throw InputInvalidException when trying to add question with invalid score.").getMessage();

        String msgForCase2 = assertThrows(InputInvalidException.class, () -> {
            Question question = new Question("Question content 2", QuestionType.SINGLE_CHOICE, 6,
                    Set.of(new AnswerOption("Answer 1", true),
                            new AnswerOption("Answer 2", false),
                            new AnswerOption("Answer 3", false)), null);

            course.addQuestionToQuizInSection(1L, 1L, question);
        }, "Should throw InputInvalidException when trying to add question with invalid score.").getMessage();

        assertEquals("Score of a question must be a non-negative number. Maximum score is 5.", msgForCase1);
        assertEquals("Score of a question must be a non-negative number. Maximum score is 5.", msgForCase2);
    }

    @Test
    void addQuestionToQuiz_QuestionSingleChoiceInvalidAnswerOptions_ThrowsException() {
        // Arrange
        Quiz quiz = TestFactory.createDefaultQuiz(1L);
        Course course = prepareSectionInCourseForQuiz(quiz);
        quiz = spy(quiz);
        when(quiz.getId()).thenReturn(1L);
        course.addQuizToSection(1L, quiz);

        CourseSection section = course.getSections().iterator().next();
        assertEquals(1, section.getQuizzes().size());

        // Act & Assert
        String msgForCase1 = assertThrows(InputInvalidException.class, () -> {
            Question question = new Question("Question content 1", QuestionType.SINGLE_CHOICE, 2,
                    Set.of(new AnswerOption("Answer 1", true),
                            new AnswerOption("Answer 2", true),
                            new AnswerOption("Answer 3", false),
                            new AnswerOption("Answer 4", false)), null);

            course.addQuestionToQuizInSection(1L, 1L, question);
        }, "Should throw InputInvalidException when trying to add question single choice but has two correct options.").getMessage();

        String msgForCase2 = assertThrows(InputInvalidException.class, () -> {
            Question question = new Question("Question content 2", QuestionType.SINGLE_CHOICE, 2,
                    Set.of(new AnswerOption("Answer 1", true)), null);

            course.addQuestionToQuizInSection(1L, 1L, question);
        }, "Should throw InputInvalidException when trying to add question with one answer option.").getMessage();

        assertEquals("A single choice question must have exactly 1 correct option.", msgForCase1);
        assertEquals("Question must have at least 2 options.", msgForCase2);
    }

    @Test
    void addQuestionToQuiz_QuestionMultipleChoiceInvalidAnswerOptions_ThrowsException() {
        // Arrange
        Quiz quiz = TestFactory.createDefaultQuiz(1L);
        Course course = prepareSectionInCourseForQuiz(quiz);
        quiz = spy(quiz);
        when(quiz.getId()).thenReturn(1L);
        course.addQuizToSection(1L, quiz);

        CourseSection section = course.getSections().iterator().next();
        assertEquals(1, section.getQuizzes().size());

        // Act & Assert
        String msgForCase1 = assertThrows(InputInvalidException.class, () -> {
            Question question = new Question("Question content 1", QuestionType.MULTIPLE_CHOICE, 2,
                    Set.of(new AnswerOption("Answer 1", false),
                            new AnswerOption("Answer 2", false),
                            new AnswerOption("Answer 3", false),
                            new AnswerOption("Answer 4", false)), null);

            course.addQuestionToQuizInSection(1L, 1L, question);
        }, "Should throw InputInvalidException when trying to add question multiple/single choice but has zero correct option.").getMessage();

        assertEquals("Question must have at least 1 correct option.", msgForCase1);
    }

    @Test
    void addQuestionToQuiz_QuestionTrueFalseInvalidAnswerOptions_ThrowsException() {
        // Arrange
        Quiz quiz = TestFactory.createDefaultQuiz(1L);
        Course course = prepareSectionInCourseForQuiz(quiz);
        quiz = spy(quiz);
        when(quiz.getId()).thenReturn(1L);
        course.addQuizToSection(1L, quiz);

        CourseSection section = course.getSections().iterator().next();
        assertEquals(1, section.getQuizzes().size());

        // Act & Assert
        String msgForCase1 = assertThrows(InputInvalidException.class, () -> {
            Question question = new Question("Question content 1", QuestionType.TRUE_FALSE, 2, null, null);

            course.addQuestionToQuizInSection(1L, 1L, question);
        }, "Should throw InputInvalidException when trying to add question true false but not decide true or false.").getMessage();

        assertEquals("True/False question must define the option is true or false.", msgForCase1);
    }

    @Test
    void updateQuestionInQuiz_ValidQuestion_UpdatesQuestion() {
        // Arrange
        Quiz quiz = TestFactory.createDefaultQuiz(1L);
        Course course = prepareSectionInCourseForQuiz(quiz);
        quiz = spy(quiz);
        when(quiz.getId()).thenReturn(1L);
        course.addQuizToSection(1L, quiz);

        CourseSection section = course.getSections().iterator().next();
        assertEquals(1, section.getQuizzes().size());

        // Add question
        Question question = spy(TestFactory.createDefaultQuestion());
        when(question.getId()).thenReturn(1L);
        course.addQuestionToQuizInSection(1L, 1L, question);

        assertEquals(quiz.getTotalScore(), question.getScore());

        // Act
        Question updatedQuestionContent = new Question("Updated Question Content", QuestionType.MULTIPLE_CHOICE, 5,
                Set.of(new AnswerOption("Answer 1", true),
                        new AnswerOption("Answer 2", false),
                        new AnswerOption("Answer 3", true),
                        new AnswerOption("Answer 4", false),
                        new AnswerOption("Answer 5", false)), null);
        course.updateQuestionInQuizInSection(1L, 1L, question.getId(), updatedQuestionContent);

        // Assert
        Quiz quizAfterUpdate = section.getQuizzes().iterator().next();
        Question updatedQuestion = quizAfterUpdate.getQuestions().iterator().next();

        assertEquals(quiz.getTotalScore(), 5);
        assertEquals("Updated Question Content", updatedQuestion.getContent());
        assertEquals(QuestionType.MULTIPLE_CHOICE, updatedQuestion.getType());
        assertEquals(5, updatedQuestion.getScore());
        assertEquals(5, updatedQuestion.getOptions().size());
        assertEquals(updatedQuestion.getScore(), quizAfterUpdate.getTotalScore());
    }

    @Test
    void updateQuestionInQuiz_QuestionNotFound_ThrowsException() {
        // Arrange
        Quiz quiz = TestFactory.createDefaultQuiz(1L);
        Course course = prepareSectionInCourseForQuiz(quiz);
        quiz = spy(quiz);
        when(quiz.getId()).thenReturn(1L);
        course.addQuizToSection(1L, quiz);

        CourseSection section = course.getSections().iterator().next();
        assertEquals(1, section.getQuizzes().size());

        // Add question
        Question question = spy(TestFactory.createDefaultQuestion());
        when(question.getId()).thenReturn(1L);
        course.addQuestionToQuizInSection(1L, 1L, question);

        assertEquals(quiz.getTotalScore(), question.getScore());

        // Act & Assert
        assertThrows(ResourceNotFoundException.class, () -> {
            Question updatedQuestionContent = new Question("Updated Question Content", QuestionType.MULTIPLE_CHOICE, 5,
                    Set.of(new AnswerOption("Answer 1", true),
                            new AnswerOption("Answer 2", false),
                            new AnswerOption("Answer 3", true),
                            new AnswerOption("Answer 4", false),
                            new AnswerOption("Answer 5", false)), null);
            course.updateQuestionInQuizInSection(1L, 1L, 999L, updatedQuestionContent);
        }, "Should throw ResourceNotFoundException when question not found.");

        assertEquals(quiz.getTotalScore(), question.getScore());
    }

    @Test
    void updateQuestionInQuiz_PublishedCourse_ThrowsException() {
        // Arrange
        Quiz quiz = TestFactory.createDefaultQuiz(1L);
        Course course = prepareSectionInCourseForQuiz(quiz);
        quiz = spy(quiz);
        when(quiz.getId()).thenReturn(1L);
        course.addQuizToSection(1L, quiz);

        CourseSection section = course.getSections().iterator().next();
        assertEquals(1, section.getQuizzes().size());

        // Add question
        Question question = spy(TestFactory.createDefaultQuestion());
        when(question.getId()).thenReturn(1L);
        course.addQuestionToQuizInSection(1L, 1L, question);

        when(course.isNotPublishedAndDeleted()).thenReturn(false);

        // Act & Assert
        String msg = assertThrows(InputInvalidException.class, () -> {
            Question updatedQuestionContent = new Question("Updated Question Content", QuestionType.MULTIPLE_CHOICE, 5,
                    Set.of(new AnswerOption("Answer 1", true),
                            new AnswerOption("Answer 2", false),
                            new AnswerOption("Answer 3", true),
                            new AnswerOption("Answer 4", false),
                            new AnswerOption("Answer 5", false)), null);
            course.updateQuestionInQuizInSection(1L, 1L, question.getId(), updatedQuestionContent);
        }, "Should throw InputInvalidException when trying to update question in a published course.").getMessage();

        assertEquals("Cannot update a question in a published course.", msg);
    }

    @Test
    void updateQuestionInQuiz_ExistingQuestionContent_ThrowsException() {
        // Arrange
        Quiz quiz = TestFactory.createDefaultQuiz(1L);
        Course course = prepareSectionInCourseForQuiz(quiz);
        quiz = spy(quiz);
        when(quiz.getId()).thenReturn(1L);
        course.addQuizToSection(1L, quiz);

        CourseSection section = course.getSections().iterator().next();
        assertEquals(1, section.getQuizzes().size());

        // Add question
        Question question = spy(TestFactory.createDefaultQuestion());
        when(question.getId()).thenReturn(1L);
        course.addQuestionToQuizInSection(1L, 1L, question);

        assertEquals(quiz.getTotalScore(), question.getScore());

        // Add another question
        Question anotherQuestion = spy(TestFactory.createDefaultQuestion());
        when(anotherQuestion.getId()).thenReturn(2L);
        course.addQuestionToQuizInSection(1L, 1L, anotherQuestion);

        assertEquals(quiz.getTotalScore(), anotherQuestion.getScore() + question.getScore());

        // Act & Assert
        String msg = assertThrows(InputInvalidException.class, () -> {
            Question updatedQuestionContent = new Question(anotherQuestion.getContent(), QuestionType.MULTIPLE_CHOICE, 5,
                    Set.of(new AnswerOption("Answer 1", true),
                            new AnswerOption("Answer 2", false),
                            new AnswerOption("Answer 3", true),
                            new AnswerOption("Answer 4", false),
                            new AnswerOption("Answer 5", false)), null);
            course.updateQuestionInQuizInSection(1L, 1L, question.getId(), updatedQuestionContent);
        }, "Should throw InputInvalidException when trying to update question with existing content.").getMessage();

        assertEquals("Duplicate question content.", msg);
    }

    @Test
    void deleteQuestionFromQuiz_ValidQuestion_DeletesQuestion() {
        // Arrange
        Quiz quiz = TestFactory.createDefaultQuiz(1L);
        Course course = prepareSectionInCourseForQuiz(quiz);
        quiz = spy(quiz);
        when(quiz.getId()).thenReturn(1L);
        course.addQuizToSection(1L, quiz);

        CourseSection section = course.getSections().iterator().next();
        assertEquals(1, section.getQuizzes().size());

        // Add question
        Question question = spy(TestFactory.createDefaultQuestion());
        when(question.getId()).thenReturn(1L);
        course.addQuestionToQuizInSection(1L, 1L, question);

        assertEquals(quiz.getTotalScore(), question.getScore());

        // Act
        course.deleteQuestionFromQuizInSection(1L, 1L, question.getId());

        // Assert
        assertTrue(quiz.getQuestions().isEmpty());
        assertEquals(0, quiz.getTotalScore());
    }

    private Course createCourseWithQuizDefault() {
        Course course = TestFactory.createDefaultCourse();
        CourseSection section = spy(new CourseSection("SectionTitle"));
        when(section.getId()).thenReturn(1L);
        Lesson lesson = spy(new Lesson("LessonTitle", Lesson.Type.TEXT, "https://www.example.com"));
        when(lesson.getId()).thenReturn(1L);

        // Add Section to Course
        course.addSection(section);
        course.addLessonToSection(1L, lesson);

        /**
         * sectionId: 1,
         * lessonId: 1,
         * quizId: 1,
         * */
        Quiz quiz = spy(TestFactory.createDefaultQuiz(1L));
        when(quiz.getAfterLessonId()).thenReturn(1L);
        when(quiz.getId()).thenReturn(1L);
        course.addQuizToSection(1L, quiz);

        // prepare questions
        AnswerOption answerOption1 = spyAnswerOption(new AnswerOption("Answer 1", true), 1);
        AnswerOption answerOption2 = spyAnswerOption(new AnswerOption("Answer 2", false), 2);
        AnswerOption answerOption3 = spyAnswerOption(new AnswerOption("Answer 3", false), 3);
        Question question1 = spy(new Question("Question 1", QuestionType.SINGLE_CHOICE, 2, Set.of(answerOption1, answerOption2, answerOption3), null));
        when(question1.getId()).thenReturn(1L);
        course.addQuestionToQuizInSection(1L, 1L, question1);

        Question question2 = spy(new Question("Question 2", QuestionType.TRUE_FALSE, 3, null, true));
        when(question2.getId()).thenReturn(2L);
        course.addQuestionToQuizInSection(1L, 1L, question2);

        AnswerOption answerOption6 = spyAnswerOption(new AnswerOption("Answer 1", true), 6);
        AnswerOption answerOption7 = spyAnswerOption(new AnswerOption("Answer 2", false), 7);
        AnswerOption answerOption8 = spyAnswerOption(new AnswerOption("Answer 3", true), 8);
        Question question3 = spy(new Question("Question 3", QuestionType.MULTIPLE_CHOICE, 4, Set.of(answerOption6, answerOption7, answerOption8), null));
        when(question3.getId()).thenReturn(3L);
        course.addQuestionToQuizInSection(1L, 1L, question3);

        return course;
    }

    @Test
    void testCalculateQuiz() {
        Course course = createCourseWithQuizDefault();

        // calculate quiz
        QuizCalculationResult result = course.calculateQuiz(1L, new HashMap<>(
                Map.of(
                        1L, Set.of(1L),
                        2L, true,
                        3L, Set.of(6L, 7L)
                )
        ));
        assertEquals(7, result.score());
        assertTrue(result.passed());
    }

    @Test
    void testCalculateQuizButInputAnswerInvalid() {
        Course course = createCourseWithQuizDefault();

        String msgCase1 = assertThrows(InputInvalidException.class, () -> {
            course.calculateQuiz(1L, new HashMap<>(
                    Map.of(
                            1L, Set.of(1L, 2L), // question with id = 1 is single choice, but input answer has 2 answer
                            2L, true,
                            3L, Set.of(6L, 7L, 8L)
                    )
            ));
        }).getMessage();

        String msgCase2 = assertThrows(InputInvalidException.class, () -> {
            course.calculateQuiz(1L, new HashMap<>(
                    Map.of(
                            1L, Set.of(1L),
                            2L, Set.of(4L, 5L), // question with id = 2 is true/false, but input answer is not boolean
                            3L, Set.of(6L, 7L, 8L)
                    )
            ));
        }).getMessage();

        assertEquals("Quiz calculation error: Single choice question must have exactly one answer.", msgCase1);
        assertEquals("Quiz calculation error: User answer for true/false must be boolean type.", msgCase2);
    }

    AnswerOption spyAnswerOption(AnswerOption answerOption, long id) {
        AnswerOption spyAnswerOption = spy(answerOption);
        when(spyAnswerOption.getId()).thenReturn(id);
        return spyAnswerOption;
    }

}
