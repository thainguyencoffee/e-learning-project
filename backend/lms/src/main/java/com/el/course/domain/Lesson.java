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
    private Integer orderIndex;

    public enum Type {
        VIDEO, TEXT, QUIZ, ASSIGNMENT
    }

    public Lesson (String title, Type type, String link) {
        if (title.isBlank()) throw new InputInvalidException("Lesson title must not be empty.");
        if (type == null) throw new InputInvalidException("Lesson type must not be null.");

        validateLink(type, link);
        this.title = title;
        this.type = type;
        this.link = link;
    }

    public void updateFrom(Lesson updatedLesson) {
        if (updatedLesson.getTitle().isBlank()) throw new InputInvalidException("Lesson title must not be empty.");
        if (updatedLesson.getType() == null) throw new InputInvalidException("Lesson type must not be null.");

        this.title = updatedLesson.getTitle();
        this.type = updatedLesson.getType();
        validateLink(updatedLesson.getType(), updatedLesson.getLink());
        this.link = updatedLesson.getLink();
    }

    private void validateLink(Type type, String link) {
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
            default:
                throw new InputInvalidException("Unsupported lesson type.");
        }
    }

    protected void setOrderIndex(Integer i) {
        this.orderIndex = i;
    }

}
