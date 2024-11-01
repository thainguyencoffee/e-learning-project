package com.el.course.application.dto;

import com.el.common.ValidateMessages;
import com.el.course.domain.CourseRequest;
import com.el.course.domain.RequestType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record CourseRequestDTO (
        @NotNull(message = ValidateMessages.NOT_NULL)
        RequestType type,
        @NotBlank(message = ValidateMessages.NOT_BLANK)
        @Size(min = 25, message = ValidateMessages.MIN_LENGTH)
        @Size(max = 2000, message = ValidateMessages.MAX_LENGTH)
        String message,
        String requestedBy
) {

    public CourseRequest toCourseRequest() {
        return new CourseRequest(type, message, requestedBy);
    }

}
