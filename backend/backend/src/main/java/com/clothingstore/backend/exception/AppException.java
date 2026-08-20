package com.clothingstore.backend.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

/**
 * Exception tùy chỉnh cho các lỗi nghiệp vụ.
 * Chứa HttpStatus để GlobalExceptionHandler có thể trả đúng HTTP status code.
 * <p>
 * Cách dùng:
 * throw new AppException(HttpStatus.CONFLICT, "Email đã được sử dụng");
 * throw new AppException(HttpStatus.UNAUTHORIZED, "Email hoặc mật khẩu không chính xác");
 */
@Getter
public class AppException extends RuntimeException {

    private final HttpStatus status;

    public AppException(HttpStatus status, String message) {
        super(message);
        this.status = status;
    }
}
