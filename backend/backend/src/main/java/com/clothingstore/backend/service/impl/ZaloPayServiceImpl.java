package com.clothingstore.backend.service.impl;

import com.clothingstore.backend.config.ZaloPayConfig;
import com.clothingstore.backend.dto.request.ZaloPayCallbackRequest;
import com.clothingstore.backend.dto.request.ZaloPayCreateRequest;
import com.clothingstore.backend.dto.response.ApiResponse;
import com.clothingstore.backend.dto.response.ZaloPayCreateResponse;
import com.clothingstore.backend.entity.Order;
import com.clothingstore.backend.entity.Payment;
import com.clothingstore.backend.entity.User;
import com.clothingstore.backend.exception.AppException;
import com.clothingstore.backend.repository.OrderRepository;
import com.clothingstore.backend.repository.PaymentRepository;
import com.clothingstore.backend.repository.UserRepository;
import com.clothingstore.backend.service.ZaloPayService;
import com.clothingstore.backend.util.ZaloPayMacUtil;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

@Service
@RequiredArgsConstructor
public class ZaloPayServiceImpl implements ZaloPayService {

    private final ZaloPayConfig zaloPayConfig;
    private final OrderRepository orderRepository;
    private final PaymentRepository paymentRepository;
    private final UserRepository userRepository;
    private final ObjectMapper objectMapper;

    @Override
    @Transactional
    public ApiResponse<ZaloPayCreateResponse> createPayment(String userEmail, ZaloPayCreateRequest request) {
        User user = userRepository.findByEmailAndDeletedAtIsNull(userEmail)
                .orElseThrow(() -> new AppException(HttpStatus.UNAUTHORIZED, "Vui lòng đăng nhập trước khi thanh toán"));

        Order order = orderRepository.findById(request.getOrderId())
                .orElseThrow(() -> new AppException(HttpStatus.NOT_FOUND, "Không tìm thấy đơn hàng với ID: " + request.getOrderId()));

        if (!order.getUser().getId().equals(user.getId())) {
            throw new AppException(HttpStatus.FORBIDDEN, "Bạn không có quyền thanh toán đơn hàng của người khác");
        }

        if ("PAID".equalsIgnoreCase(order.getPaymentStatus())) {
            throw new AppException(HttpStatus.BAD_REQUEST, "Đơn hàng này đã được thanh toán thành công trước đó");
        }

        long amount = order.getTotalAmount().longValue();
        String dateStr = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyMMdd"));
        String appTransId = String.format("%s_%d_%d", dateStr, order.getId(), System.currentTimeMillis() % 100000);
        long appTime = System.currentTimeMillis();

        String embedData = String.format("{\"redirecturl\":\"%s\"}", zaloPayConfig.getRedirectUrl());
        String item = "[]";
        String appUser = user.getEmail();

        // 1. Chuỗi dữ liệu ký MAC bằng KEY1: app_id|app_trans_id|app_user|amount|app_time|embed_data|item
        String dataToSign = String.format("%s|%s|%s|%d|%d|%s|%s",
                zaloPayConfig.getAppId(),
                appTransId,
                appUser,
                amount,
                appTime,
                embedData,
                item
        );

        String mac = ZaloPayMacUtil.computeHmacSha256(dataToSign, zaloPayConfig.getKey1());

        // 2. Tạo đường dẫn thanh toán ZaloPay Gateway (Hỗ trợ URL ZaloPay Sandbox & Trang ZaloPay Mock Page cho local)
        String orderUrl = String.format("%s/zalopay-pay?order_id=%d&app_trans_id=%s&amount=%d&mac=%s",
                "http://localhost:5173",
                order.getId(),
                appTransId,
                amount,
                mac
        );

        // 3. Tạo hoặc cập nhật bản ghi lịch sử Giao dịch ZaloPay
        Optional<Payment> existingPaymentOpt = paymentRepository.findByOrderIdOrderByCreatedAtDesc(order.getId())
                .stream().filter(p -> "ZALOPAY".equalsIgnoreCase(p.getPaymentGateway())).findFirst();

        Payment payment;
        if (existingPaymentOpt.isPresent()) {
            payment = existingPaymentOpt.get();
            payment.setTransactionCode(appTransId);
            payment.setStatus("PENDING");
            payment.setAmount(order.getTotalAmount());
        } else {
            payment = Payment.builder()
                    .order(order)
                    .transactionCode(appTransId)
                    .paymentGateway("ZALOPAY")
                    .paymentMethod("ZALOPAY")
                    .amount(order.getTotalAmount())
                    .currency("VND")
                    .status("PENDING")
                    .paymentInfo("Thanh toán ví điện tử ZaloPay Sandbox")
                    .build();
        }
        paymentRepository.save(payment);

        // Cập nhật phương thức đơn hàng sang ZaloPay
        order.setPaymentMethod("ZALOPAY");
        orderRepository.save(order);

        ZaloPayCreateResponse response = ZaloPayCreateResponse.builder()
                .success(true)
                .orderUrl(orderUrl)
                .appTransId(appTransId)
                .message("Tạo đơn hàng ZaloPay thành công")
                .returnCode(1)
                .build();

        return ApiResponse.success("Khởi tạo thanh toán ZaloPay thành công", response);
    }

