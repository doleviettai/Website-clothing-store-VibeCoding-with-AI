package com.clothingstore.backend.service;

import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Map;

/**
 * Service upload & xóa hình ảnh trên Cloudinary.
 */
public interface CloudinaryService {

    /**
     * Upload file ảnh lên Cloudinary vào thư mục đã cấu hình (FashionShop2).
     *
     * @param file File ảnh tải lên từ request
     * @return Map kết quả trả về từ Cloudinary (bao gồm secure_url, public_id...)
     */
    Map uploadImage(MultipartFile file) throws IOException;

    /**
     * Upload file ảnh lên Cloudinary vào thư mục tùy chỉnh.
     *
     * @param file File ảnh
     * @param folderName Tên thư mục (VD: "FashionShop2/products", "FashionShop2/avatars")
     */
    Map uploadImage(MultipartFile file, String folderName) throws IOException;

    /**
     * Xóa ảnh khỏi Cloudinary bằng publicId.
     */
    Map deleteImage(String publicId) throws IOException;
}
