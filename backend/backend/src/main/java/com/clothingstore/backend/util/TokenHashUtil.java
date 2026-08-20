package com.clothingstore.backend.util;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * Tiện ích hash token sử dụng SHA-256.
 * <p>
 * Dùng để hash refresh token trước khi lưu database.
 * Bảo mật: nếu database bị lộ, kẻ tấn công không thể dùng hash để refresh token.
 * <p>
 * Luồng:
 * 1. Backend tạo raw token (UUID ngẫu nhiên)
 * 2. Backend hash(raw token) → lưu vào DB
 * 3. Backend trả raw token cho client
 * 4. Client gửi raw token → backend hash lại → tìm trong DB
 */
public class TokenHashUtil {

    private TokenHashUtil() {
        // Utility class — không khởi tạo
    }

    /**
     * Hash một chuỗi bằng SHA-256 và trả kết quả dạng hex string.
     */
    public static String hash(String input) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hashBytes = digest.digest(input.getBytes(StandardCharsets.UTF_8));

            // Chuyển byte array thành hex string
            StringBuilder hexString = new StringBuilder();
            for (byte b : hashBytes) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) {
                    hexString.append('0');
                }
                hexString.append(hex);
            }
            return hexString.toString();

        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("SHA-256 algorithm not available", e);
        }
    }
}
