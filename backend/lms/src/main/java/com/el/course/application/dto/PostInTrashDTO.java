package com.el.course.application.dto;

import java.time.LocalDateTime;

public record PostInTrashDTO(
        Long id,
        String content,
        String username,
        LocalDateTime createdDate
) {
}
