package com.elearning.course.application;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.util.Set;

public record AudienceDTO (
        @NotNull Boolean isPublic,
        @NotEmpty(message = "Email authorities must not be empty if isPublic is false")
        Set<String> emailAuthorities
) {

}
