package com.el.course.domain;

import com.el.common.exception.InputInvalidException;

public record UserInfo(
        String firstName,
        String lastName,
        String username
) {

    public UserInfo {
        if (lastName == null || lastName.isBlank()) {
            throw new InputInvalidException("Last name is required");
        }
        if (firstName == null || firstName.isBlank()) {
            throw new InputInvalidException("First name is required");
        }
        if (username == null || username.isBlank()) {
            throw new InputInvalidException("Username is required");
        }
    }

}
