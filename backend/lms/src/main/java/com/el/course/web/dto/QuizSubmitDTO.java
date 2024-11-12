package com.el.course.web.dto;

import jakarta.validation.Valid;

import java.util.List;

public record QuizSubmitDTO(
        @Valid
        List<QuestionSubmitDTO> questions
) {
}
