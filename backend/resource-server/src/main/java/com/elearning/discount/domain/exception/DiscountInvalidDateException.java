package com.elearning.discount.domain.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.BAD_REQUEST)
public class DiscountInvalidDateException extends RuntimeException {
    public DiscountInvalidDateException(String message) {
        super(message);
    }
}
