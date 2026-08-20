package com.clothingstore.backend.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import lombok.Getter;

import java.util.Map;

/**
 * Wrapper response chuẩn cho toàn bộ API.
 * <p>
 * Cấu trúc response thành công:
 * { "success": true, "message": "...", "data": {...} }
 * <p>
 * Cấu trúc response lỗi:
 * { "success": false, "message": "...", "data": null }
 * <p>
 * Cấu trúc response validation:
 * { "success": false, "message": "...", "errors": {"email": "..."} }
 */
@Getter
@Builder
// Không serialize các field null vào JSON
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ApiResponse<T> {

    private boolean success;
    private String message;
    private T data;

    // Chỉ có khi validation lỗi — map field → error message
    private Map<String, String> errors;

    // ======= Factory methods =======

    public static <T> ApiResponse<T> success(String message, T data) {
        return ApiResponse.<T>builder()
                .success(true)
                .message(message)
                .data(data)
                .build();
    }

    public static <T> ApiResponse<T> success(String message) {
        return ApiResponse.<T>builder()
                .success(true)
                .message(message)
                .build();
    }

    public static <T> ApiResponse<T> error(String message) {
        return ApiResponse.<T>builder()
                .success(false)
                .message(message)
                .build();
    }

    public static <T> ApiResponse<T> validationError(String message, Map<String, String> errors) {
        return ApiResponse.<T>builder()
                .success(false)
                .message(message)
                .errors(errors)
                .build();
    }
}
