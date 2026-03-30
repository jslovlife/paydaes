package com.paydaes.tms.exception;

import lombok.AllArgsConstructor;
import lombok.Data;
import java.time.LocalDateTime;
import java.util.List;

@Data
@AllArgsConstructor
public class ApiErrorResponse {
    private int status;
    private String message;
    private LocalDateTime timestamp;
    private List<String> errors;

    public ApiErrorResponse(int status, String message) {
        this.status = status;
        this.message = message;
        this.timestamp = LocalDateTime.now();
        this.errors = null;
    }

    public ApiErrorResponse(int status, String message, List<String> errors) {
        this.status = status;
        this.message = message;
        this.timestamp = LocalDateTime.now();
        this.errors = errors;
    }
}
