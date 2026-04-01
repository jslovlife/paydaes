package com.paydaes.corehr.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.SERVICE_UNAVAILABLE)
public class TenantResolutionException extends RuntimeException {

    public TenantResolutionException(String message) {
        super(message);
    }

    public TenantResolutionException(String message, Throwable cause) {
        super(message, cause);
    }
}
