package com.elearning.course.domain;

import com.elearning.common.exception.InputInvalidException;
import com.elearning.common.exception.ResourceNotFoundException;
import org.javamoney.moneta.Money;
import org.junit.Assert;
import org.junit.jupiter.api.Test;

import javax.money.MonetaryAmount;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class CourseTests {

    @Test
    public void courseConstructor_ValidInput_CreatesCourse() {
        Set<String> benefits = new HashSet<>(Arrays.asList("Benefit1", "Benefit2"));
        Set<String> prerequisites = new HashSet<>(Arrays.asList("Prerequisite1", "Prerequisite2"));
        Set<Language> subtitles = new HashSet<>(Arrays.asList(Language.ENGLISH, Language.SPANISH));

        Course course = new Course("Title", "Description", "ThumbnailUrl", benefits, Language.ENGLISH, prerequisites, subtitles, "Teacher");

        assertEquals("Title", course.getTitle());
        assertEquals("Description", course.getDescription());
        assertEquals("ThumbnailUrl", course.getThumbnailUrl());
        assertEquals(Language.ENGLISH, course.getLanguage());
        assertEquals(benefits, course.getBenefits());
        assertEquals(prerequisites, course.getPrerequisites());
        assertEquals(subtitles, course.getSubtitles());
        assertEquals("Teacher", course.getTeacher());
        assertFalse(course.isDeleted());
        assertFalse(course.getPublished());
    }

    @Test
    public void courseConstructor_EmptyTitle_ThrowsException() {
        Set<String> benefits = new HashSet<>(Arrays.asList("Benefit1", "Benefit2"));
        Set<String> prerequisites = new HashSet<>(Arrays.asList("Prerequisite1", "Prerequisite2"));
        Set<Language> subtitles = new HashSet<>(Arrays.asList(Language.ENGLISH, Language.SPANISH));
        assertThrows(IllegalArgumentException.class, () -> new Course("", "Description", "ThumbnailUrl", benefits, Language.ENGLISH, prerequisites, subtitles, "Teacher"));
    }

    @Test
    public void courseConstructor_NullTitle_ThrowsException() {
        Set<String> benefits = new HashSet<>(Arrays.asList("Benefit1", "Benefit2"));
        Set<String> prerequisites = new HashSet<>(Arrays.asList("Prerequisite1", "Prerequisite2"));
        Set<Language> subtitles = new HashSet<>(Arrays.asList(Language.ENGLISH, Language.SPANISH));

        assertThrows(IllegalArgumentException.class, () -> new Course(null, "Description", "ThumbnailUrl", benefits, Language.ENGLISH, prerequisites, subtitles, "Teacher"));
    }

    // Update course use case
    @Test
    public void updateInfo_ValidInput_UpdatesCourseInfo() {
        Set<String> benefits = new HashSet<>(Arrays.asList("Benefit1", "Benefit2"));
        Set<String> prerequisites = new HashSet<>(Arrays.asList("Prerequisite1", "Prerequisite2"));
        Set<Language> subtitles = new HashSet<>(Arrays.asList(Language.ENGLISH, Language.SPANISH));
        Course course = new Course("Title", "Description", "ThumbnailUrl", benefits, Language.ENGLISH, prerequisites, subtitles, "Teacher");

        Set<String> newBenefits = new HashSet<>(Arrays.asList("NewBenefit1", "NewBenefit2"));
        Set<String> newPrerequisites = new HashSet<>(Arrays.asList("NewPrerequisite1", "NewPrerequisite2"));
        Set<Language> newSubtitles = new HashSet<>(Arrays.asList(Language.FRENCH, Language.GERMAN));
        course.updateInfo("NewTitle", "NewDescription", "NewThumbnailUrl", newBenefits, newPrerequisites, newSubtitles);

        assertEquals("NewTitle", course.getTitle());
        assertEquals("NewDescription", course.getDescription());
        assertEquals("NewThumbnailUrl", course.getThumbnailUrl());
        assertEquals(newBenefits, course.getBenefits());
        assertEquals(newPrerequisites, course.getPrerequisites());
        assertEquals(newSubtitles, course.getSubtitles());
    }

    @Test
    public void updateInfo_EmptyTitle_ThrowsException() {
        Set<String> benefits = new HashSet<>(Arrays.asList("Benefit1", "Benefit2"));
        Set<String> prerequisites = new HashSet<>(Arrays.asList("Prerequisite1", "Prerequisite2"));
        Set<Language> subtitles = new HashSet<>(Arrays.asList(Language.ENGLISH, Language.SPANISH));
        Course course = new Course("Title", "Description", "ThumbnailUrl", benefits, Language.ENGLISH, prerequisites, subtitles, "Teacher");

        Set<String> newBenefits = new HashSet<>(Arrays.asList("NewBenefit1", "NewBenefit2"));
        Set<String> newPrerequisites = new HashSet<>(Arrays.asList("NewPrerequisite1", "NewPrerequisite2"));
        Set<Language> newSubtitles = new HashSet<>(Arrays.asList(Language.FRENCH, Language.GERMAN));
        assertThrows(IllegalArgumentException.class, () -> course.updateInfo("", "NewDescription", "NewThumbnailUrl", newBenefits, newPrerequisites, newSubtitles));
    }

    @Test
    public void updateInfo_NullTitle_ThrowsException() {
        Set<String> benefits = new HashSet<>(Arrays.asList("Benefit1", "Benefit2"));
        Set<String> prerequisites = new HashSet<>(Arrays.asList("Prerequisite1", "Prerequisite2"));
        Set<Language> subtitles = new HashSet<>(Arrays.asList(Language.ENGLISH, Language.SPANISH));
        Course course = new Course("Title", "Description", "ThumbnailUrl", benefits, Language.ENGLISH, prerequisites, subtitles, "Teacher");

        Set<String> newBenefits = new HashSet<>(Arrays.asList("NewBenefit1", "NewBenefit2"));
        Set<String> newPrerequisites = new HashSet<>(Arrays.asList("NewPrerequisite1", "NewPrerequisite2"));
        Set<Language> newSubtitles = new HashSet<>(Arrays.asList(Language.FRENCH, Language.GERMAN));
        assertThrows(IllegalArgumentException.class, () -> course.updateInfo(null, "NewDescription", "NewThumbnailUrl", newBenefits, newPrerequisites, newSubtitles));
    }

    @Test
    public void updateInfo_PublishedCourse_ThrowsException() {
        // Spy đối tượng Course để dùng logic thực
        Set<String> benefits = new HashSet<>(Arrays.asList("Benefit1", "Benefit2"));
        Set<String> prerequisites = new HashSet<>(Arrays.asList("Prerequisite1", "Prerequisite2"));
        Set<Language> subtitles = new HashSet<>(Arrays.asList(Language.ENGLISH, Language.SPANISH));

        Course course = spy(new Course(
                "Title",
                "Description",
                "ThumbnailUrl",
                benefits,
                Language.ENGLISH,
                prerequisites,
                subtitles,
                "Teacher"
        ));

        // Giả lập trạng thái đã publish bằng cách mock canEdit trả về false
        when(course.canEdit()).thenReturn(false);

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
        Course course = new Course("Title", "Description", "ThumbnailUrl", new HashSet<>(), Language.ENGLISH, new HashSet<>(), new HashSet<>(), "Teacher");
        course.delete();
        assertTrue(course.isDeleted());
    }

    @Test
    public void delete_AlreadyDeletedCourse_ThrowsException() {
        Course course = new Course("Title", "Description", "ThumbnailUrl", new HashSet<>(), Language.ENGLISH, new HashSet<>(), new HashSet<>(), "Teacher");
        course.delete();
        assertThrows(InputInvalidException.class, course::delete);
    }

    @Test
    void delete_PublishedCourse_ThrowsException() {
        Course courseMock = spy(new Course("Title", "Description", "ThumbnailUrl", new HashSet<>(), Language.ENGLISH, new HashSet<>(), new HashSet<>(), "Teacher"));
        when(courseMock.canEdit()).thenReturn(false);
        assertThrows(InputInvalidException.class, courseMock::delete);
    }

    @Test
    void changePrice_ValidPrice_ChangesPrice() {
        Course course = new Course("Title", "Description", "ThumbnailUrl", new HashSet<>(), Language.ENGLISH, new HashSet<>(), new HashSet<>(), "Teacher");
        MonetaryAmount newPrice = Money.of(100, "USD");
        course.changePrice(newPrice);
        assertEquals(newPrice, course.getPrice());
    }

    @Test
    void changePrice_NegativePrice_ThrowsException() {
        Course course = new Course("Title", "Description", "ThumbnailUrl", new HashSet<>(), Language.ENGLISH, new HashSet<>(), new HashSet<>(), "Teacher");
        MonetaryAmount negativePrice = Money.of(-100, "USD");
        assertThrows(InputInvalidException.class, () -> course.changePrice(negativePrice));
    }

    @Test
    void changePrice_PublishedCourse_ThrowsException() {
        Course course = spy(new Course("Title", "Description", "ThumbnailUrl", new HashSet<>(), Language.ENGLISH, new HashSet<>(), new HashSet<>(), "Teacher"));
        when(course.canEdit()).thenReturn(false);
        MonetaryAmount newPrice = Money.of(100, "USD");
        assertThrows(InputInvalidException.class, () -> course.changePrice(newPrice));
    }

    @Test
    void assignTeacher_ValidTeacher_AssignsTeacher() {
        Course course = new Course("Title", "Description", "ThumbnailUrl", new HashSet<>(), Language.ENGLISH, new HashSet<>(), new HashSet<>(), "OldTeacher");
        course.assignTeacher("NewTeacher");
        assertEquals("NewTeacher", course.getTeacher());
    }

    @Test
    void assignTeacher_NullTeacher_ThrowsException() {
        Course course = new Course("Title", "Description", "ThumbnailUrl", new HashSet<>(), Language.ENGLISH, new HashSet<>(), new HashSet<>(), "OldTeacher");
        assertThrows(NullPointerException.class, () -> course.assignTeacher(null));
    }

    @Test
    void assignTeacher_PublishedCourse_ThrowsException() {
        Course course = spy(new Course("Title", "Description", "ThumbnailUrl", new HashSet<>(), Language.ENGLISH, new HashSet<>(), new HashSet<>(), "OldTeacher"));
        when(course.canEdit()).thenReturn(false);
        assertThrows(InputInvalidException.class, () -> course.assignTeacher("NewTeacher"));
    }

    @Test
    void applyDiscount_ValidDiscount_AppliesDiscount() {
        Course course = new Course("Title", "Description", "ThumbnailUrl", new HashSet<>(), Language.ENGLISH, new HashSet<>(), new HashSet<>(), "Teacher");
        MonetaryAmount price = Money.of(200, "USD");
        MonetaryAmount discountedPrice = Money.of(150, "USD");
        course.changePrice(price);
        String discountCode = "25OFF";
        course.applyDiscount(discountedPrice, discountCode);
        assertEquals(Money.of(50, "USD"), course.getDiscountedPrice());
        assertEquals(discountCode, course.getDiscountCode());
    }

    @Test
    void applyDiscount_NullDiscountedPrice_ThrowsException() {
        Course course = new Course("Title", "Description", "ThumbnailUrl", new HashSet<>(), Language.ENGLISH, new HashSet<>(), new HashSet<>(), "Teacher");
        MonetaryAmount price = Money.of(200, "USD");
        String discountCode = "25OFF";
        course.changePrice(price);
        assertThrows(NullPointerException.class, () -> course.applyDiscount(null, discountCode));
    }

    @Test
    void addSection_ValidSection_AddsSection() {
        Course course = new Course("Title", "Description", "ThumbnailUrl", new HashSet<>(), Language.ENGLISH, new HashSet<>(), new HashSet<>(), "Teacher");
        CourseSection section = new CourseSection("SectionTitle");
        section.addLesson(new Lesson("LessonTitle", Lesson.Type.TEXT, "https://www.example.com", null));
        course.addSection(section);
        assertTrue(course.getSections().contains(section));
    }

    @Test
    void addSection_NullSection_ThrowsException() {
        Course course = new Course("Title", "Description", "ThumbnailUrl", new HashSet<>(), Language.ENGLISH, new HashSet<>(), new HashSet<>(), "Teacher");
        assertThrows(NullPointerException.class, () -> course.addSection(null));
    }

    @Test
    void addSection_SectionWithSameTitle_ThrowsException() {
        Course course = new Course("Title", "Description", "ThumbnailUrl", new HashSet<>(), Language.ENGLISH, new HashSet<>(), new HashSet<>(), "Teacher");
        CourseSection section = new CourseSection("SectionTitle");
        section.addLesson(new Lesson("LessonTitle", Lesson.Type.TEXT, "https://www.example.com", null));
        course.addSection(section);

        // Tạo một section khác có cùng title với section đã thêm vào course
        CourseSection duplicateSection = new CourseSection("SectionTitle");
        assertThrows(InputInvalidException.class, () -> course.addSection(duplicateSection));
    }

    @Test
    void addSection_SectionWithoutLessons_ThrowsException() {
        Course course = new Course("Title", "Description", "ThumbnailUrl", new HashSet<>(), Language.ENGLISH, new HashSet<>(), new HashSet<>(), "Teacher");
        CourseSection section = new CourseSection("SectionTitle");
        assertThrows(InputInvalidException.class, () -> course.addSection(section));
    }

    @Test
    void addSection_PublishedCourse_ThrowsException() {
        Course course = spy(new Course("Title", "Description", "ThumbnailUrl", new HashSet<>(), Language.ENGLISH, new HashSet<>(), new HashSet<>(), "Teacher"));
        when(course.canEdit()).thenReturn(false); // Giả lập khóa học đã publish

        CourseSection section = new CourseSection("SectionTitle");
        assertThrows(InputInvalidException.class, () -> course.addSection(section));
    }

    @Test
    void testUpdateSection_ShouldUpdateSectionTitle() {
        Course course = new Course("Title", "Description", "ThumbnailUrl", new HashSet<>(), Language.ENGLISH, new HashSet<>(), new HashSet<>(), "Teacher");
        CourseSection section = spy(new CourseSection("SectionTitle"));
        // Mock id của section
        when(section.getId()).thenReturn(1L);
        section.addLesson(new Lesson("LessonTitle", Lesson.Type.TEXT, "https://www.example.com", null));
        course.addSection(section);
        assertTrue(course.getSections().contains(section));

        // update section title
        course.updateSection(section.getId(), "NewSectionTitle");

        // verify section title is updated
        verify(section).updateInfo("NewSectionTitle");
        assertEquals("NewSectionTitle", section.getTitle());
    }

    @Test
    void testUpdateSection_SectionWithSameTitle_ThrowsException() {
        Course course = new Course("Title", "Description", "ThumbnailUrl", new HashSet<>(), Language.ENGLISH, new HashSet<>(), new HashSet<>(), "Teacher");
        CourseSection section = spy(new CourseSection("SectionTitle"));
        // Mock id của section
        when(section.getId()).thenReturn(1L);
        section.addLesson(new Lesson("LessonTitle", Lesson.Type.TEXT, "https://www.example.com", null));
        course.addSection(section);
        assertTrue(course.getSections().contains(section));

        // Tạo một section khác có cùng title với section đã thêm vào course
        CourseSection duplicateSection = new CourseSection("SectionTitle");
        assertThrows(InputInvalidException.class, () -> course.addSection(duplicateSection));
    }

    @Test
    void testUpdateSection_PublishedCourse_ThrowsException() {
        Course course = spy(new Course("Title", "Description", "ThumbnailUrl", new HashSet<>(), Language.ENGLISH, new HashSet<>(), new HashSet<>(), "Teacher"));
        when(course.canEdit()).thenReturn(false); // Giả lập khóa học đã publish

        CourseSection section = new CourseSection("SectionTitle");
        assertThrows(InputInvalidException.class, () -> course.addSection(section));
    }

    @Test
    void testUpdateSection_SectionNotFound_ThrowsException() {
        Course course = new Course("Title", "Description", "ThumbnailUrl", new HashSet<>(), Language.ENGLISH, new HashSet<>(), new HashSet<>(), "Teacher");
        assertThrows(ResourceNotFoundException.class, () -> course.updateSection(1L, "NewSectionTitle"));
    }


}
