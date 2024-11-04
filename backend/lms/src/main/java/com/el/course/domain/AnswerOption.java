package com.el.course.domain;

import com.el.common.exception.InputInvalidException;
import lombok.Getter;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

@Table("answer_option")
@Getter
public class AnswerOption {
    @Id
    private Long id;
    private String content;
    private Boolean correct;

    public AnswerOption(String content, Boolean correct) {
        if (content == null || content.isBlank())
            throw new InputInvalidException("Content of an answer option must not be empty.");

        if (correct == null)
            throw new InputInvalidException("Correctness of an answer option must not be empty.");

        this.content = content;
        this.correct = correct;
    }

}
