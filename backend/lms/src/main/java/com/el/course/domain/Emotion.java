package com.el.course.domain;

import lombok.Getter;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

import java.time.LocalDateTime;

@Table("emotion")
@Getter
public class Emotion {
    @Id
    private Long id;
    private String username;
    private LocalDateTime createdDate;

    public Emotion(String username) {
        this.username = username;
        this.createdDate = LocalDateTime.now();
    }

    public Emotion() {}

}
