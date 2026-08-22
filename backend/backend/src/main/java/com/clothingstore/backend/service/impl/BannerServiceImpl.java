package com.clothingstore.backend.service.impl;

import com.clothingstore.backend.dto.request.BannerRequest;
import com.clothingstore.backend.dto.response.ApiResponse;
import com.clothingstore.backend.dto.response.BannerResponse;
import com.clothingstore.backend.dto.response.PageResponse;
import com.clothingstore.backend.entity.Banner;
import com.clothingstore.backend.exception.AppException;
import com.clothingstore.backend.repository.BannerRepository;
import com.clothingstore.backend.service.BannerService;
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
 * Cài đặt BannerService.
 */
@Service
@RequiredArgsConstructor
public class BannerServiceImpl implements BannerService {

    private final BannerRepository bannerRepository;
    private final CloudinaryService cloudinaryService;

    private static final String CLOUDINARY_BANNER_FOLDER = "FashionShop2/banners";

    @Override
    public ApiResponse<PageResponse<BannerResponse>> getAdminBanners(
            String keyword, String position, String status, int page, int size
    ) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("sortOrder").ascending().and(Sort.by("id").descending()));

        Specification<Banner> spec = (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            // Chỉ lấy record chưa xóa mềm
            predicates.add(cb.isNull(root.get("deletedAt")));

            if (keyword != null && !keyword.isBlank()) {
                String likeKeyword = "%" + keyword.trim().toLowerCase() + "%";
                predicates.add(cb.or(
                        cb.like(cb.lower(root.get("title")), likeKeyword),
                        cb.like(cb.lower(root.get("slug")), likeKeyword)
                ));
            }

            if (position != null && !position.isBlank()) {
                predicates.add(cb.equal(root.get("position"), position.trim()));
            }

            if (status != null && !status.isBlank()) {
                predicates.add(cb.equal(root.get("status"), status.trim()));
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };

        Page<Banner> bannerPage = bannerRepository.findAll(spec, pageable);
        Page<BannerResponse> responsePage = bannerPage.map(this::toBannerResponse);

        return ApiResponse.success("Lấy danh sách banner thành công", PageResponse.from(responsePage));
    }

    @Override
    public ApiResponse<List<BannerResponse>> getClientBannersByPosition(String position) {
        List<Banner> banners;
        if (position != null && !position.isBlank()) {
            banners = bannerRepository.findAllByPositionAndStatusAndDeletedAtIsNullOrderBySortOrderAsc(position.trim(), "ACTIVE");
        } else {
            banners = bannerRepository.findAllByStatusAndDeletedAtIsNullOrderBySortOrderAsc("ACTIVE");
        }

        List<BannerResponse> list = banners.stream().map(this::toBannerResponse).collect(Collectors.toList());
        return ApiResponse.success("Lấy danh sách banner thành công", list);
    }

    @Override
    public ApiResponse<BannerResponse> getBannerById(Long id) {
        Banner banner = bannerRepository.findByIdAndDeletedAtIsNull(id)
                .orElseThrow(() -> new AppException(HttpStatus.NOT_FOUND, "Không tìm thấy banner với ID: " + id));
        return ApiResponse.success("Lấy thông tin banner thành công", toBannerResponse(banner));
    }

    @Override
    @Transactional
    public ApiResponse<BannerResponse> createBanner(BannerRequest request, MultipartFile imageFile) {
        String slug = (request.getSlug() != null && !request.getSlug().isBlank())
                ? toSlug(request.getSlug())
                : toSlug(request.getTitle());

        if (bannerRepository.existsBySlugAndDeletedAtIsNull(slug)) {
            throw new AppException(HttpStatus.CONFLICT, "Slug banner '" + slug + "' đã tồn tại");
        }

        String uploadedImageUrl = request.getImageUrl();
        if (imageFile != null && !imageFile.isEmpty()) {
            uploadedImageUrl = uploadFileToCloudinary(imageFile);
        }

        if (uploadedImageUrl == null || uploadedImageUrl.isBlank()) {
            throw new AppException(HttpStatus.BAD_REQUEST, "Vui lòng tải lên ảnh tượng trưng cho banner");
        }

        Banner banner = Banner.builder()
                .title(request.getTitle().trim())
                .slug(slug)
                .description(request.getDescription())
                .imageUrl(uploadedImageUrl)
                .targetUrl(request.getTargetUrl())
                .position(request.getPosition() != null ? request.getPosition() : "HOME_TOP")
                .status(request.getStatus() != null ? request.getStatus() : "ACTIVE")
                .sortOrder(request.getSortOrder() != null ? request.getSortOrder() : 0)
                .build();

        bannerRepository.save(banner);
        return ApiResponse.success("Tạo banner thành công", toBannerResponse(banner));
    }

    @Override
    @Transactional
    public ApiResponse<BannerResponse> updateBanner(Long id, BannerRequest request, MultipartFile imageFile) {
        Banner banner = bannerRepository.findByIdAndDeletedAtIsNull(id)
                .orElseThrow(() -> new AppException(HttpStatus.NOT_FOUND, "Không tìm thấy banner với ID: " + id));

        String slug = (request.getSlug() != null && !request.getSlug().isBlank())
                ? toSlug(request.getSlug())
                : toSlug(request.getTitle());

        if (bannerRepository.existsBySlugAndIdNotAndDeletedAtIsNull(slug, id)) {
            throw new AppException(HttpStatus.CONFLICT, "Slug banner '" + slug + "' đã tồn tại");
        }

        if (imageFile != null && !imageFile.isEmpty()) {
            String newImageUrl = uploadFileToCloudinary(imageFile);
            banner.setImageUrl(newImageUrl);
        } else if (request.getImageUrl() != null) {
            banner.setImageUrl(request.getImageUrl());
        }

        banner.setTitle(request.getTitle().trim());
        banner.setSlug(slug);
        banner.setDescription(request.getDescription());
        banner.setTargetUrl(request.getTargetUrl());
        if (request.getPosition() != null) banner.setPosition(request.getPosition());
        if (request.getStatus() != null) banner.setStatus(request.getStatus());
        if (request.getSortOrder() != null) banner.setSortOrder(request.getSortOrder());

        bannerRepository.save(banner);
        return ApiResponse.success("Cập nhật banner thành công", toBannerResponse(banner));
    }

    @Override
    @Transactional
    public ApiResponse<Void> deleteBanner(Long id) {
        Banner banner = bannerRepository.findByIdAndDeletedAtIsNull(id)
                .orElseThrow(() -> new AppException(HttpStatus.NOT_FOUND, "Không tìm thấy banner với ID: " + id));

        banner.setDeletedAt(LocalDateTime.now());
        banner.setStatus("INACTIVE");
        bannerRepository.save(banner);

        return ApiResponse.success("Xóa banner thành công");
    }

    // ================= Private helpers =================

    private BannerResponse toBannerResponse(Banner b) {
        return BannerResponse.builder()
                .id(b.getId())
                .title(b.getTitle())
                .slug(b.getSlug())
                .description(b.getDescription())
                .imageUrl(b.getImageUrl())
                .targetUrl(b.getTargetUrl())
                .position(b.getPosition())
                .status(b.getStatus())
                .sortOrder(b.getSortOrder())
                .createdAt(b.getCreatedAt())
                .updatedAt(b.getUpdatedAt())
                .build();
    }

    private String uploadFileToCloudinary(MultipartFile file) {
        try {
            Map uploadResult = cloudinaryService.uploadImage(file, CLOUDINARY_BANNER_FOLDER);
            return (String) uploadResult.get("secure_url");
        } catch (IOException e) {
            throw new AppException(HttpStatus.INTERNAL_SERVER_ERROR, "Lỗi khi tải ảnh banner lên Cloudinary: " + e.getMessage());
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
