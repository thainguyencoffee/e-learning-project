package com.elearning.course.domain;

import com.elearning.common.Currencies;
import com.elearning.common.exception.InputInvalidException;
import com.elearning.common.exception.ResourceNotFoundException;
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
        Set<String> benefits = new HashSet<>(Arrays.asList("Benefit1", "Benefit2"));
        Set<String> prerequisites = new HashSet<>(Arrays.asList("Prerequisite1", "Prerequisite2"));
        Set<Language> subtitles = new HashSet<>(Arrays.asList(Language.ENGLISH, Language.SPANISH));

        courseNoSections = new Course("Title", "Description", "ThumbnailUrl", benefits, Language.ENGLISH, prerequisites, subtitles, "Teacher");

        // Tạo một course có sections
        CourseSection courseSection = new CourseSection("SectionTitle");
        courseSection.addLesson(new Lesson("LessonTitle", Lesson.Type.TEXT, "https://www.example.com", null));

        courseWithSections = new Course("Title", "Description", "ThumbnailUrl", benefits, Language.ENGLISH, prerequisites, subtitles, "Teacher");
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
        assertThrows(IllegalArgumentException.class, () -> courseNoSections.updateInfo("", "NewDescription", "NewThumbnailUrl", newBenefits, newPrerequisites, newSubtitles));
    }


    @Test
    public void updateInfo_NullTitle_ThrowsException() {

        Set<String> newBenefits = new HashSet<>(Arrays.asList("NewBenefit1", "NewBenefit2"));
        Set<String> newPrerequisites = new HashSet<>(Arrays.asList("NewPrerequisite1", "NewPrerequisite2"));
        Set<Language> newSubtitles = new HashSet<>(Arrays.asList(Language.FRENCH, Language.GERMAN));
        assertThrows(IllegalArgumentException.class, () -> courseNoSections.updateInfo(null, "NewDescription", "NewThumbnailUrl", newBenefits, newPrerequisites, newSubtitles));
    }

    @Test
    public void updateInfo_PublishedCourse_ThrowsException() {
        // Spy đối tượng Course để dùng logic thực
        Course course = spy(courseNoSections);

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
        when(courseMock.canEdit()).thenReturn(false);
        assertThrows(InputInvalidException.class, courseMock::delete);
    }

    @Test
    void changePrice_ValidPrice_ChangesPrice() {
        MonetaryAmount newPrice = Money.of(100, Currencies.VND);
        courseNoSections.changePrice(newPrice);
        assertEquals(newPrice, courseNoSections.getPrice());
    }

    @Test
    void changePrice_NegativePrice_ThrowsException() {
        MonetaryAmount negativePrice = Money.of(-100, Currencies.VND);
        assertThrows(InputInvalidException.class, () -> courseNoSections.changePrice(negativePrice));
    }

    @Test
    void changePrice_PublishedCourse_ThrowsException() {
        Course course = spy(courseNoSections);
        when(course.canEdit()).thenReturn(false);
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
        assertThrows(NullPointerException.class, () -> courseNoSections.assignTeacher(null));
    }

    @Test
    void assignTeacher_PublishedCourse_ThrowsException() {
        Course course = spy(courseNoSections);
        when(course.canEdit()).thenReturn(false);
        assertThrows(InputInvalidException.class, () -> course.assignTeacher("NewTeacher"));
    }

    @Test
    void publish_ValidCourse_PublishesCourse() {
        courseWithSections.publish("Admin");

        assertTrue(courseWithSections.getPublished());
        assertEquals("Admin", courseWithSections.getApprovedBy());
    }

    @Test
    void publish_AlreadyPublishedCourse_ThrowsException() {
        Course course = spy(courseWithSections);
        when(course.canEdit()).thenReturn(false);

        assertThrows(InputInvalidException.class, () -> course.publish("Admin"));
    }

    @Test
    void publish_CourseWithoutSections_ThrowsException() {
        // Tạo một course không có sections nhưng đã set giá
        courseNoSections.changePrice(Money.of(100, Currencies.VND));

        assertThrows(InputInvalidException.class, () -> courseNoSections.publish("Admin"));
    }

    @Test
    void publish_CourseWithoutPrice_ThrowsException() {
        Course course = new Course("Title", "Description", "ThumbnailUrl", new HashSet<>(), Language.ENGLISH, new HashSet<>(), new HashSet<>(), "Teacher");
        course.addSection(courseWithSections.getSections().iterator().next());

        assertThrows(InputInvalidException.class, () -> course.publish("Admin"));
    }

    @Test
    void publish_CourseWithSameTeacherAsApprover_ThrowsException() {
        assertThrows(InputInvalidException.class, () -> courseWithSections.publish("Teacher"));
    }

    @Test
    void publish_CourseWithoutTeacher_ThrowsException() {
        Course courseMock = spy(courseWithSections);
        when(courseMock.getTeacher()).thenReturn(null);

        assertThrows(InputInvalidException.class, () -> courseMock.publish("Admin"));
    }

    @Test
    void publish_NullApprovedBy_ThrowsException() {
        assertThrows(InputInvalidException.class, () -> courseWithSections.publish(null));
    }

    @Test
    void applyDiscount_ValidDiscount_AppliesDiscount() {
        MonetaryAmount price = Money.of(200, Currencies.VND);
        MonetaryAmount discountedPrice = Money.of(150, Currencies.VND);
        courseNoSections.changePrice(price);
        String discountCode = "25OFF";
        courseNoSections.applyDiscount(discountedPrice, discountCode);
        assertEquals(Money.of(50, Currencies.VND), courseNoSections.getDiscountedPrice());
        assertEquals(discountCode, courseNoSections.getDiscountCode());
    }

    @Test
    void applyDiscount_NullDiscountedPrice_ThrowsException() {
        MonetaryAmount price = Money.of(200, Currencies.VND);
        String discountCode = "25OFF";
        courseNoSections.changePrice(price);
        assertThrows(NullPointerException.class, () -> courseNoSections.applyDiscount(null, discountCode));
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
        assertThrows(NullPointerException.class, () -> courseNoSections.addSection(null));
    }

    @Test
    void addSection_SectionWithSameTitle_ThrowsException() {
        // Tạo một section khác có cùng title với section đã thêm vào course
        CourseSection duplicateSection = new CourseSection(courseWithSections.getSections().iterator().next().getTitle());
        assertThrows(InputInvalidException.class, () -> courseWithSections.addSection(duplicateSection));
    }

    @Test
    void addSection_SectionWithoutLessons_ThrowsException() {
        CourseSection section = new CourseSection("SectionTitle");
        assertThrows(InputInvalidException.class, () -> courseNoSections.addSection(section));
    }

    @Test
    void addSection_PublishedCourse_ThrowsException() {
        Course course = spy(courseNoSections);
        when(course.canEdit()).thenReturn(false); // Giả lập khóa học đã publish

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
        when(course.canEdit()).thenReturn(false); // Giả lập khóa học đã publish

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
        when(course.canEdit()).thenReturn(false);

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
        when(course.canEdit()).thenReturn(false);

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
        when(course.canEdit()).thenReturn(false);

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
        when(course.canEdit()).thenReturn(false);

        assertThrows(InputInvalidException.class, () -> course.removeLessonFromSection(1L, 1L));
    }

}
