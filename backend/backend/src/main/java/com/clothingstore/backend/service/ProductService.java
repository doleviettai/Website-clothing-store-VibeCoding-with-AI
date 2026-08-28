package com.clothingstore.backend.service;

import com.clothingstore.backend.dto.request.ProductRequest;
import com.clothingstore.backend.dto.response.ApiResponse;
import com.clothingstore.backend.dto.response.PageResponse;
import com.clothingstore.backend.dto.response.ProductResponse;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

/**
 * Service quản lý Sản phẩm.
 */
public interface ProductService {

    /**
     * Admin: Lấy danh sách sản phẩm phân trang, tìm kiếm & lọc (chuyên mục, thương hiệu, trạng thái).
     */
    ApiResponse<PageResponse<ProductResponse>> getAdminProducts(
            String keyword, Long categoryId, Long brandId, String status, int page, int size
    );

    /**
     * Client: Lấy danh sách sản phẩm public có phân trang, tìm kiếm realtime, lọc category, brand và sắp xếp sort.
     */
    ApiResponse<PageResponse<ProductResponse>> getClientProducts(
            String keyword, Long categoryId, Long brandId, String sort, int page, int size
    );

    /**
     * Client: Lấy danh sách tất cả sản phẩm đang hoạt động (không phân trang).
     */
    ApiResponse<List<ProductResponse>> getClientActiveProducts();

    /**
     * Lấy chi tiết sản phẩm theo ID.
     */
    ApiResponse<ProductResponse> getProductById(Long id);

    /**
     * Admin: Tạo sản phẩm mới (tự động upload ảnh đại diện lên Cloudinary FashionShop2/products nếu chọn file).
     */
    ApiResponse<ProductResponse> createProduct(ProductRequest request, MultipartFile imageFile);

    /**
     * Admin: Cập nhật sản phẩm.
     */
    ApiResponse<ProductResponse> updateProduct(Long id, ProductRequest request, MultipartFile imageFile);

    /**
     * Admin: Xóa mềm sản phẩm.
     */
    ApiResponse<Void> deleteProduct(Long id);
}
