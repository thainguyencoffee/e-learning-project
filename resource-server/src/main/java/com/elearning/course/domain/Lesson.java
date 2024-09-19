package com.elearning.course.domain;

import lombok.Data;
import lombok.ToString;
import org.springframework.data.annotation.*;
import org.springframework.data.relational.core.mapping.Table;
import org.springframework.util.Assert;


@Data
@Table("lesson")
@ToString
public class Lesson {
    @Id
    private Long id;
    private String title;
    private Type type;
    private String link;

    public enum Type {
        VIDEO, TEXT, QUIZ, ASSIGNMENT
    }

    public Lesson(String title, Type type, String link) {
        Assert.hasText(title, "Title must not be empty");
        Assert.notNull(type, "Type must not be null");
        Assert.hasText(link, "Link must not be empty");

        this.title = title;
        this.type = type;
        this.link = link;
    }

    public void updateInfo(String title, Type type, String link) {
        Assert.hasText(title, "Title must not be empty");
        Assert.notNull(type, "Type must not be null");
        Assert.hasText(link, "Link must not be empty");

        this.title = title;
        this.type = type;
        this.link = link;
    }

}
