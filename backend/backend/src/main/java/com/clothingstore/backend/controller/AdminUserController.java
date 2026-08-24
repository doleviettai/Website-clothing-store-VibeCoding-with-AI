package com.clothingstore.backend.controller;

import com.clothingstore.backend.dto.request.UserRequest;
import com.clothingstore.backend.dto.response.ApiResponse;
import com.clothingstore.backend.dto.response.PageResponse;
import com.clothingstore.backend.dto.response.UserResponse;
import com.clothingstore.backend.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

/**
 * Controller Admin cho Quản lý Người Dùng (Full CRUD + Multipart upload avatar Cloudinary).
 * Base path: /api/v1/admin/users
 */
@RestController
@RequestMapping("/api/v1/admin/users")
@PreAuthorize("hasRole('ADMIN')")
@RequiredArgsConstructor
public class AdminUserController {

    private final UserService userService;

    /**
     * GET /api/v1/admin/users
     * Lấy danh sách người dùng phân trang, tìm kiếm & lọc theo status/role.
     */
    @GetMapping
    public ResponseEntity<ApiResponse<PageResponse<UserResponse>>> getAdminUsers(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String roleName,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        return ResponseEntity.ok(userService.getAdminUsers(keyword, status, roleName, page, size));
    }

    /**
     * GET /api/v1/admin/users/{id}
     */
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<UserResponse>> getUserById(@PathVariable Long id) {
        return ResponseEntity.ok(userService.getUserById(id));
    }

    /**
     * POST /api/v1/admin/users
     * Tạo tài khoản người dùng mới (Multipart/form-data chọn avatar upload Cloudinary).
     */
    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ApiResponse<UserResponse>> createUser(
            @Valid @RequestPart("user") UserRequest request,
            @RequestPart(value = "image", required = false) MultipartFile imageFile
    ) {
        ApiResponse<UserResponse> response = userService.createUser(request, imageFile);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * PUT /api/v1/admin/users/{id}
     * Cập nhật thông tin người dùng.
     */
    @PutMapping(value = "/{id}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ApiResponse<UserResponse>> updateUser(
            @PathVariable Long id,
            @Valid @RequestPart("user") UserRequest request,
            @RequestPart(value = "image", required = false) MultipartFile imageFile
    ) {
        ApiResponse<UserResponse> response = userService.updateUser(id, request, imageFile);
        return ResponseEntity.ok(response);
    }

    /**
     * DELETE /api/v1/admin/users/{id}
     * Xóa mềm người dùng.
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteUser(@PathVariable Long id) {
        return ResponseEntity.ok(userService.deleteUser(id));
    }
}
