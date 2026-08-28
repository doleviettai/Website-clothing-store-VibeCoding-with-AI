package com.clothingstore.backend.service;

import com.clothingstore.backend.dto.response.ApiResponse;
import com.clothingstore.backend.dto.response.FavoriteResponse;

import java.util.List;
import java.util.Map;

public interface FavoriteService {

    ApiResponse<FavoriteResponse> addFavorite(String userEmail, Long productId);

    ApiResponse<Void> removeFavorite(String userEmail, Long productId);

    ApiResponse<Map<String, Boolean>> checkFavorite(String userEmail, Long productId);

    ApiResponse<List<FavoriteResponse>> getUserFavorites(String userEmail);
}
