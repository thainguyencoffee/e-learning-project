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
    private boolean bonus;
    private boolean completed;
    private Integer orderIndex;
    private LocalDateTime completedDate;
    private Boolean inProgress;

    public LessonProgress(String lessonTitle, Long lessonId, Integer orderIndex) {
        if (lessonId == null) throw new InputInvalidException("LessonId must not be null.");
        if (lessonTitle == null) throw new InputInvalidException("LessonTitle must not be null.");
        if (orderIndex == null) throw new InputInvalidException("OrderIndex must not be null.");

        this.lessonId = lessonId;
        this.lessonTitle = lessonTitle;
        this.orderIndex = orderIndex;
        this.completed = false;
        this.bonus = false;
        completedDate = null;

        inProgress = orderIndex == 1;
    }

    public void markAsBonus() {
        if (this.bonus) {
            throw new InputInvalidException("LessonProgress is already be bonus.");
        }

        this.bonus = true;
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

    public void makeInProgress() {
        if (this.inProgress) {
            throw new InputInvalidException("LessonProgress is already in progress.");
        }

        this.inProgress = true;
    }

    public void makeNotInProgress() {
        if (!this.inProgress) {
            throw new InputInvalidException("LessonProgress is already not in progress.");
        }

        this.inProgress = false;
    }

}
