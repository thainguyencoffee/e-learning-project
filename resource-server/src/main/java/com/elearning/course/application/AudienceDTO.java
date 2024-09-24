package com.elearning.course.application;

import jakarta.validation.constraints.NotNull;

import java.util.Set;

@ValidAudience
public record AudienceDTO (
        @NotNull(message = "isPublic must not be null")
        Boolean isPublic,
        Set<String> emailAuthorities
) {

}
