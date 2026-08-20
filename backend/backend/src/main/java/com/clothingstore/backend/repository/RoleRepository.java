package com.clothingstore.backend.repository;

import com.clothingstore.backend.entity.Role;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Repository cho bảng `roles`.
 */
@Repository
public interface RoleRepository extends JpaRepository<Role, Long> {

    /**
     * Tìm vai trò theo code — dùng khi đăng ký để gán vai trò CLIENT.
     * Ví dụ: findByCode("CLIENT") hoặc findByCode("ADMIN")
     */
    Optional<Role> findByCode(String code);
}