    @Override
    @Transactional
    public Map<String, Object> handleCallback(ZaloPayCallbackRequest callbackRequest) {
        Map<String, Object> result = new HashMap<>();
        try {
            String dataStr = callbackRequest.getData();
            String reqMac = callbackRequest.getMac();

            // Xác thực MAC với KEY2
            String expectedMac = ZaloPayMacUtil.computeHmacSha256(dataStr, zaloPayConfig.getKey2());
            if (!expectedMac.equals(reqMac)) {
                result.put("return_code", -1);
                result.put("return_message", "mac not equal");
                return result;
            }

            Map<String, Object> dataJson = objectMapper.readValue(dataStr, Map.class);
            String appTransId = (String) dataJson.get("app_trans_id");
            String zpTransId = String.valueOf(dataJson.get("zp_trans_id"));

            Optional<Payment> paymentOpt = paymentRepository.findByTransactionCode(appTransId);
            if (paymentOpt.isPresent()) {
                Payment payment = paymentOpt.get();
                if (!"SUCCESS".equalsIgnoreCase(payment.getStatus())) {
                    payment.setStatus("SUCCESS");
                    payment.setGatewayTransactionNo(zpTransId);
                    payment.setPaidAt(LocalDateTime.now());
                    paymentRepository.save(payment);

                    Order order = payment.getOrder();
                    order.setPaymentStatus("PAID");
                    order.setPaymentMethod("ZALOPAY");
                    orderRepository.save(order);
                }
            }

            result.put("return_code", 1);
            result.put("return_message", "success");
        } catch (Exception e) {
            result.put("return_code", 0);
            result.put("return_message", e.getMessage());
        }
        return result;
    }

    @Override
    @Transactional(readOnly = true)
    public ApiResponse<Map<String, Object>> getPaymentStatusByOrder(Long orderId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new AppException(HttpStatus.NOT_FOUND, "Không tìm thấy đơn hàng với ID: " + orderId));

        Map<String, Object> data = new HashMap<>();
        data.put("orderId", order.getId());
        data.put("orderCode", order.getOrderCode());
        data.put("paymentStatus", order.getPaymentStatus());
        data.put("orderStatus", order.getStatus());
        data.put("totalAmount", order.getTotalAmount());
        data.put("customerName", order.getCustomerName());

        Optional<Payment> paymentOpt = paymentRepository.findByOrderIdOrderByCreatedAtDesc(order.getId()).stream().findFirst();
        if (paymentOpt.isPresent()) {
            Payment p = paymentOpt.get();
            data.put("appTransId", p.getTransactionCode());
            data.put("paymentGateway", p.getPaymentGateway());
            data.put("paymentStatusDetail", p.getStatus());
            data.put("gatewayTransactionNo", p.getGatewayTransactionNo());
        }

        return ApiResponse.success("Lấy trạng thái thanh toán ZaloPay thành công", data);
    }

    @Override
    @Transactional
    public ApiResponse<Map<String, Object>> confirmMockPayment(Long orderId, String appTransId, String status) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new AppException(HttpStatus.NOT_FOUND, "Không tìm thấy đơn hàng với ID: " + orderId));

        Payment payment = paymentRepository.findByTransactionCode(appTransId)
                .orElseGet(() -> {
                    Payment newP = Payment.builder()
                            .order(order)
                            .transactionCode(appTransId)
                            .paymentGateway("ZALOPAY")
                            .paymentMethod("ZALOPAY")
                            .amount(order.getTotalAmount())
                            .currency("VND")
                            .status("PENDING")
                            .build();
                    return paymentRepository.save(newP);
                });

        if ("SUCCESS".equalsIgnoreCase(status)) {
            payment.setStatus("SUCCESS");
            payment.setGatewayTransactionNo("ZP-MOCK-" + System.currentTimeMillis());
            payment.setPaidAt(LocalDateTime.now());
            paymentRepository.save(payment);

            order.setPaymentStatus("PAID");
            order.setPaymentMethod("ZALOPAY");
            orderRepository.save(order);
        } else {
            payment.setStatus("FAILED");
            paymentRepository.save(payment);

            order.setPaymentStatus("UNPAID");
            orderRepository.save(order);
        }

        Map<String, Object> resData = new HashMap<>();
        resData.put("orderId", order.getId());
        resData.put("orderCode", order.getOrderCode());
        resData.put("paymentStatus", order.getPaymentStatus());
        resData.put("status", status);

        return ApiResponse.success("Đã xử lý thanh toán ZaloPay ảo thành công", resData);
    }
}
