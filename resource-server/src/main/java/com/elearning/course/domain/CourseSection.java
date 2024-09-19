package com.elearning.course.domain;

import lombok.Data;
import lombok.ToString;
import org.springframework.data.annotation.*;
import org.springframework.data.relational.core.mapping.MappedCollection;
import org.springframework.data.relational.core.mapping.Table;
import org.springframework.util.Assert;

import java.util.HashSet;
import java.util.Set;

@Data
@Table("course_section")
@ToString(exclude = "lessons")
public class CourseSection {
    @Id
    private Long id;
    private String title;
    @MappedCollection(idColumn = "course_section")
    private Set<Lesson> lessons = new HashSet<>();

    public CourseSection(String title) {
        Assert.hasText(title, "Title must not be empty");
        this.title = title;
    }

    public void addLesson(Lesson lesson) {
        if (lesson == null) {
            throw new IllegalArgumentException("Lesson can't be null");
        }
        this.lessons.add(lesson);
    }

    public Lesson findLessonById(Long lessonId) {
        return this.lessons.stream()
                .filter(lesson -> lesson.getId().equals(lessonId))
                .findFirst()
                .orElse(null);
    }

    public void updateInfo(String title) {
        Assert.hasText(title, "Title must not be empty");
        this.title = title;
    }

    public void removeLessonsOrphan(Set<Long> validLessonIds) {
        this.lessons.removeIf(lesson -> lesson.getId() != null &&  !validLessonIds.contains(lesson.getId()));
    }

}