package com.el.course.domain;

import com.el.common.exception.InputInvalidException;
import lombok.Getter;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

import java.time.LocalDateTime;

@Table("review")
@Getter
public class Review {
    @Id
    private Long id;
    private String username;
    private Integer rating;
    private String comment;
    private LocalDateTime reviewDate;

    public Review(String username, Integer rating, String comment) {
        if (username == null || username.isBlank()) throw new InputInvalidException("Username for review must not be null.");
        if (rating == null || rating < 1 || rating > 5) throw new InputInvalidException("Rating for review must be between 1 and 5.");


        this.username = username;
        this.rating = rating;
        this.comment = (comment == null || comment.isBlank()) ? "No comment provided." : comment;
        this.reviewDate = LocalDateTime.now();
    }

}
