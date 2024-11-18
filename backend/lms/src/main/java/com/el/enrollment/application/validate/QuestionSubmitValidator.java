package com.el.enrollment.application.validate;

import com.el.course.domain.QuestionType;
import com.el.enrollment.application.dto.QuestionSubmitDTO;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class QuestionSubmitValidator implements ConstraintValidator<QuestionSubmitConstraint, QuestionSubmitDTO> {

    @Override
    public boolean isValid(QuestionSubmitDTO questionSubmitDTO, ConstraintValidatorContext constraintValidatorContext) {
        if (questionSubmitDTO == null) return true;

        boolean trueFalseAnswerNull = questionSubmitDTO.trueFalseAnswer() == null;

        if (questionSubmitDTO.type() == QuestionType.TRUE_FALSE) {
            if (trueFalseAnswerNull) {
                constraintValidatorContext.disableDefaultConstraintViolation();
                constraintValidatorContext.buildConstraintViolationWithTemplate("True false question must have an answer")
                        .addPropertyNode("trueFalseAnswer")
                        .addConstraintViolation();
                return false;
            }
        } else if (questionSubmitDTO.type() == QuestionType.SINGLE_CHOICE) {
            boolean oneAnswer = questionSubmitDTO.answerOptionIds().size() == 1;

            if (!oneAnswer) {
                constraintValidatorContext.disableDefaultConstraintViolation();
                constraintValidatorContext.buildConstraintViolationWithTemplate("Single choice question must have exactly one answer")
                        .addPropertyNode("answerOptionIds")
                        .addConstraintViolation();
                return false;
            }
        } else if (questionSubmitDTO.type() == QuestionType.MULTIPLE_CHOICE) {
            boolean multipleAnswer = questionSubmitDTO.answerOptionIds().size() > 1;

            if (!multipleAnswer) {
                constraintValidatorContext.disableDefaultConstraintViolation();
                constraintValidatorContext.buildConstraintViolationWithTemplate("Multiple choice question must have more than one answer")
                        .addPropertyNode("answerOptionIds")
                        .addConstraintViolation();
                return false;
            }
        }

        return true;
    }
}
