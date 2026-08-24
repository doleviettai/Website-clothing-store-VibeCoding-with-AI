package com.clothingstore.backend.service;

import com.clothingstore.backend.dto.request.UserRequest;
import com.clothingstore.backend.dto.response.ApiResponse;
import com.clothingstore.backend.dto.response.PageResponse;
import com.clothingstore.backend.dto.response.UserResponse;
import org.springframework.web.multipart.MultipartFile;

/**
 * Service quản lý Tài khoản Người Dùng.
 */
public interface UserService {

    /**
     * Admin: Lấy danh sách người dùng phân trang, tìm kiếm & lọc status/role.
     */
    ApiResponse<PageResponse<UserResponse>> getAdminUsers(
            String keyword, String status, String roleName, int page, int size
    );

    /**
     * Lấy thông tin chi tiết người dùng theo ID.
     */
    ApiResponse<UserResponse> getUserById(Long id);

    /**
     * Admin: Tạo tài khoản người dùng mới (upload avatar lên Cloudinary FashionShop2/users nếu có file).
     */
    ApiResponse<UserResponse> createUser(UserRequest request, MultipartFile imageFile);

    /**
     * Admin: Cập nhật thông tin người dùng.
     */
    ApiResponse<UserResponse> updateUser(Long id, UserRequest request, MultipartFile imageFile);

    /**
     * Admin: Xóa mềm tài khoản người dùng.
     */
    ApiResponse<Void> deleteUser(Long id);
}
