package com.clothingstore.backend.service.impl;

import com.clothingstore.backend.dto.request.UserRequest;
import com.clothingstore.backend.dto.response.ApiResponse;
import com.clothingstore.backend.dto.response.PageResponse;
import com.clothingstore.backend.dto.response.UserResponse;
import com.clothingstore.backend.entity.Role;
import com.clothingstore.backend.entity.User;
import com.clothingstore.backend.exception.AppException;
import com.clothingstore.backend.repository.RoleRepository;
import com.clothingstore.backend.repository.UserRepository;
import com.clothingstore.backend.service.CloudinaryService;
import com.clothingstore.backend.service.UserService;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.Predicate;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Cài đặt UserService.
 */
@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    private final CloudinaryService cloudinaryService;

    private static final String CLOUDINARY_USER_FOLDER = "FashionShop2/users";

    @Override
    public ApiResponse<PageResponse<UserResponse>> getAdminUsers(
            String keyword, String status, String roleName, int page, int size
    ) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("id").descending());

        Specification<User> spec = (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            // Chỉ lấy user chưa xóa mềm
            predicates.add(cb.isNull(root.get("deletedAt")));

            if (keyword != null && !keyword.isBlank()) {
                String likeKeyword = "%" + keyword.trim().toLowerCase() + "%";
                predicates.add(cb.or(
                        cb.like(cb.lower(root.get("fullName")), likeKeyword),
                        cb.like(cb.lower(root.get("email")), likeKeyword),
                        cb.like(cb.lower(root.get("phone")), likeKeyword)
                ));
            }

            if (status != null && !status.isBlank()) {
                predicates.add(cb.equal(root.get("status"), status.trim()));
            }

            if (roleName != null && !roleName.isBlank()) {
                Join<User, Role> roleJoin = root.join("roles");
                predicates.add(cb.equal(roleJoin.get("name"), roleName.trim()));
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };

        Page<User> userPage = userRepository.findAll(spec, pageable);
        Page<UserResponse> responsePage = userPage.map(this::toUserResponse);

        return ApiResponse.success("Lấy danh sách người dùng thành công", PageResponse.from(responsePage));
    }

    @Override
    public ApiResponse<UserResponse> getUserById(Long id) {
        User user = userRepository.findByIdAndDeletedAtIsNull(id)
                .orElseThrow(() -> new AppException(HttpStatus.NOT_FOUND, "Không tìm thấy người dùng với ID: " + id));
        return ApiResponse.success("Lấy thông tin người dùng thành công", toUserResponse(user));
    }

    @Override
    @Transactional
    public ApiResponse<UserResponse> createUser(UserRequest request, MultipartFile imageFile) {
        String email = request.getEmail().trim().toLowerCase();
        if (userRepository.existsByEmail(email)) {
            throw new AppException(HttpStatus.CONFLICT, "Email '" + email + "' đã tồn tại trên hệ thống");
        }

        if (request.getPhone() != null && !request.getPhone().isBlank() && userRepository.existsByPhone(request.getPhone())) {
            throw new AppException(HttpStatus.CONFLICT, "Số điện thoại '" + request.getPhone() + "' đã được sử dụng");
        }

        if (request.getPassword() == null || request.getPassword().isBlank()) {
            throw new AppException(HttpStatus.BAD_REQUEST, "Mật khẩu không được để trống khi tạo mới tài khoản");
        }

        String uploadedAvatarUrl = request.getAvatarUrl();
        if (imageFile != null && !imageFile.isEmpty()) {
            uploadedAvatarUrl = uploadFileToCloudinary(imageFile);
        }

        String targetRole = (request.getRoleName() != null && !request.getRoleName().isBlank())
                ? request.getRoleName()
                : "ROLE_USER";

        Role role = roleRepository.findByCode(targetRole)
                .or(() -> roleRepository.findByName(targetRole))
                .orElseThrow(() -> new AppException(HttpStatus.NOT_FOUND, "Không tìm thấy vai trò: " + targetRole));

        Set<Role> roles = new HashSet<>();
        roles.add(role);

        User user = User.builder()
                .fullName(request.getFullName().trim())
                .email(email)
                .phone(request.getPhone())
                .passwordHash(passwordEncoder.encode(request.getPassword()))
                .avatarUrl(uploadedAvatarUrl)
                .status(request.getStatus() != null ? request.getStatus() : "ACTIVE")
                .roles(roles)
                .build();

        userRepository.save(user);
        return ApiResponse.success("Tạo tài khoản người dùng mới thành công", toUserResponse(user));
    }

    @Override
    @Transactional
    public ApiResponse<UserResponse> updateUser(Long id, UserRequest request, MultipartFile imageFile) {
        User user = userRepository.findByIdAndDeletedAtIsNull(id)
                .orElseThrow(() -> new AppException(HttpStatus.NOT_FOUND, "Không tìm thấy người dùng với ID: " + id));

        String email = request.getEmail().trim().toLowerCase();
        if (userRepository.existsByEmailAndIdNot(email, id)) {
            throw new AppException(HttpStatus.CONFLICT, "Email '" + email + "' đã được sử dụng bởi tài khoản khác");
        }

        if (request.getPhone() != null && !request.getPhone().isBlank()
                && userRepository.existsByPhoneAndIdNot(request.getPhone(), id)) {
            throw new AppException(HttpStatus.CONFLICT, "Số điện thoại '" + request.getPhone() + "' đã được sử dụng");
        }

        if (imageFile != null && !imageFile.isEmpty()) {
            String newAvatarUrl = uploadFileToCloudinary(imageFile);
            user.setAvatarUrl(newAvatarUrl);
        } else if (request.getAvatarUrl() != null) {
            user.setAvatarUrl(request.getAvatarUrl());
        }

        user.setFullName(request.getFullName().trim());
        user.setEmail(email);
        user.setPhone(request.getPhone());
        if (request.getStatus() != null) user.setStatus(request.getStatus());

        // Đổi mật khẩu nếu có nhập
        if (request.getPassword() != null && !request.getPassword().isBlank()) {
            user.setPasswordHash(passwordEncoder.encode(request.getPassword()));
        }

        // Đổi Vai Trò nếu có chọn
        if (request.getRoleName() != null && !request.getRoleName().isBlank()) {
            Role role = roleRepository.findByCode(request.getRoleName())
                    .or(() -> roleRepository.findByName(request.getRoleName()))
                    .orElseThrow(() -> new AppException(HttpStatus.NOT_FOUND, "Không tìm thấy vai trò: " + request.getRoleName()));
            Set<Role> roles = new HashSet<>();
            roles.add(role);
            user.setRoles(roles);
        }

        userRepository.save(user);
        return ApiResponse.success("Cập nhật người dùng thành công", toUserResponse(user));
    }

    @Override
    @Transactional
    public ApiResponse<Void> deleteUser(Long id) {
        User user = userRepository.findByIdAndDeletedAtIsNull(id)
                .orElseThrow(() -> new AppException(HttpStatus.NOT_FOUND, "Không tìm thấy người dùng với ID: " + id));

        user.setDeletedAt(LocalDateTime.now());
        user.setStatus("INACTIVE");
        userRepository.save(user);

        return ApiResponse.success("Xóa người dùng thành công");
    }

    // ================= Private helpers =================

    private UserResponse toUserResponse(User u) {
        Set<String> roleCodes = u.getRoles() != null
                ? u.getRoles().stream().map(Role::getCode).collect(Collectors.toSet())
                : Collections.emptySet();

        return UserResponse.builder()
                .id(u.getId())
                .fullName(u.getFullName())
                .email(u.getEmail())
                .phone(u.getPhone())
                .avatarUrl(u.getAvatarUrl())
                .status(u.getStatus())
                .roles(roleCodes)
                .lastLoginAt(u.getLastLoginAt())
                .lastLogoutAt(u.getLastLogoutAt())
                .createdAt(u.getCreatedAt())
                .updatedAt(u.getUpdatedAt())
                .build();
    }

    private String uploadFileToCloudinary(MultipartFile file) {
        try {
            Map uploadResult = cloudinaryService.uploadImage(file, CLOUDINARY_USER_FOLDER);
            return (String) uploadResult.get("secure_url");
        } catch (IOException e) {
            throw new AppException(HttpStatus.INTERNAL_SERVER_ERROR, "Lỗi khi tải ảnh avatar lên Cloudinary: " + e.getMessage());
        }
    }
}
