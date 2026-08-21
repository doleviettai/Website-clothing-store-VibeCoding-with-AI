package com.clothingstore.backend.service.impl;

import com.clothingstore.backend.service.CloudinaryService;
import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Map;

/**
 * Service thực thi việc tải ảnh lên Cloudinary lưu trữ vào folder FashionShop2.
 */
@Service
@RequiredArgsConstructor
public class CloudinaryServiceImpl implements CloudinaryService {

    private final Cloudinary cloudinary;

    @Value("${cloudinary.folder:FashionShop2}")
    private String defaultFolder;

    @Override
    public Map uploadImage(MultipartFile file) throws IOException {
        return uploadImage(file, defaultFolder);
    }

    @Override
    public Map uploadImage(MultipartFile file, String folderName) throws IOException {
        return cloudinary.uploader().upload(
                file.getBytes(),
                ObjectUtils.asMap(
                        "folder", folderName,
                        "resource_type", "auto"
                )
        );
    }

    @Override
    public Map deleteImage(String publicId) throws IOException {
        return cloudinary.uploader().destroy(publicId, ObjectUtils.emptyMap());
    }
}
