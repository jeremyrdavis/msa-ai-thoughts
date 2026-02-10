package com.redhat.demos.evaluation.exception;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class ErrorResponse {

    public String correlationId;
    public String message;
    public int status;
    public LocalDateTime timestamp;
    public List<FieldError> fieldErrors;

    public ErrorResponse() {
        this.correlationId = UUID.randomUUID().toString();
        this.timestamp = LocalDateTime.now();
        this.fieldErrors = new ArrayList<>();
    }

    public ErrorResponse(String message, int status) {
        this();
        this.message = message;
        this.status = status;
    }

    public void addFieldError(String field, String message) {
        fieldErrors.add(new FieldError(field, message));
    }

    public static class FieldError {
        public String field;
        public String message;

        public FieldError(String field, String message) {
            this.field = field;
            this.message = message;
        }
    }
}
