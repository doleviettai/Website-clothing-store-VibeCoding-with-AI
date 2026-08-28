package com.clothingstore.backend.repository;

import com.clothingstore.backend.entity.UserFavorite;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserFavoriteRepository extends JpaRepository<UserFavorite, Long> {

    boolean existsByUserIdAndProductId(Long userId, Long productId);

    Optional<UserFavorite> findByUserIdAndProductId(Long userId, Long productId);

    List<UserFavorite> findByUserIdOrderByCreatedAtDesc(Long userId);

    void deleteByUserIdAndProductId(Long userId, Long productId);
}
