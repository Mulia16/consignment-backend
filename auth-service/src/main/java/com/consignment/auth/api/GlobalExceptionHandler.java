package com.consignment.auth.api;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);
    private static final Pattern JSON_FIELD_PATTERN = Pattern.compile("through reference chain: .*?\\[\\\"([^\\\"]+)\\\"\\]");

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse<?>> handleValidation(MethodArgumentNotValidException ex) {
        List<Map<String, String>> errors = ex.getBindingResult().getFieldErrors().stream()
            .map(f -> Map.of(
                "field", f.getField(),
                "message", f.getDefaultMessage() == null ? "Invalid value" : f.getDefaultMessage(),
                "value", f.getRejectedValue() == null ? "null" : String.valueOf(f.getRejectedValue())
            ))
            .collect(Collectors.toList());
        String message = errors.stream()
            .map(e -> e.get("field") + ": " + e.get("message"))
                .collect(Collectors.joining(", "));
        log.warn("[422] ValidationError: {}", message);
        return ResponseEntity.status(HttpStatus.UNPROCESSABLE_ENTITY)
            .body(ApiResponse.validationError(errors));
        }

        @ExceptionHandler(HttpMessageNotReadableException.class)
        public ResponseEntity<ApiResponse<?>> handleNotReadable(HttpMessageNotReadableException ex) {
        String detail = ex.getMostSpecificCause() != null ? ex.getMostSpecificCause().getMessage() : ex.getMessage();
        String field = extractJsonField(detail);
        log.warn("[400] MessageNotReadable: {}", detail);
        if (field != null) {
            List<Map<String, String>> errors = List.of(Map.of(
                "field", field,
                "message", "Invalid value or type for field",
                "value", detail == null ? "unknown" : detail
            ));
            return ResponseEntity.badRequest().body(ApiResponse.error(400, "Invalid request format", errors));
        }
        return ResponseEntity.badRequest().body(ApiResponse.error(400, "Invalid request format: " + detail));
        }

        @ExceptionHandler(DataIntegrityViolationException.class)
        public ResponseEntity<ApiResponse<?>> handleDataIntegrity(DataIntegrityViolationException ex) {
        String detail = ex.getMostSpecificCause() != null ? ex.getMostSpecificCause().getMessage() : ex.getMessage();
        log.warn("[422] DataIntegrityViolation: {}", detail);
        List<Map<String, String>> errors = List.of(Map.of(
            "field", extractConstraintField(detail),
            "message", "Request violates database constraint",
            "value", detail
        ));
        return ResponseEntity.status(HttpStatus.UNPROCESSABLE_ENTITY)
            .body(ApiResponse.error(422, "Validation failed", errors));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<?>> handleGeneral(Exception ex) {
        log.error("[500] Unexpected error: {}", ex.getMessage(), ex);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.error(500, "An unexpected error occurred"));
    }

    private String extractJsonField(String detail) {
        if (detail == null) {
            return null;
        }
        Matcher matcher = JSON_FIELD_PATTERN.matcher(detail);
        if (matcher.find()) {
            return matcher.group(1);
        }
        return null;
    }

    private String extractConstraintField(String detail) {
        if (detail == null || detail.isBlank()) {
            return "payload";
        }
        String lower = detail.toLowerCase();
        if (lower.contains("username")) {
            return "username";
        }
        if (lower.contains("email")) {
            return "email";
        }
        if (lower.contains("password")) {
            return "password";
        }
        return "payload";
    }
}
