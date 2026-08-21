package com.clothingstore.backend.service.impl;

import com.clothingstore.backend.dto.request.CategoryRequest;
import com.clothingstore.backend.dto.response.ApiResponse;
import com.clothingstore.backend.dto.response.CategoryResponse;
import com.clothingstore.backend.dto.response.PageResponse;
import com.clothingstore.backend.entity.Category;
import com.clothingstore.backend.exception.AppException;
import com.clothingstore.backend.repository.CategoryRepository;
import com.clothingstore.backend.service.CategoryService;
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
import java.util.Map;
import java.util.Locale;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * Cài đặt CategoryService.
 */
@Service
@RequiredArgsConstructor
public class CategoryServiceImpl implements CategoryService {

    private final CategoryRepository categoryRepository;
    private final CloudinaryService cloudinaryService;

    private static final String CLOUDINARY_CATEGORY_FOLDER = "FashionShop2/categories";

    @Override
    public ApiResponse<PageResponse<CategoryResponse>> getAdminCategories(
            String keyword, String status, Long parentId, int page, int size
    ) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("sortOrder").ascending().and(Sort.by("id").descending()));

        Specification<Category> spec = (root, query, cb) -> {
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

            if (parentId != null) {
                if (parentId == 0) {
                    predicates.add(cb.isNull(root.get("parent")));
                } else {
                    predicates.add(cb.equal(root.get("parent").get("id"), parentId));
                }
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };

        Page<Category> categoryPage = categoryRepository.findAll(spec, pageable);
        Page<CategoryResponse> responsePage = categoryPage.map(this::toCategoryResponse);

        return ApiResponse.success("Lấy danh sách chuyên mục thành công", PageResponse.from(responsePage));
    }

    @Override
    public ApiResponse<List<CategoryResponse>> getAllCategoriesForDropdown() {
        List<Category> categories = categoryRepository.findAllByDeletedAtIsNullOrderBySortOrderAscNameAsc();
        List<CategoryResponse> list = categories.stream()
                .map(this::toCategoryResponse)
                .collect(Collectors.toList());
        return ApiResponse.success("Lấy danh sách chuyên mục thành công", list);
    }

    @Override
    public ApiResponse<List<CategoryResponse>> getClientActiveCategories() {
        List<Category> categories = categoryRepository.findAllByStatusAndDeletedAtIsNullOrderBySortOrderAscNameAsc("ACTIVE");
        List<CategoryResponse> list = categories.stream()
                .map(this::toCategoryResponse)
                .collect(Collectors.toList());
        return ApiResponse.success("Lấy danh sách chuyên mục thành công", list);
    }

    @Override
    public ApiResponse<CategoryResponse> getCategoryById(Long id) {
        Category category = categoryRepository.findByIdAndDeletedAtIsNull(id)
                .orElseThrow(() -> new AppException(HttpStatus.NOT_FOUND, "Không tìm thấy chuyên mục với ID: " + id));
        return ApiResponse.success("Lấy thông tin chuyên mục thành công", toCategoryResponse(category));
    }

    @Override
    @Transactional
    public ApiResponse<CategoryResponse> createCategory(CategoryRequest request, MultipartFile imageFile) {
        // Sinh slug nếu trống
        String slug = (request.getSlug() != null && !request.getSlug().isBlank())
                ? toSlug(request.getSlug())
                : toSlug(request.getName());

        if (categoryRepository.existsBySlugAndDeletedAtIsNull(slug)) {
            throw new AppException(HttpStatus.CONFLICT, "Slug chuyên mục '" + slug + "' đã tồn tại");
        }

        // Upload ảnh lên Cloudinary nếu có chọn tệp
        String uploadedImageUrl = request.getImageUrl();
        if (imageFile != null && !imageFile.isEmpty()) {
            uploadedImageUrl = uploadFileToCloudinary(imageFile);
        }

        Category parentCategory = null;
        if (request.getParentId() != null && request.getParentId() > 0) {
            parentCategory = categoryRepository.findByIdAndDeletedAtIsNull(request.getParentId())
                    .orElseThrow(() -> new AppException(HttpStatus.BAD_REQUEST, "Chuyên mục cha không tồn tại"));
        }

        Category category = Category.builder()
                .name(request.getName().trim())
                .slug(slug)
                .description(request.getDescription())
                .imageUrl(uploadedImageUrl)
                .status(request.getStatus() != null ? request.getStatus() : "ACTIVE")
                .sortOrder(request.getSortOrder() != null ? request.getSortOrder() : 0)
                .parent(parentCategory)
                .build();

        categoryRepository.save(category);
        return ApiResponse.success("Tạo chuyên mục thành công", toCategoryResponse(category));
    }

    @Override
    @Transactional
    public ApiResponse<CategoryResponse> updateCategory(Long id, CategoryRequest request, MultipartFile imageFile) {
        Category category = categoryRepository.findByIdAndDeletedAtIsNull(id)
                .orElseThrow(() -> new AppException(HttpStatus.NOT_FOUND, "Không tìm thấy chuyên mục với ID: " + id));

        String slug = (request.getSlug() != null && !request.getSlug().isBlank())
                ? toSlug(request.getSlug())
                : toSlug(request.getName());

        if (categoryRepository.existsBySlugAndIdNotAndDeletedAtIsNull(slug, id)) {
            throw new AppException(HttpStatus.CONFLICT, "Slug chuyên mục '" + slug + "' đã tồn tại");
        }

        // Nếu có upload ảnh mới → tải lên Cloudinary
        if (imageFile != null && !imageFile.isEmpty()) {
            String newImageUrl = uploadFileToCloudinary(imageFile);
            category.setImageUrl(newImageUrl);
        } else if (request.getImageUrl() != null) {
            category.setImageUrl(request.getImageUrl());
        }

        // Kiểm tra chuyên mục cha (tránh tự chọn chính nó làm cha)
        if (request.getParentId() != null) {
            if (request.getParentId().equals(id)) {
                throw new AppException(HttpStatus.BAD_REQUEST, "Chuyên mục không thể làm cha của chính nó");
            }
            if (request.getParentId() > 0) {
                Category parentCategory = categoryRepository.findByIdAndDeletedAtIsNull(request.getParentId())
                        .orElseThrow(() -> new AppException(HttpStatus.BAD_REQUEST, "Chuyên mục cha không tồn tại"));
                category.setParent(parentCategory);
            } else {
                category.setParent(null);
            }
        }

        category.setName(request.getName().trim());
        category.setSlug(slug);
        category.setDescription(request.getDescription());
        if (request.getStatus() != null) category.setStatus(request.getStatus());
        if (request.getSortOrder() != null) category.setSortOrder(request.getSortOrder());

        categoryRepository.save(category);
        return ApiResponse.success("Cập nhật chuyên mục thành công", toCategoryResponse(category));
    }

    @Override
    @Transactional
    public ApiResponse<Void> deleteCategory(Long id) {
        Category category = categoryRepository.findByIdAndDeletedAtIsNull(id)
                .orElseThrow(() -> new AppException(HttpStatus.NOT_FOUND, "Không tìm thấy chuyên mục với ID: " + id));

        category.setDeletedAt(LocalDateTime.now());
        category.setStatus("INACTIVE");
        categoryRepository.save(category);

        return ApiResponse.success("Xóa chuyên mục thành công");
    }

    // ================= Private helpers =================

    private CategoryResponse toCategoryResponse(Category cat) {
        return CategoryResponse.builder()
                .id(cat.getId())
                .name(cat.getName())
                .slug(cat.getSlug())
                .description(cat.getDescription())
                .imageUrl(cat.getImageUrl())
                .status(cat.getStatus())
                .sortOrder(cat.getSortOrder())
                .parentId(cat.getParent() != null ? cat.getParent().getId() : null)
                .parentName(cat.getParent() != null ? cat.getParent().getName() : null)
                .createdAt(cat.getCreatedAt())
                .updatedAt(cat.getUpdatedAt())
                .build();
    }

    private String uploadFileToCloudinary(MultipartFile file) {
        try {
            Map uploadResult = cloudinaryService.uploadImage(file, CLOUDINARY_CATEGORY_FOLDER);
            return (String) uploadResult.get("secure_url");
        } catch (IOException e) {
            throw new AppException(HttpStatus.INTERNAL_SERVER_ERROR, "Lỗi khi tải ảnh lên Cloudinary: " + e.getMessage());
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
