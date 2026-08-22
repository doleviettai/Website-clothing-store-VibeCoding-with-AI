package com.clothingstore.backend.service;

import com.clothingstore.backend.dto.request.BrandRequest;
import com.clothingstore.backend.dto.response.ApiResponse;
import com.clothingstore.backend.dto.response.BrandResponse;
import com.clothingstore.backend.dto.response.PageResponse;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

/**
 * Service quản lý Thương hiệu.
 */
public interface BrandService {

    /**
     * Admin: Lấy danh sách thương hiệu phân trang, tìm kiếm & lọc.
     */
    ApiResponse<PageResponse<BrandResponse>> getAdminBrands(
            String keyword, String status, int page, int size
    );

    /**
     * Lấy toàn bộ danh sách thương hiệu cho dropdown.
     */
    ApiResponse<List<BrandResponse>> getAllBrandsForDropdown();

    /**
     * Client: Lấy danh sách thương hiệu ACTIVE.
     */
    ApiResponse<List<BrandResponse>> getClientActiveBrands();

    /**
     * Lấy chi tiết thương hiệu theo ID.
     */
    ApiResponse<BrandResponse> getBrandById(Long id);

    /**
     * Admin: Tạo thương hiệu mới (tự động upload logo lên Cloudinary FashionShop2/brands nếu có).
     */
    ApiResponse<BrandResponse> createBrand(BrandRequest request, MultipartFile imageFile);

    /**
     * Admin: Cập nhật thương hiệu.
     */
    ApiResponse<BrandResponse> updateBrand(Long id, BrandRequest request, MultipartFile imageFile);

    /**
     * Admin: Xóa mềm thương hiệu.
     */
    ApiResponse<Void> deleteBrand(Long id);
}
