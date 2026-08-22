package com.clothingstore.backend.service;

import com.clothingstore.backend.dto.request.BannerRequest;
import com.clothingstore.backend.dto.response.ApiResponse;
import com.clothingstore.backend.dto.response.BannerResponse;
import com.clothingstore.backend.dto.response.PageResponse;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

/**
 * Service quản lý Banner quảng cáo & Slide.
 */
public interface BannerService {

    /**
     * Admin: Lấy danh sách banner phân trang, tìm kiếm & lọc vị trí.
     */
    ApiResponse<PageResponse<BannerResponse>> getAdminBanners(
            String keyword, String position, String status, int page, int size
    );

    /**
     * Client: Lấy danh sách banner ACTIVE theo vị trí (được sắp xếp sortOrder tăng dần).
     */
    ApiResponse<List<BannerResponse>> getClientBannersByPosition(String position);

    /**
     * Lấy chi tiết banner theo ID.
     */
    ApiResponse<BannerResponse> getBannerById(Long id);

    /**
     * Admin: Tạo banner mới (upload ảnh lên Cloudinary FashionShop2/banners nếu chọn file).
     */
    ApiResponse<BannerResponse> createBanner(BannerRequest request, MultipartFile imageFile);

    /**
     * Admin: Cập nhật banner.
     */
    ApiResponse<BannerResponse> updateBanner(Long id, BannerRequest request, MultipartFile imageFile);

    /**
     * Admin: Xóa mềm banner.
     */
    ApiResponse<Void> deleteBanner(Long id);
}
