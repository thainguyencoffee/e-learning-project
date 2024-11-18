package com.el.course.domain;

import com.el.common.exception.InputInvalidException;
import lombok.Getter;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.MappedCollection;
import org.springframework.data.relational.core.mapping.Table;

import java.util.Set;

@Table("question")
@Getter
public class Question {
    @Id
    private Long id;
    private String content;
    private QuestionType type;
    @MappedCollection(idColumn = "question")
    private Set<AnswerOption> options;
    private Boolean trueFalseAnswer;
    private Integer score;

    public Question(String content, QuestionType type, Integer score, Set<AnswerOption> options, Boolean trueFalseAnswer) {

        if (content == null || content.isBlank())
            throw new InputInvalidException("Content of a question must not be empty.");

        if (score == null || score < 0 || score > 5)
            throw new InputInvalidException("Score of a question must be a non-negative number. Maximum score is 5.");

        if (type == null)
            throw new InputInvalidException("Type of a question must not be empty.");

        this.content = content;
        this.type = type;
        this.score = score;
        this.options = options;
        this.trueFalseAnswer = trueFalseAnswer;

        validateTypeAndOptions();
    }

    public void updateFrom(Question updatedQuestion) {
        if (updatedQuestion.getContent() == null || updatedQuestion.getContent().isBlank())
            throw new InputInvalidException("Content of a question must not be empty.");

        if (updatedQuestion.getScore() == null || updatedQuestion.getScore() < 0 || updatedQuestion.getScore() > 5)
            throw new InputInvalidException("Score of a question must be a non-negative number. Maximum score is 5.");

        if (updatedQuestion.getType() == null)
            throw new InputInvalidException("Type of a question must not be empty.");

        this.content = updatedQuestion.getContent();
        this.type = updatedQuestion.getType();
        this.score = updatedQuestion.getScore();
        this.options = updatedQuestion.getOptions();
        this.trueFalseAnswer = updatedQuestion.getTrueFalseAnswer();

        validateTypeAndOptions();
    }

    private void validateTypeAndOptions() {
        if (type == QuestionType.TRUE_FALSE) {
            if (trueFalseAnswer == null) {
                throw new InputInvalidException("True/False question must define the option is true or false.");
            }
        } else {
            if (options.stream().anyMatch(o -> options.stream().filter(o2 -> o2.getContent().equals(o.getContent())).count() > 1))
                throw new InputInvalidException("Duplicate answer option content.");
            if (options.size() < 2)
                throw new InputInvalidException("Question must have at least 2 options.");

            long numberOfCorrect = options.stream().filter(AnswerOption::getCorrect).count();
            if (numberOfCorrect == 0)
                throw new InputInvalidException("Question must have at least 1 correct option.");

            if (type == QuestionType.SINGLE_CHOICE) {
                if (numberOfCorrect != 1)
                    throw new InputInvalidException("A single choice question must have exactly 1 correct option.");
            }
        }
    }

}
