package com.el.course.web.dto;

import com.el.common.ValidateMessages;
import com.el.course.domain.Post;
import com.el.course.domain.UserInfo;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.util.Set;

public record CoursePostDTO(
        @NotBlank
        @Size(min = 10, message = ValidateMessages.MIN_LENGTH)
        @Size(max = 10000, message = ValidateMessages.MAX_LENGTH)
        String content,
        Set<String> attachmentUrls
) {

        public Post toPost(UserInfo info) {
                return new Post(content, info, attachmentUrls);
        }

}
