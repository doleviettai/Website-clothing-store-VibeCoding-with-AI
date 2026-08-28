package com.clothingstore.backend.service.impl;

import com.clothingstore.backend.dto.request.ProductRequest;
import com.clothingstore.backend.dto.response.ApiResponse;
import com.clothingstore.backend.dto.response.PageResponse;
import com.clothingstore.backend.dto.response.ProductResponse;
import com.clothingstore.backend.entity.Brand;
import com.clothingstore.backend.entity.Category;
import com.clothingstore.backend.entity.Product;
import com.clothingstore.backend.exception.AppException;
import com.clothingstore.backend.repository.BrandRepository;
import com.clothingstore.backend.repository.CategoryRepository;
import com.clothingstore.backend.repository.ProductRepository;
import com.clothingstore.backend.service.CloudinaryService;
import com.clothingstore.backend.service.ProductService;
import jakarta.persistence.criteria.Predicate;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.text.Normalizer;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * Cài đặt ProductService.
 */
@Service
@RequiredArgsConstructor
public class ProductServiceImpl implements ProductService {

    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;
    private final BrandRepository brandRepository;
    private final CloudinaryService cloudinaryService;

    private static final String CLOUDINARY_PRODUCT_FOLDER = "FashionShop2/products";

    @Override
    public ApiResponse<PageResponse<ProductResponse>> getAdminProducts(
            String keyword, Long categoryId, Long brandId, String status, int page, int size
    ) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("id").descending());

        Specification<Product> spec = (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            // Chỉ lấy record chưa xóa mềm
            predicates.add(cb.isNull(root.get("deletedAt")));

            if (keyword != null && !keyword.isBlank()) {
                String likeKeyword = "%" + keyword.trim().toLowerCase() + "%";
                predicates.add(cb.or(
                        cb.like(cb.lower(root.get("name")), likeKeyword),
                        cb.like(cb.lower(root.get("slug")), likeKeyword)
                ));
            }

            if (categoryId != null) {
                predicates.add(cb.equal(root.get("category").get("id"), categoryId));
            }

            if (brandId != null) {
                predicates.add(cb.equal(root.get("brand").get("id"), brandId));
            }

            if (status != null && !status.isBlank()) {
                predicates.add(cb.equal(root.get("status"), status.trim()));
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };

        Page<Product> productPage = productRepository.findAll(spec, pageable);
        Page<ProductResponse> responsePage = productPage.map(this::toProductResponse);

        return ApiResponse.success("Lấy danh sách sản phẩm thành công", PageResponse.from(responsePage));
    }

    @Override
    public ApiResponse<PageResponse<ProductResponse>> getClientProducts(
            String keyword, Long categoryId, Long brandId, String sort, int page, int size
    ) {
        // Xử lý Sắp xếp sort (newest, price_asc, price_desc)
        Sort sortObj = Sort.by(Sort.Direction.DESC, "createdAt");
        if ("price_asc".equalsIgnoreCase(sort)) {
            sortObj = Sort.by(Sort.Direction.ASC, "price");
        } else if ("price_desc".equalsIgnoreCase(sort)) {
            sortObj = Sort.by(Sort.Direction.DESC, "price");
        }

        Pageable pageable = PageRequest.of(page, size, sortObj);

        Specification<Product> spec = (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            // Chỉ lấy sản phẩm ACTIVE và chưa xóa mềm
            predicates.add(cb.isNull(root.get("deletedAt")));
            predicates.add(cb.equal(root.get("status"), "ACTIVE"));

            // Tìm kiếm realtime không phân biệt hoa/thường theo tên hoặc slug
            if (keyword != null && !keyword.isBlank()) {
                String likeKeyword = "%" + keyword.trim().toLowerCase() + "%";
                predicates.add(cb.or(
                        cb.like(cb.lower(root.get("name")), likeKeyword),
                        cb.like(cb.lower(root.get("slug")), likeKeyword)
                ));
            }

            // Lọc theo Chuyên mục categoryId
            if (categoryId != null) {
                predicates.add(cb.equal(root.get("category").get("id"), categoryId));
            }

            // Lọc theo Thương hiệu brandId
            if (brandId != null) {
                predicates.add(cb.equal(root.get("brand").get("id"), brandId));
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };

        Page<Product> productPage = productRepository.findAll(spec, pageable);
        Page<ProductResponse> responsePage = productPage.map(this::toProductResponse);

        return ApiResponse.success("Lấy danh sách sản phẩm cửa hàng thành công", PageResponse.from(responsePage));
    }

    @Override
    public ApiResponse<List<ProductResponse>> getClientActiveProducts() {
        List<Product> products = productRepository.findAllByStatusAndDeletedAtIsNullOrderByIdDesc("ACTIVE");
        List<ProductResponse> list = products.stream().map(this::toProductResponse).collect(Collectors.toList());
        return ApiResponse.success("Lấy danh sách sản phẩm thành công", list);
    }

    @Override
    public ApiResponse<ProductResponse> getProductById(Long id) {
        Product product = productRepository.findByIdAndDeletedAtIsNull(id)
                .orElseThrow(() -> new AppException(HttpStatus.NOT_FOUND, "Không tìm thấy sản phẩm với ID: " + id));
        return ApiResponse.success("Lấy thông tin sản phẩm thành công", toProductResponse(product));
    }

    @Override
    @Transactional
    public ApiResponse<ProductResponse> createProduct(ProductRequest request, MultipartFile imageFile) {
        String slug = (request.getSlug() != null && !request.getSlug().isBlank())
                ? toSlug(request.getSlug())
                : toSlug(request.getName());

        if (productRepository.existsBySlugAndDeletedAtIsNull(slug)) {
            throw new AppException(HttpStatus.CONFLICT, "Slug sản phẩm '" + slug + "' đã tồn tại");
        }

        Category category = null;
        if (request.getCategoryId() != null) {
            category = categoryRepository.findByIdAndDeletedAtIsNull(request.getCategoryId())
                    .orElse(null);
        }

        Brand brand = null;
        if (request.getBrandId() != null) {
            brand = brandRepository.findByIdAndDeletedAtIsNull(request.getBrandId())
                    .orElse(null);
        }

        String uploadedThumbnailUrl = request.getThumbnailUrl();
        if (imageFile != null && !imageFile.isEmpty()) {
            uploadedThumbnailUrl = uploadFileToCloudinary(imageFile);
        }

        Product product = Product.builder()
                .name(request.getName().trim())
                .slug(slug)
                .category(category)
                .brand(brand)
                .price(request.getPrice())
                .salePrice(request.getSalePrice())
                .stockQuantity(request.getStockQuantity() != null ? request.getStockQuantity() : 0)
                .availableSizes(request.getAvailableSizes())
                .availableColors(request.getAvailableColors())
                .thumbnailUrl(uploadedThumbnailUrl)
                .shortDescription(request.getShortDescription())
                .description(request.getDescription())
                .status(request.getStatus() != null ? request.getStatus() : "ACTIVE")
                .isFeatured(request.getIsFeatured() != null ? request.getIsFeatured() : false)
                .favoriteCount(request.getFavoriteCount() != null ? request.getFavoriteCount() : 0)
                .averageRating(request.getAverageRating() != null ? request.getAverageRating() : new java.math.BigDecimal("5.00"))
                .build();

        productRepository.save(product);
        return ApiResponse.success("Tạo sản phẩm mới thành công", toProductResponse(product));
    }

    @Override
    @Transactional
    public ApiResponse<ProductResponse> updateProduct(Long id, ProductRequest request, MultipartFile imageFile) {
        Product product = productRepository.findByIdAndDeletedAtIsNull(id)
                .orElseThrow(() -> new AppException(HttpStatus.NOT_FOUND, "Không tìm thấy sản phẩm với ID: " + id));

        String slug = (request.getSlug() != null && !request.getSlug().isBlank())
                ? toSlug(request.getSlug())
                : toSlug(request.getName());

        if (productRepository.existsBySlugAndIdNotAndDeletedAtIsNull(slug, id)) {
            throw new AppException(HttpStatus.CONFLICT, "Slug sản phẩm '" + slug + "' đã tồn tại");
        }

        if (request.getCategoryId() != null) {
            Category category = categoryRepository.findByIdAndDeletedAtIsNull(request.getCategoryId()).orElse(null);
            product.setCategory(category);
        } else {
            product.setCategory(null);
        }

        if (request.getBrandId() != null) {
            Brand brand = brandRepository.findByIdAndDeletedAtIsNull(request.getBrandId()).orElse(null);
            product.setBrand(brand);
        } else {
            product.setBrand(null);
        }

        if (imageFile != null && !imageFile.isEmpty()) {
            String newThumbnailUrl = uploadFileToCloudinary(imageFile);
            product.setThumbnailUrl(newThumbnailUrl);
        } else if (request.getThumbnailUrl() != null) {
            product.setThumbnailUrl(request.getThumbnailUrl());
        }

        product.setName(request.getName().trim());
        product.setSlug(slug);
        product.setPrice(request.getPrice());
        product.setSalePrice(request.getSalePrice());
        if (request.getStockQuantity() != null) product.setStockQuantity(request.getStockQuantity());
        product.setAvailableSizes(request.getAvailableSizes());
        product.setAvailableColors(request.getAvailableColors());
        product.setShortDescription(request.getShortDescription());
        product.setDescription(request.getDescription());
        if (request.getStatus() != null) product.setStatus(request.getStatus());
        if (request.getIsFeatured() != null) product.setIsFeatured(request.getIsFeatured());
        if (request.getFavoriteCount() != null) product.setFavoriteCount(request.getFavoriteCount());
        if (request.getAverageRating() != null) product.setAverageRating(request.getAverageRating());

        productRepository.save(product);
        return ApiResponse.success("Cập nhật sản phẩm thành công", toProductResponse(product));
    }

    @Override
    @Transactional
    public ApiResponse<Void> deleteProduct(Long id) {
        Product product = productRepository.findByIdAndDeletedAtIsNull(id)
                .orElseThrow(() -> new AppException(HttpStatus.NOT_FOUND, "Không tìm thấy sản phẩm với ID: " + id));

        product.setDeletedAt(LocalDateTime.now());
        product.setStatus("INACTIVE");
        productRepository.save(product);

        return ApiResponse.success("Xóa sản phẩm thành công", null);
    }

    // ================= Private helpers =================

    private ProductResponse toProductResponse(Product p) {
        return ProductResponse.builder()
                .id(p.getId())
                .name(p.getName())
                .slug(p.getSlug())
                .categoryId(p.getCategory() != null ? p.getCategory().getId() : null)
                .categoryName(p.getCategory() != null ? p.getCategory().getName() : null)
                .brandId(p.getBrand() != null ? p.getBrand().getId() : null)
                .brandName(p.getBrand() != null ? p.getBrand().getName() : null)
                .price(p.getPrice())
                .salePrice(p.getSalePrice())
                .stockQuantity(p.getStockQuantity())
                .availableSizes(p.getAvailableSizes())
                .availableColors(p.getAvailableColors())
                .thumbnailUrl(p.getThumbnailUrl())
                .shortDescription(p.getShortDescription())
                .description(p.getDescription())
                .status(p.getStatus())
                .isFeatured(p.getIsFeatured())
                .favoriteCount(p.getFavoriteCount())
                .reviewCount(p.getReviewCount())
                .averageRating(p.getAverageRating())
                .createdAt(p.getCreatedAt())
                .updatedAt(p.getUpdatedAt())
                .build();
    }

    private String uploadFileToCloudinary(MultipartFile file) {
        try {
            Map uploadResult = cloudinaryService.uploadImage(file, CLOUDINARY_PRODUCT_FOLDER);
            return (String) uploadResult.get("secure_url");
        } catch (IOException e) {
            throw new AppException(HttpStatus.INTERNAL_SERVER_ERROR, "Lỗi khi tải ảnh sản phẩm lên Cloudinary: " + e.getMessage());
        }
    }

    private String toSlug(String input) {
        if (input == null || input.isBlank()) return "";
        String nowhitespace = Pattern.compile("\\s+").matcher(input.trim()).replaceAll("-");
        String normalized = Normalizer.normalize(nowhitespace, Normalizer.Form.NFD);
        String slug = Pattern.compile("\\p{InCombiningDiacriticalMarks}+").matcher(normalized).replaceAll("");
        slug = slug.replaceAll("[đĐ]", "d");
        return slug.toLowerCase(Locale.ENGLISH).replaceAll("[^a-z0-9-]", "").replaceAll("-+", "-");
    }
}
