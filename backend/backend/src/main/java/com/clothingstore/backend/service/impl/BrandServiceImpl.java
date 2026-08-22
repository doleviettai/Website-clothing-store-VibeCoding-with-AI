package com.clothingstore.backend.service.impl;

import com.clothingstore.backend.dto.request.BrandRequest;
import com.clothingstore.backend.dto.response.ApiResponse;
import com.clothingstore.backend.dto.response.BrandResponse;
import com.clothingstore.backend.dto.response.PageResponse;
import com.clothingstore.backend.entity.Brand;
import com.clothingstore.backend.exception.AppException;
import com.clothingstore.backend.repository.BrandRepository;
import com.clothingstore.backend.service.BrandService;
import com.clothingstore.backend.service.CloudinaryService;
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
 * Cài đặt BrandService.
 */
@Service
@RequiredArgsConstructor
public class BrandServiceImpl implements BrandService {

    private final BrandRepository brandRepository;
    private final CloudinaryService cloudinaryService;

    private static final String CLOUDINARY_BRAND_FOLDER = "FashionShop2/brands";

    @Override
    public ApiResponse<PageResponse<BrandResponse>> getAdminBrands(
            String keyword, String status, int page, int size
    ) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("id").descending());

        Specification<Brand> spec = (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            // Chỉ lấy record chưa bị xóa mềm
            predicates.add(cb.isNull(root.get("deletedAt")));

            if (keyword != null && !keyword.isBlank()) {
                String likeKeyword = "%" + keyword.trim().toLowerCase() + "%";
                predicates.add(cb.or(
                        cb.like(cb.lower(root.get("name")), likeKeyword),
                        cb.like(cb.lower(root.get("slug")), likeKeyword)
                ));
            }

            if (status != null && !status.isBlank()) {
                predicates.add(cb.equal(root.get("status"), status.trim()));
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };

        Page<Brand> brandPage = brandRepository.findAll(spec, pageable);
        Page<BrandResponse> responsePage = brandPage.map(this::toBrandResponse);

        return ApiResponse.success("Lấy danh sách thương hiệu thành công", PageResponse.from(responsePage));
    }

    @Override
    public ApiResponse<List<BrandResponse>> getAllBrandsForDropdown() {
        List<Brand> brands = brandRepository.findAllByDeletedAtIsNullOrderByNameAsc();
        List<BrandResponse> list = brands.stream().map(this::toBrandResponse).collect(Collectors.toList());
        return ApiResponse.success("Lấy danh sách thương hiệu thành công", list);
    }

    @Override
    public ApiResponse<List<BrandResponse>> getClientActiveBrands() {
        List<Brand> brands = brandRepository.findAllByStatusAndDeletedAtIsNullOrderByNameAsc("ACTIVE");
        List<BrandResponse> list = brands.stream().map(this::toBrandResponse).collect(Collectors.toList());
        return ApiResponse.success("Lấy danh sách thương hiệu thành công", list);
    }

    @Override
    public ApiResponse<BrandResponse> getBrandById(Long id) {
        Brand brand = brandRepository.findByIdAndDeletedAtIsNull(id)
                .orElseThrow(() -> new AppException(HttpStatus.NOT_FOUND, "Không tìm thấy thương hiệu với ID: " + id));
        return ApiResponse.success("Lấy thông tin thương hiệu thành công", toBrandResponse(brand));
    }

    @Override
    @Transactional
    public ApiResponse<BrandResponse> createBrand(BrandRequest request, MultipartFile imageFile) {
        String slug = (request.getSlug() != null && !request.getSlug().isBlank())
                ? toSlug(request.getSlug())
                : toSlug(request.getName());

        if (brandRepository.existsBySlugAndDeletedAtIsNull(slug)) {
            throw new AppException(HttpStatus.CONFLICT, "Slug thương hiệu '" + slug + "' đã tồn tại");
        }

        String uploadedLogoUrl = request.getLogoUrl();
        if (imageFile != null && !imageFile.isEmpty()) {
            uploadedLogoUrl = uploadFileToCloudinary(imageFile);
        }

        Brand brand = Brand.builder()
                .name(request.getName().trim())
                .slug(slug)
                .description(request.getDescription())
                .logoUrl(uploadedLogoUrl)
                .status(request.getStatus() != null ? request.getStatus() : "ACTIVE")
                .build();

        brandRepository.save(brand);
        return ApiResponse.success("Tạo thương hiệu thành công", toBrandResponse(brand));
    }

    @Override
    @Transactional
    public ApiResponse<BrandResponse> updateBrand(Long id, BrandRequest request, MultipartFile imageFile) {
        Brand brand = brandRepository.findByIdAndDeletedAtIsNull(id)
                .orElseThrow(() -> new AppException(HttpStatus.NOT_FOUND, "Không tìm thấy thương hiệu với ID: " + id));

        String slug = (request.getSlug() != null && !request.getSlug().isBlank())
                ? toSlug(request.getSlug())
                : toSlug(request.getName());

        if (brandRepository.existsBySlugAndIdNotAndDeletedAtIsNull(slug, id)) {
            throw new AppException(HttpStatus.CONFLICT, "Slug thương hiệu '" + slug + "' đã tồn tại");
        }

        if (imageFile != null && !imageFile.isEmpty()) {
            String newLogoUrl = uploadFileToCloudinary(imageFile);
            brand.setLogoUrl(newLogoUrl);
        } else if (request.getLogoUrl() != null) {
            brand.setLogoUrl(request.getLogoUrl());
        }

        brand.setName(request.getName().trim());
        brand.setSlug(slug);
        brand.setDescription(request.getDescription());
        if (request.getStatus() != null) brand.setStatus(request.getStatus());

        brandRepository.save(brand);
        return ApiResponse.success("Cập nhật thương hiệu thành công", toBrandResponse(brand));
    }

    @Override
    @Transactional
    public ApiResponse<Void> deleteBrand(Long id) {
        Brand brand = brandRepository.findByIdAndDeletedAtIsNull(id)
                .orElseThrow(() -> new AppException(HttpStatus.NOT_FOUND, "Không tìm thấy thương hiệu với ID: " + id));

        brand.setDeletedAt(LocalDateTime.now());
        brand.setStatus("INACTIVE");
        brandRepository.save(brand);

        return ApiResponse.success("Xóa thương hiệu thành công");
    }

    // ================= Private helpers =================

    private BrandResponse toBrandResponse(Brand b) {
        return BrandResponse.builder()
                .id(b.getId())
                .name(b.getName())
                .slug(b.getSlug())
                .logoUrl(b.getLogoUrl())
                .description(b.getDescription())
                .status(b.getStatus())
                .createdAt(b.getCreatedAt())
                .updatedAt(b.getUpdatedAt())
                .build();
    }

    private String uploadFileToCloudinary(MultipartFile file) {
        try {
            Map uploadResult = cloudinaryService.uploadImage(file, CLOUDINARY_BRAND_FOLDER);
            return (String) uploadResult.get("secure_url");
        } catch (IOException e) {
            throw new AppException(HttpStatus.INTERNAL_SERVER_ERROR, "Lỗi khi tải logo lên Cloudinary: " + e.getMessage());
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
