package com.clothingstore.backend.service.impl;

import com.clothingstore.backend.dto.response.ApiResponse;
import com.clothingstore.backend.dto.response.FavoriteResponse;
import com.clothingstore.backend.entity.Product;
import com.clothingstore.backend.entity.User;
import com.clothingstore.backend.entity.UserFavorite;
import com.clothingstore.backend.exception.AppException;
import com.clothingstore.backend.repository.ProductRepository;
import com.clothingstore.backend.repository.UserFavoriteRepository;
import com.clothingstore.backend.repository.UserRepository;
import com.clothingstore.backend.service.FavoriteService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class FavoriteServiceImpl implements FavoriteService {

    private final UserFavoriteRepository favoriteRepository;
    private final UserRepository userRepository;
    private final ProductRepository productRepository;

    @Override
    @Transactional
    public ApiResponse<FavoriteResponse> addFavorite(String userEmail, Long productId) {
        User user = getUserByEmail(userEmail);
        Product product = productRepository.findByIdAndDeletedAtIsNull(productId)
                .orElseThrow(() -> new AppException(HttpStatus.NOT_FOUND, "Không tìm thấy sản phẩm với ID: " + productId));

        if (favoriteRepository.existsByUserIdAndProductId(user.getId(), productId)) {
            throw new AppException(HttpStatus.CONFLICT, "Sản phẩm này đã có trong danh sách yêu thích của bạn");
        }

        UserFavorite favorite = UserFavorite.builder()
                .user(user)
                .product(product)
                .build();

        favoriteRepository.save(favorite);

        // Tăng favoriteCount của sản phẩm
        product.setFavoriteCount(product.getFavoriteCount() + 1);
        productRepository.save(product);

        return ApiResponse.success("Đã thêm sản phẩm vào danh sách yêu thích", toFavoriteResponse(favorite));
    }

    @Override
    @Transactional
    public ApiResponse<Void> removeFavorite(String userEmail, Long productId) {
        User user = getUserByEmail(userEmail);
        UserFavorite favorite = favoriteRepository.findByUserIdAndProductId(user.getId(), productId)
                .orElseThrow(() -> new AppException(HttpStatus.NOT_FOUND, "Sản phẩm chưa có trong danh sách yêu thích"));

        favoriteRepository.delete(favorite);

        // Giảm favoriteCount của sản phẩm
        Product product = favorite.getProduct();
        if (product != null && product.getFavoriteCount() > 0) {
            product.setFavoriteCount(product.getFavoriteCount() - 1);
            productRepository.save(product);
        }

        return ApiResponse.success("Đã xóa sản phẩm khỏi danh sách yêu thích", null);
    }

    @Override
    @Transactional(readOnly = true)
    public ApiResponse<Map<String, Boolean>> checkFavorite(String userEmail, Long productId) {
        if (userEmail == null || userEmail.isBlank()) {
            return ApiResponse.success("Kiểm tra yêu thích thành công", Collections.singletonMap("favorite", false));
        }

        User user = userRepository.findByEmailAndDeletedAtIsNull(userEmail).orElse(null);
        if (user == null) {
            return ApiResponse.success("Kiểm tra yêu thích thành công", Collections.singletonMap("favorite", false));
        }

        boolean isFavorite = favoriteRepository.existsByUserIdAndProductId(user.getId(), productId);
        return ApiResponse.success("Kiểm tra yêu thích thành công", Collections.singletonMap("favorite", isFavorite));
    }

    @Override
    @Transactional(readOnly = true)
    public ApiResponse<List<FavoriteResponse>> getUserFavorites(String userEmail) {
        User user = getUserByEmail(userEmail);
        List<UserFavorite> list = favoriteRepository.findByUserIdOrderByCreatedAtDesc(user.getId());
        List<FavoriteResponse> responses = list.stream().map(this::toFavoriteResponse).collect(Collectors.toList());
        return ApiResponse.success("Lấy danh sách yêu thích thành công", responses);
    }

    private User getUserByEmail(String email) {
        return userRepository.findByEmailAndDeletedAtIsNull(email)
                .orElseThrow(() -> new AppException(HttpStatus.UNAUTHORIZED, "Người dùng chưa đăng nhập hoặc không khả dụng"));
    }

    private FavoriteResponse toFavoriteResponse(UserFavorite f) {
        Product p = f.getProduct();
        return FavoriteResponse.builder()
                .id(f.getId())
                .productId(p.getId())
                .productName(p.getName())
                .productSlug(p.getSlug())
                .brandName(p.getBrand() != null ? p.getBrand().getName() : null)
                .categoryName(p.getCategory() != null ? p.getCategory().getName() : null)
                .price(p.getPrice())
                .salePrice(p.getSalePrice())
                .thumbnailUrl(p.getThumbnailUrl())
                .averageRating(p.getAverageRating())
                .createdAt(f.getCreatedAt())
                .build();
    }
}
