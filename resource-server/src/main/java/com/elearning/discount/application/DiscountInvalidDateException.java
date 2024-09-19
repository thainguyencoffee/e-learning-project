package com.elearning.discount.application;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.BAD_REQUEST)
public class DiscountInvalidDateException extends RuntimeException {
    public DiscountInvalidDateException() {
        super();
    }
}
