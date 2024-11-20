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
            boolean oneAnswerNull = questionSubmitDTO.singleChoiceAnswer() == null;

            if (oneAnswerNull) {
                constraintValidatorContext.disableDefaultConstraintViolation();
                constraintValidatorContext.buildConstraintViolationWithTemplate("Single choice question must have an answer")
                        .addPropertyNode("singleChoiceAnswer")
                        .addConstraintViolation();
                return false;
            }
        } else if (questionSubmitDTO.type() == QuestionType.MULTIPLE_CHOICE) {
            boolean multipleAnswerEmpty = questionSubmitDTO.answerOptionIds().isEmpty();

            if (multipleAnswerEmpty) {
                constraintValidatorContext.disableDefaultConstraintViolation();
                constraintValidatorContext.buildConstraintViolationWithTemplate("Multiple choice question must provide answer")
                        .addPropertyNode("answerOptionIds")
                        .addConstraintViolation();
                return false;
            }
        }

        return true;
    }
}
