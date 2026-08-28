package com.clothingstore.backend.service;

import com.clothingstore.backend.dto.request.CartItemRequest;
import com.clothingstore.backend.dto.request.CartItemUpdateCountRequest;
import com.clothingstore.backend.dto.response.ApiResponse;
import com.clothingstore.backend.dto.response.CartResponse;

public interface CartService {

    ApiResponse<CartResponse> getCart(String userEmail);

    ApiResponse<CartResponse> addToCart(String userEmail, CartItemRequest request);

    ApiResponse<CartResponse> updateCartItemQuantity(String userEmail, Long itemId, CartItemUpdateCountRequest request);

    ApiResponse<CartResponse> removeCartItem(String userEmail, Long itemId);
}
