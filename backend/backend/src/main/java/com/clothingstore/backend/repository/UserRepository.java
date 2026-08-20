package com.clothingstore.backend.repository;

import com.clothingstore.backend.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Repository thực hiện các truy vấn liên quan đến bảng `users`.
 * Spring Data JPA tự động tạo implementation từ tên phương thức.
 */
@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    /**
     * Tìm user theo email — dùng khi đăng nhập.
     * Chỉ trả user chưa bị xóa mềm (deleted_at IS NULL).
     */
    Optional<User> findByEmailAndDeletedAtIsNull(String email);

    /**
     * Kiểm tra email đã tồn tại chưa — dùng khi đăng ký.
     * Kiểm tra cả tài khoản đã xóa mềm (tránh đăng ký lại email cũ).
     */
    boolean existsByEmail(String email);

    /**
     * Kiểm tra số điện thoại đã tồn tại chưa — dùng khi đăng ký.
     */
    boolean existsByPhone(String phone);
}
