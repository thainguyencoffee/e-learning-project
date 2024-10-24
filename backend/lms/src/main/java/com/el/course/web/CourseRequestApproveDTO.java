package com.el.course.web;

import com.el.common.ValidateMessages;
import com.el.course.application.dto.CourseRequestResolveDTO;
import com.el.course.domain.RequestType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record CourseRequestApproveDTO(
        @NotNull(message = ValidateMessages.NOT_NULL)
        RequestType approveType,
        @NotBlank(message = ValidateMessages.NOT_BLANK)
        @Size(min = 25, message = ValidateMessages.MIN_LENGTH)
        @Size(max = 2000, message = ValidateMessages.MAX_LENGTH)
        String approveMessage,
        @NotBlank(message = ValidateMessages.NOT_BLANK)
        String approveBy
) {

        public CourseRequestResolveDTO toCourseRequestResolveDTO() {
                return new CourseRequestResolveDTO(approveMessage, approveBy);
        }

}
