package com.elearning.course.domain.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.BAD_GATEWAY)
public class CannotUpdateCourseException extends RuntimeException {
    public CannotUpdateCourseException(String message) {
        super(message);
    }
}
