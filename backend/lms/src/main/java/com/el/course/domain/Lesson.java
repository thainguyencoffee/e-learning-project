package com.el.course.domain;

import com.el.common.exception.InputInvalidException;
import lombok.Getter;
import lombok.ToString;
import org.springframework.data.annotation.*;
import org.springframework.data.relational.core.mapping.Table;

@Getter
@Table("lesson")
@ToString
public class Lesson {

    @Id
    private Long id;
    private String title;
    private Type type;
    private String link;
    private Long quiz;
    private Integer orderIndex;

    public enum Type {
        VIDEO, TEXT, QUIZ, ASSIGNMENT
    }

    public Lesson (String title, Type type, String link, Long quiz) {
        if (title.isBlank()) throw new InputInvalidException("Lesson title must not be empty.");
        if (type == null) throw new InputInvalidException("Lesson type must not be null.");

        validateLinkOrQuiz(type, link, quiz);
        this.title = title;
        this.type = type;
        this.link = link;
        this.quiz = quiz;
    }

    public void updateFrom(Lesson updatedLesson) {
        if (updatedLesson.getTitle().isBlank()) throw new InputInvalidException("Lesson title must not be empty.");
        if (updatedLesson.getType() == null) throw new InputInvalidException("Lesson type must not be null.");

        this.title = updatedLesson.getTitle();
        this.type = updatedLesson.getType();
        validateLinkOrQuiz(updatedLesson.getType(), updatedLesson.getLink(), updatedLesson.getQuiz());
        this.link = updatedLesson.getLink();
        this.quiz = updatedLesson.getQuiz();
    }

    private void validateLinkOrQuiz(Type type, String link, Long quiz) {
        switch (type) {
            case VIDEO:
            case TEXT:
                if (link == null || link.isEmpty()) {
                    throw new InputInvalidException("Link must not be empty for VIDEO or TEXT lesson.");
                }
                // validate link format
                if (!link.startsWith("http://") && !link.startsWith("https://")) {
                    throw new InputInvalidException("Link must is a valid URL.");
                }
                break;
            case QUIZ:
                if (quiz == null) {
                    throw new InputInvalidException("Quiz ID must not be null for QUIZ lesson.");
                }
                break;
            default:
                throw new InputInvalidException("Unsupported lesson type.");
        }
    }

    protected void setOrderIndex(Integer i) {
        this.orderIndex = i;
    }

}
