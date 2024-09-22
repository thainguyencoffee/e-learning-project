package com.elearning.course.web;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.BAD_REQUEST)
public class CoursePermissionException extends RuntimeException {
    public CoursePermissionException(String message) {
        super(message);
    }
}
