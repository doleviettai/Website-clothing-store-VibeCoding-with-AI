package com.clothingstore.backend.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

/**
 * Request DTO khi Admin tạo mới hoặc cập nhật Người dùng.
 */
@Getter
@Setter
public class UserRequest {

    @NotBlank(message = "Tên người dùng không được để trống")
    @Size(max = 100, message = "Tên người dùng tối đa 100 ký tự")
    private String fullName;

    @NotBlank(message = "Email không được để trống")
    @Email(message = "Email không hợp lệ")
    private String email;

    private String phone;

    private String password;

    private String avatarUrl;

    private String status = "ACTIVE";

    private String roleName = "ROLE_USER";
}
