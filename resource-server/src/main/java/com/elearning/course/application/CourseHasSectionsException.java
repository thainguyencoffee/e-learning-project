package com.elearning.course.application;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.BAD_REQUEST)
public class CourseHasSectionsException extends RuntimeException {
    public CourseHasSectionsException(String message) {
        super(message);
    }
}
