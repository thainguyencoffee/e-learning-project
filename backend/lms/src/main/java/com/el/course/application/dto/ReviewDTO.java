package com.el.course.application.dto;

import com.el.common.ValidateMessages;
import com.el.course.domain.Review;
import jakarta.validation.constraints.NotNull;

public record ReviewDTO(
        @NotNull(message = ValidateMessages.NOT_NULL)
        Integer rating,
        String comment
) {

    public Review toReview(String username) {
        return new Review(username, rating, comment);
    }
}