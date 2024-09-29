package com.elearning.course.domain;

import com.elearning.common.exception.InputInvalidException;
import lombok.Getter;
import org.apache.commons.lang3.Validate;
import org.springframework.data.annotation.*;
import org.springframework.data.relational.core.mapping.Table;
import org.springframework.util.Assert;

@Getter
@Table("lesson")
public class Lesson {

    @Id
    private Long id;
    private String title;
    private Type type;
    private String link;
    private Long quiz;

    public enum Type {
        VIDEO, TEXT, QUIZ, ASSIGNMENT
    }

    public Lesson (String title, Type type, String link, Long quiz) {
        // Kiểm tra tính hợp lệ ngay trong constructor
        Assert.hasText(title, "Lesson title must not be empty.");
        Assert.notNull(type, "Lesson type must not be null.");

        validateLinkOrQuiz(type, link, quiz);
        // Gán giá trị sau khi đã kiểm tra tính hợp lệ
        this.title = title;
        this.type = type;
        this.link = link;
        this.quiz = quiz;
    }

    public void updateFrom(Lesson updatedLesson) {
        Assert.hasText(updatedLesson.getTitle(), "Updated lesson title must not be empty.");
        Assert.notNull(updatedLesson.getType(), "Updated lesson type must not be null.");
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

}
