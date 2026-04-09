package com.consignment.service.exception;

import java.util.List;
import java.util.Map;

public class RequestValidationException extends RuntimeException {

    private final List<Map<String, String>> errors;

    public RequestValidationException(String message, List<Map<String, String>> errors) {
        super(message);
        this.errors = errors;
    }

    public List<Map<String, String>> getErrors() {
        return errors;
    }
}
