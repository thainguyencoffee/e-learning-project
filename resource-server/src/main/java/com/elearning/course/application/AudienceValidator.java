package com.elearning.course.application;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class AudienceValidator implements ConstraintValidator<ValidAudience, AudienceDTO> {

    @Override
    public boolean isValid(AudienceDTO audienceDTO, ConstraintValidatorContext context) {
        if (!audienceDTO.isPublic() && (audienceDTO.emailAuthorities() == null || audienceDTO.emailAuthorities().isEmpty())) {
            context.disableDefaultConstraintViolation();
            context.buildConstraintViolationWithTemplate("Email authorities must not be empty if isPublic is false")
                    .addPropertyNode("emailAuthorities")
                    .addConstraintViolation();
            return false;
        }
        return true;
    }

}
