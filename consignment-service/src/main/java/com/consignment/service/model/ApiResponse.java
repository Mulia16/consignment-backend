package com.consignment.service.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({ "message", "status", "data", "meta" })
public record ApiResponse<T>(
        String message,
        int status,
        T data,
        PageMeta meta
) {
    // ── 2xx ───────────────────────────────────────────────────────────────────

    public static <T> ApiResponse<T> success(T data) {
        return new ApiResponse<>("success", 200, data, null);
    }

    public static <T> ApiResponse<T> success(String message, T data) {
        return new ApiResponse<>(message, 201, data, null);
    }

    public static <T> ApiResponse<T> ok(String message, T data) {
        return new ApiResponse<>(message, 200, data, null);
    }

    public static <T> ApiResponse<T> paginated(T data, PageMeta meta) {
        return new ApiResponse<>("success", 200, data, meta);
    }

    // ── 4xx / 5xx ─────────────────────────────────────────────────────────────

    public static ApiResponse<Void> error(int status, String message) {
        return new ApiResponse<>(message, status, null, null);
    }
}
