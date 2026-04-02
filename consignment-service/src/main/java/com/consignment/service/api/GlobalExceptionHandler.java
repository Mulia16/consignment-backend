package com.consignment.service.api;

import com.consignment.service.exception.InvalidStateTransitionException;
import com.consignment.service.exception.ResourceNotFoundException;
import com.consignment.service.exception.BusinessRuleViolationException;
import com.consignment.service.model.ApiErrorResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.Instant;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ApiErrorResponse> handleNotFound(ResourceNotFoundException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(new ApiErrorResponse("NOT_FOUND", ex.getMessage(), Instant.now()));
    }

    @ExceptionHandler(InvalidStateTransitionException.class)
    public ResponseEntity<ApiErrorResponse> handleInvalidState(InvalidStateTransitionException ex) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(new ApiErrorResponse("INVALID_STATE", ex.getMessage(), Instant.now()));
    }

    @ExceptionHandler(BusinessRuleViolationException.class)
    public ResponseEntity<ApiErrorResponse> handleBusinessRule(BusinessRuleViolationException ex) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(new ApiErrorResponse("BUSINESS_RULE_VIOLATION", ex.getMessage(), Instant.now()));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiErrorResponse> handleValidation(MethodArgumentNotValidException ex) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(new ApiErrorResponse("VALIDATION_ERROR", "Request validation failed", Instant.now()));
    }
}
