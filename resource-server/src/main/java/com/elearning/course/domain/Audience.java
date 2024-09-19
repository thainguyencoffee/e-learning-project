package com.elearning.course.domain;

import java.util.Set;

public record Audience(
        Boolean isPublic,
        Set<String> emailAuthorities
) {

    public Audience {
        // Validate the input logic
        if (!isPublic) {
            if (emailAuthorities == null || emailAuthorities.isEmpty()) {
                throw new AudienceInvalidException("Audience is not public, email authorities must be provided and cannot be empty.");
            }
        }
    }

}