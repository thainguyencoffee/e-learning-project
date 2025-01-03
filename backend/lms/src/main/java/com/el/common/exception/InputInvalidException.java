package com.el.common.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.BAD_REQUEST)
/*
* 201 Usages*/
public class InputInvalidException extends RuntimeException {
    public InputInvalidException(String message) {
        super(message);
    }
}
