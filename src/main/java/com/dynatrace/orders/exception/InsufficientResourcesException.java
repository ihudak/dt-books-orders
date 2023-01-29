package com.dynatrace.orders.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(value = HttpStatus.EXPECTATION_FAILED)
public class InsufficientResourcesException extends RuntimeException {
    private static final long serialVersionUID = 1L;

    public InsufficientResourcesException(String message) {
        super(message);
    }
}
