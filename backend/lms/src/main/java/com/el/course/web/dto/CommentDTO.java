package com.el.course.web.dto;

import com.el.common.ValidateMessages;
import com.el.course.domain.Comment;
import com.el.course.domain.UserInfo;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.util.Set;

public record CommentDTO(
        @NotBlank
        @Size(min = 10, message = ValidateMessages.MIN_LENGTH)
        @Size(max = 5000, message = ValidateMessages.MAX_LENGTH)
        String content,
        Set<String> attachmentUrls
) {
    public Comment toComment(UserInfo info) {
        return new Comment(content, info, attachmentUrls);
    }
}
