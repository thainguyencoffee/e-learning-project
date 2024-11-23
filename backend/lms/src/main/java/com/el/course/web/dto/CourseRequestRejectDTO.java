package com.el.course.web.dto;

import com.el.common.ValidateMessages;
import com.el.course.domain.RequestType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record CourseRequestRejectDTO(
        @NotNull(message = ValidateMessages.NOT_NULL)
        RequestType rejectType,
        @NotBlank(message = ValidateMessages.NOT_BLANK)
        @Size(min = 25, message = ValidateMessages.MIN_LENGTH)
        @Size(max = 2000, message = ValidateMessages.MAX_LENGTH)
        String rejectCause,
        @NotBlank(message = ValidateMessages.NOT_BLANK)
        String rejectBy
) {

        public CourseRequestResolveDTO toCourseRequestResolveDTO() {
                return new CourseRequestResolveDTO(rejectCause, rejectBy);
        }

}
