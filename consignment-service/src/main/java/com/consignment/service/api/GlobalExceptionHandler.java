package com.consignment.service.api;

import com.consignment.service.exception.BusinessRuleViolationException;
import com.consignment.service.exception.InvalidStateTransitionException;
import com.consignment.service.exception.MissingStoreClaimException;
import com.consignment.service.exception.RequestValidationException;
import com.consignment.service.exception.ResourceNotFoundException;
import com.consignment.service.model.ApiResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);
    private static final Pattern JSON_FIELD_PATTERN = Pattern.compile("through reference chain: .*?\\[\\\"([^\\\"]+)\\\"\\]");

    // 403 - JWT tidak memiliki claim store
    @ExceptionHandler(MissingStoreClaimException.class)
    public ResponseEntity<ApiResponse<Void>> handleMissingStoreClaim(MissingStoreClaimException ex, HttpServletRequest request) {
        log.warn("[403] MissingStoreClaim: {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.FORBIDDEN)
                .body(ApiResponse.error(403, "Store claim missing in token"));
    }

    // 404 - resource tidak ditemukan
    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ApiResponse<Void>> handleNotFound(ResourceNotFoundException ex, HttpServletRequest request) {
        log.warn("[404] ResourceNotFound: {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(ApiResponse.error(404, ex.getMessage()));
    }

    // 400 - format/parsing error (JSON tidak valid, tipe data salah)
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ApiResponse<Void>> handleNotReadable(HttpMessageNotReadableException ex, HttpServletRequest request) {
        String detail = ex.getMostSpecificCause() != null ? ex.getMostSpecificCause().getMessage() : ex.getMessage();
        String field = extractJsonField(detail);
        log.warn("[400] MessageNotReadable: {}", detail);
        if (field != null) {
            List<Map<String, String>> errors = List.of(Map.of(
                "field", field,
                "message", "Invalid value or type for field",
                "value", detail == null ? "unknown" : detail
            ));
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(new ApiResponse<>("Invalid request format", 400, errors, null, null));
        }
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
            .body(ApiResponse.error(400, "Invalid request format: " + detail));
    }

    // 422 - field validation gagal (@NotBlank, @NotNull, dll)
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse<Void>> handleValidation(MethodArgumentNotValidException ex, HttpServletRequest request) {
        List<Map<String, String>> errors = ex.getBindingResult().getFieldErrors().stream()
                .map(f -> Map.of("field", f.getField(), "message", f.getDefaultMessage()))
                .collect(Collectors.toList());
        String summary = errors.stream()
                .map(e -> e.get("field") + ": " + e.get("message"))
                .collect(Collectors.joining(", "));
        log.warn("[422] ValidationError: {}", summary);
        return ResponseEntity.status(HttpStatus.UNPROCESSABLE_ENTITY)
                .body(ApiResponse.validationError(errors));
    }

        // 422 - request validation gagal dari service layer
        @ExceptionHandler(RequestValidationException.class)
        public ResponseEntity<ApiResponse<Void>> handleRequestValidation(RequestValidationException ex, HttpServletRequest request) {
        String summary = ex.getErrors().stream()
            .map(e -> e.get("field") + ": " + e.get("message"))
            .collect(Collectors.joining(", "));
        log.warn("[422] RequestValidationError: {}", summary);
        return ResponseEntity.status(HttpStatus.UNPROCESSABLE_ENTITY)
            .body(new ApiResponse<>(ex.getMessage(), 422, ex.getErrors(), null, null));
        }

        // 422 - validation untuk @Validated di query/path/header
        @ExceptionHandler(ConstraintViolationException.class)
        public ResponseEntity<ApiResponse<Void>> handleConstraintViolation(ConstraintViolationException ex, HttpServletRequest request) {
        List<Map<String, String>> errors = ex.getConstraintViolations().stream()
            .map(this::toConstraintError)
            .collect(Collectors.toList());
        String summary = errors.stream()
            .map(e -> e.get("field") + ": " + e.get("message"))
            .collect(Collectors.joining(", "));
        log.warn("[422] ConstraintViolation: {}", summary);
        return ResponseEntity.status(HttpStatus.UNPROCESSABLE_ENTITY)
            .body(new ApiResponse<>("Validation failed", 422, errors, null, null));
        }

    // 422 - business rule violation (item tidak terdaftar, status tidak valid, dll)
    @ExceptionHandler(BusinessRuleViolationException.class)
    public ResponseEntity<ApiResponse<Void>> handleBusinessRule(BusinessRuleViolationException ex, HttpServletRequest request) {
        log.warn("[422] BusinessRuleViolation: {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.UNPROCESSABLE_ENTITY)
                .body(ApiResponse.error(422, ex.getMessage()));
    }

    // 422 - state transition tidak valid
    @ExceptionHandler(InvalidStateTransitionException.class)
    public ResponseEntity<ApiResponse<Void>> handleInvalidState(InvalidStateTransitionException ex, HttpServletRequest request) {
        log.warn("[422] InvalidStateTransition: {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.UNPROCESSABLE_ENTITY)
                .body(ApiResponse.error(422, ex.getMessage()));
    }

        // 422 - DB constraint violation (mis. unique/not-null/length)
        @ExceptionHandler(DataIntegrityViolationException.class)
        public ResponseEntity<ApiResponse<Void>> handleDataIntegrity(DataIntegrityViolationException ex, HttpServletRequest request) {
        String detail = ex.getMostSpecificCause() != null ? ex.getMostSpecificCause().getMessage() : ex.getMessage();
        log.warn("[422] DataIntegrityViolation: {}", detail);
            if (detail != null && detail.contains("cso_header_doc_no_key")) {
                List<Map<String, String>> duplicateDocNoErrors = List.of(Map.of(
                    "field", "docNo",
                    "message", "Generated document number already exists, please retry",
                    "value", "duplicate"
                ));
                return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body(new ApiResponse<>("Duplicate document number", 409, duplicateDocNoErrors, null, null));
            }
            if (detail != null && detail.contains("csr_header_doc_no_key")) {
                List<Map<String, String>> duplicateDocNoErrors = List.of(Map.of(
                    "field", "docNo",
                    "message", "Generated document number already exists, please retry",
                    "value", "duplicate"
                ));
                return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body(new ApiResponse<>("Duplicate document number", 409, duplicateDocNoErrors, null, null));
            }
        List<Map<String, String>> errors = List.of(Map.of(
            "field", extractConstraintField(detail),
            "message", "Request violates database constraint: " + detail
        ));
        return ResponseEntity.status(HttpStatus.UNPROCESSABLE_ENTITY)
            .body(new ApiResponse<>("Validation failed", 422, errors, null, null));
        }

    // 500 - unexpected error
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<Void>> handleGeneral(Exception ex, HttpServletRequest request) {
        log.error("[500] Unexpected error: {}", ex.getMessage(), ex);
        request.setAttribute("errorDetail", getStackTrace(ex));
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.error(500, "An unexpected error occurred"));
    }

    private String getStackTrace(Exception ex) {
        StringWriter sw = new StringWriter();
        ex.printStackTrace(new PrintWriter(sw));
        return sw.toString();
    }

    private Map<String, String> toConstraintError(ConstraintViolation<?> violation) {
        String field = violation.getPropertyPath() == null ? "request" : violation.getPropertyPath().toString();
        return Map.of(
                "field", field,
                "message", violation.getMessage()
        );
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
        if (lower.contains("document_type")) {
            return "documentType";
        }
        if (lower.contains("document_no")) {
            return "documentNo";
        }
        if (lower.contains("supplier_code")) {
            return "supplierCode";
        }
        if (lower.contains("supplier_contract")) {
            return "supplierContract";
        }
        if (lower.contains("item_code")) {
            return "itemCode";
        }
        return "payload";
    }
}
