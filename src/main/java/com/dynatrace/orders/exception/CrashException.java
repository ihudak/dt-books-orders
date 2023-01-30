package com.dynatrace.orders.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(value = HttpStatus.SERVICE_UNAVAILABLE)
public class CrashException extends RuntimeException {
        private static final long serialVersionUID = 1L;
        public CrashException(String message) {
            super(message);
        }
}
