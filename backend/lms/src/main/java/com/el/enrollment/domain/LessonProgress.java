package com.el.enrollment.domain;

import com.el.common.exception.InputInvalidException;
import lombok.Getter;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

import java.time.LocalDateTime;
import java.util.Objects;

@Table("lesson_progress")
@Getter
public class LessonProgress {
    @Id
    private Long id;
    private Long lessonId;
    private String lessonTitle;
    private boolean completed;
    private LocalDateTime completedDate;

    public LessonProgress(String lessonTitle, Long lessonId) {
        if (lessonId == null) throw new InputInvalidException("LessonId must not be null.");

        this.lessonId = lessonId;
        this.lessonTitle = lessonTitle;
        this.completed = false;
        completedDate = null;
    }

    public void markAsCompleted() {
        if (this.completed || this.completedDate != null)  {
            throw new InputInvalidException("LessonProgress is already completed.");
        }

        this.completed = true;
        this.completedDate = LocalDateTime.now();
    }

    public void markAsIncomplete() {
        if (!this.completed || this.completedDate == null) {
            throw new InputInvalidException("LessonProgress is already incomplete.");
        }

        this.completed = false;
        this.completedDate = null;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        LessonProgress that = (LessonProgress) o;
        return Objects.equals(id, that.id) && Objects.equals(lessonId, that.lessonId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, lessonId);
    }

}
