package com.elearning.course.domain;

import com.elearning.common.exception.InputInvalidException;
import lombok.Getter;
import lombok.ToString;
import org.apache.commons.lang3.Validate;
import org.springframework.data.annotation.*;
import org.springframework.data.relational.core.mapping.MappedCollection;
import org.springframework.data.relational.core.mapping.Table;
import org.springframework.util.Assert;

import java.util.HashSet;
import java.util.Set;

@Getter
@Table("course_section")
@ToString(exclude = "lessons")
public class CourseSection {
    @Id
    private Long id;
    private String title;
    @MappedCollection(idColumn = "course_section")
    private Set<Lesson> lessons = new HashSet<>();

    public CourseSection(String title) {
        Assert.hasText(title, "Title must not be empty.");

        this.title = title;
    }

    public void updateInfo(String newTitle) {
        Validate.notBlank(title, "Title must not be blank.");
        this.title = newTitle;
    }

    public void addLesson(Lesson lesson) {
        if (lessons.stream().anyMatch(l -> l.getTitle().equals(lesson.getTitle()))) {
            throw new InputInvalidException("Duplicate lesson title.");
        }

        this.lessons.add(lesson);
    }

    public void updateLesson(Long lessonId, Lesson updatedLesson) {
        if (lessons.stream().anyMatch(l -> l.getTitle().equals(updatedLesson.getTitle()))) {
            throw new IllegalArgumentException("Duplicate lesson title.");
        }

        Lesson lesson = findLessonById(lessonId);
        lesson.updateFrom(updatedLesson);  // Delegate updating logic to `Lesson`
    }

    public void removeLesson(Long lessonId) {
        Lesson lesson = findLessonById(lessonId);
        this.lessons.remove(lesson);
    }

    public Lesson findLessonById(Long lessonId) {
        return this.lessons.stream()
                .filter(lesson -> lesson.getId().equals(lessonId))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Lesson not found"));
    }

}