package com.clothingstore.backend.service.impl;

import com.clothingstore.backend.dto.request.CartItemRequest;
import com.clothingstore.backend.dto.request.CartItemUpdateCountRequest;
import com.clothingstore.backend.dto.response.ApiResponse;
import com.clothingstore.backend.dto.response.CartItemResponse;
import com.clothingstore.backend.dto.response.CartResponse;
import com.clothingstore.backend.entity.Cart;
import com.clothingstore.backend.entity.CartItem;
import com.clothingstore.backend.entity.Product;
import com.clothingstore.backend.entity.User;
import com.clothingstore.backend.exception.AppException;
import com.clothingstore.backend.repository.CartItemRepository;
import com.clothingstore.backend.repository.CartRepository;
import com.clothingstore.backend.repository.ProductRepository;
import com.clothingstore.backend.repository.UserRepository;
import com.clothingstore.backend.service.CartService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CartServiceImpl implements CartService {

    private final CartRepository cartRepository;
    private final CartItemRepository cartItemRepository;
    private final UserRepository userRepository;
    private final ProductRepository productRepository;

    @Override
    @Transactional
    public ApiResponse<CartResponse> getCart(String userEmail) {
        User user = getUserByEmail(userEmail);
        Cart cart = getOrCreateCart(user);
        return ApiResponse.success("Lấy thông tin giỏ hàng thành công", toCartResponse(cart));
    }

    @Override
    @Transactional
    public ApiResponse<CartResponse> addToCart(String userEmail, CartItemRequest request) {
        User user = getUserByEmail(userEmail);
        Cart cart = getOrCreateCart(user);

        Product product = productRepository.findByIdAndDeletedAtIsNull(request.getProductId())
                .orElseThrow(() -> new AppException(HttpStatus.NOT_FOUND, "Không tìm thấy sản phẩm với ID: " + request.getProductId()));

        if ("INACTIVE".equalsIgnoreCase(product.getStatus())) {
            throw new AppException(HttpStatus.BAD_REQUEST, "Sản phẩm hiện đang ngưng kinh doanh");
        }

        if (product.getStockQuantity() < request.getQuantity()) {
            throw new AppException(HttpStatus.BAD_REQUEST, "Số lượng tồn kho không đủ (chỉ còn " + product.getStockQuantity() + " sản phẩm)");
        }

        String size = (request.getSize() != null && !request.getSize().isBlank()) ? request.getSize().trim() : null;
        String color = (request.getColor() != null && !request.getColor().isBlank()) ? request.getColor().trim() : null;
        BigDecimal actualPrice = product.getSalePrice() != null ? product.getSalePrice() : product.getPrice();

        // Kiểm tra xem trong cart đã có CartItem trùng product + size + color chưa
        Optional<CartItem> existingItemOpt = cartItemRepository.findByCartIdAndProductIdAndSizeAndColor(
                cart.getId(), product.getId(), size, color
        );

        if (existingItemOpt.isPresent()) {
            // Tăng số lượng quantity
            CartItem existingItem = existingItemOpt.get();
            int newQuantity = existingItem.getQuantity() + request.getQuantity();
            if (product.getStockQuantity() < newQuantity) {
                throw new AppException(HttpStatus.BAD_REQUEST, "Số lượng cộng dồn vượt quá tồn kho (chỉ còn " + product.getStockQuantity() + " sản phẩm)");
            }
            existingItem.setQuantity(newQuantity);
            existingItem.setPrice(actualPrice);
            cartItemRepository.save(existingItem);
        } else {
            // Tạo CartItem mới
            CartItem newItem = CartItem.builder()
                    .cart(cart)
                    .product(product)
                    .quantity(request.getQuantity())
                    .price(actualPrice)
                    .size(size)
                    .color(color)
                    .build();
            cartItemRepository.save(newItem);
            cart.getItems().add(newItem);
        }

        return ApiResponse.success("Đã thêm sản phẩm vào giỏ hàng!", toCartResponse(cart));
    }

    @Override
    @Transactional
    public ApiResponse<CartResponse> updateCartItemQuantity(String userEmail, Long itemId, CartItemUpdateCountRequest request) {
        User user = getUserByEmail(userEmail);
        Cart cart = getOrCreateCart(user);

        CartItem item = cartItemRepository.findById(itemId)
                .orElseThrow(() -> new AppException(HttpStatus.NOT_FOUND, "Không tìm thấy vật phẩm giỏ hàng với ID: " + itemId));

        if (!item.getCart().getId().equals(cart.getId())) {
            throw new AppException(HttpStatus.FORBIDDEN, "Bạn không có quyền chỉnh sửa giỏ hàng của người khác");
        }

        Product product = item.getProduct();
        if (product.getStockQuantity() < request.getQuantity()) {
            throw new AppException(HttpStatus.BAD_REQUEST, "Số lượng tồn kho không đủ (chỉ còn " + product.getStockQuantity() + " sản phẩm)");
        }

        item.setQuantity(request.getQuantity());
        cartItemRepository.save(item);

        return ApiResponse.success("Cập nhật số lượng giỏ hàng thành công", toCartResponse(cart));
    }

    @Override
    @Transactional
    public ApiResponse<CartResponse> removeCartItem(String userEmail, Long itemId) {
        User user = getUserByEmail(userEmail);
        Cart cart = getOrCreateCart(user);

        CartItem item = cartItemRepository.findById(itemId)
                .orElseThrow(() -> new AppException(HttpStatus.NOT_FOUND, "Không tìm thấy vật phẩm giỏ hàng với ID: " + itemId));

        if (!item.getCart().getId().equals(cart.getId())) {
            throw new AppException(HttpStatus.FORBIDDEN, "Bạn không có quyền xóa sản phẩm trong giỏ hàng của người khác");
        }

        cart.getItems().remove(item);
        cartItemRepository.delete(item);

        return ApiResponse.success("Đã xóa sản phẩm khỏi giỏ hàng", toCartResponse(cart));
    }

    private User getUserByEmail(String email) {
        return userRepository.findByEmailAndDeletedAtIsNull(email)
                .orElseThrow(() -> new AppException(HttpStatus.UNAUTHORIZED, "Vui lòng đăng nhập trước khi thao tác giỏ hàng"));
    }

    private Cart getOrCreateCart(User user) {
        return cartRepository.findByUserId(user.getId())
                .orElseGet(() -> {
                    Cart newCart = Cart.builder()
                            .user(user)
                            .items(new ArrayList<>())
                            .build();
                    return cartRepository.save(newCart);
                });
    }

    private CartResponse toCartResponse(Cart cart) {
        List<CartItemResponse> itemResponses = cart.getItems().stream().map(item -> {
            BigDecimal actualPrice = item.getPrice() != null ? item.getPrice() : (item.getProduct().getSalePrice() != null ? item.getProduct().getSalePrice() : item.getProduct().getPrice());
            BigDecimal total = actualPrice.multiply(BigDecimal.valueOf(item.getQuantity()));
            return CartItemResponse.builder()
                    .id(item.getId())
                    .productId(item.getProduct().getId())
                    .productName(item.getProduct().getName())
                    .productSlug(item.getProduct().getSlug())
                    .brandName(item.getProduct().getBrand() != null ? item.getProduct().getBrand().getName() : null)
                    .categoryName(item.getProduct().getCategory() != null ? item.getProduct().getCategory().getName() : null)
                    .thumbnailUrl(item.getProduct().getThumbnailUrl())
                    .quantity(item.getQuantity())
                    .price(actualPrice)
                    .itemTotal(total)
                    .size(item.getSize())
                    .color(item.getColor())
                    .stockQuantity(item.getProduct().getStockQuantity())
                    .build();
        }).collect(Collectors.toList());

        BigDecimal subtotal = itemResponses.stream()
                .map(CartItemResponse::getItemTotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        int totalItems = itemResponses.stream()
                .mapToInt(CartItemResponse::getQuantity)
                .sum();

        return CartResponse.builder()
                .id(cart.getId())
                .userId(cart.getUser().getId())
                .items(itemResponses)
                .totalItems(totalItems)
                .subtotal(subtotal)
                .build();
    }
}
