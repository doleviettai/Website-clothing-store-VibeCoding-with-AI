package com.clothingstore.backend.util;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;

public class ZaloPayMacUtil {

    public static final String HMAC_SHA256 = "HmacSHA256";

    public static String computeHmacSha256(String data, String key) {
        try {
            Mac hmacSHA256 = Mac.getInstance(HMAC_SHA256);
            SecretKeySpec secretKey = new SecretKeySpec(key.getBytes(StandardCharsets.UTF_8), HMAC_SHA256);
            hmacSHA256.init(secretKey);
            byte[] hash = hmacSHA256.doFinal(data.getBytes(StandardCharsets.UTF_8));
            return bytesToHex(hash);
        } catch (Exception e) {
            throw new RuntimeException("Lỗi tính toán HMAC_SHA256 cho ZaloPay", e);
        }
    }

    private static String bytesToHex(byte[] bytes) {
        StringBuilder sb = new StringBuilder();
        for (byte b : bytes) {
            sb.append(String.format("%02x", b));
        }
        return sb.toString();
    }
}
