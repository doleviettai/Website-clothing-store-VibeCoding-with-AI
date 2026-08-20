package com.clothingstore.backend.exception;

import com.clothingstore.backend.dto.response.ApiResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.HashMap;
import java.util.Map;

/**
 * Xử lý tập trung tất cả exception trong ứng dụng.
 *
 * @RestControllerAdvice: áp dụng cho tất cả @RestController.
 * Mọi exception không được xử lý trong controller sẽ đến đây.
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    /**
     * Xử lý lỗi nghiệp vụ tự định nghĩa (AppException).
     * Ví dụ: email trùng, mật khẩu sai, tài khoản bị khóa...
     */
    @ExceptionHandler(AppException.class)
    public ResponseEntity<ApiResponse<Void>> handleAppException(AppException ex) {
        return ResponseEntity
                .status(ex.getStatus())
                .body(ApiResponse.error(ex.getMessage()));
    }

    /**
     * Xử lý lỗi validation từ @Valid trong Controller.
     * Ví dụ: email sai định dạng, mật khẩu quá ngắn...
     *
     * Trả về map: { "email": "Email không đúng định dạng", "password": "..." }
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse<Void>> handleValidationException(
            MethodArgumentNotValidException ex) {

        Map<String, String> errors = new HashMap<>();

        for (FieldError fieldError : ex.getBindingResult().getFieldErrors()) {
            errors.put(fieldError.getField(), fieldError.getDefaultMessage());
        }

        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(ApiResponse.validationError("Dữ liệu không hợp lệ", errors));
    }

    /**
     * Xử lý lỗi chưa dự đoán trước (lỗi hệ thống).
     * Không trả chi tiết lỗi để tránh lộ thông tin nhạy cảm.
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<Void>> handleGenericException(Exception ex) {
        // Log lỗi thực tế (trong production dùng logger thay System.err)
        System.err.println("[ERROR] Unexpected exception: " + ex.getMessage());
        ex.printStackTrace();

        return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.error("Lỗi hệ thống, vui lòng thử lại sau"));
    }
}
