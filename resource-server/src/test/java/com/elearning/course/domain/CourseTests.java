package com.elearning.course.domain;

import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

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



}
