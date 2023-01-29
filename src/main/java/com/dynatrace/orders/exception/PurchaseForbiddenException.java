package com.dynatrace.orders.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(value = HttpStatus.FORBIDDEN)
public class PurchaseForbiddenException extends RuntimeException {
    private static final long serialVersionUID = 1L;

    public PurchaseForbiddenException(String message) {
        super(message);
    }
}
