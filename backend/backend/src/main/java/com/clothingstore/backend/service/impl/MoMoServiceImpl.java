package com.clothingstore.backend.service.impl;

import com.clothingstore.backend.config.MoMoConfig;
import com.clothingstore.backend.dto.request.MoMoCreateRequest;
import com.clothingstore.backend.dto.request.MoMoIPNRequest;
import com.clothingstore.backend.dto.response.ApiResponse;
import com.clothingstore.backend.dto.response.MoMoCreateResponse;
import com.clothingstore.backend.entity.Order;
import com.clothingstore.backend.entity.Payment;
import com.clothingstore.backend.entity.User;
import com.clothingstore.backend.exception.AppException;
import com.clothingstore.backend.repository.OrderRepository;
import com.clothingstore.backend.repository.PaymentRepository;
import com.clothingstore.backend.repository.UserRepository;
import com.clothingstore.backend.service.MoMoService;
import com.clothingstore.backend.util.MoMoSignatureUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;

@Service
@RequiredArgsConstructor
public class MoMoServiceImpl implements MoMoService {

    private final MoMoConfig moMoConfig;
    private final OrderRepository orderRepository;
    private final PaymentRepository paymentRepository;
    private final UserRepository userRepository;

    @Override
    @Transactional
    public ApiResponse<MoMoCreateResponse> createPayment(String userEmail, MoMoCreateRequest request) {
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
        String requestId = String.format("REQ_%d_%d", order.getId(), System.currentTimeMillis() % 1000000);
        String momoOrderId = String.format("MOMO_%d_%d", order.getId(), System.currentTimeMillis() % 100000);
        String orderInfo = "Thanh toán đơn hàng #" + order.getOrderCode() + " qua ví MoMo";
        String requestType = "captureWallet";
        String extraData = "";

        // 1. Chuỗi dữ liệu rawSignature MoMo v2
        String rawSignature = String.format(
                "accessKey=%s&amount=%d&extraData=%s&ipnUrl=%s&orderId=%s&orderInfo=%s&partnerCode=%s&redirectUrl=%s&requestId=%s&requestType=%s",
                moMoConfig.getAccessKey(),
                amount,
                extraData,
                moMoConfig.getIpnUrl(),
                momoOrderId,
                orderInfo,
                moMoConfig.getPartnerCode(),
                moMoConfig.getRedirectUrl(),
                requestId,
                requestType
        );

        String signature = MoMoSignatureUtil.computeHmacSha256(rawSignature, moMoConfig.getSecretKey());

        // 2. Tạo đường dẫn Cổng Thanh Toán MoMo (Hỗ trợ MoMo Sandbox URL & Trang MoMo Mock Page cho local)
        String payUrl = String.format("%s/momo-pay?order_id=%d&request_id=%s&momo_order_id=%s&amount=%d&signature=%s",
                "http://localhost:5173",
                order.getId(),
                requestId,
                momoOrderId,
                amount,
                signature
        );

        // 3. Lưu lịch sử giao dịch MoMo trong bảng payments
        Optional<Payment> existingPaymentOpt = paymentRepository.findByOrderIdOrderByCreatedAtDesc(order.getId())
                .stream().filter(p -> "MOMO".equalsIgnoreCase(p.getPaymentGateway())).findFirst();

        Payment payment;
        if (existingPaymentOpt.isPresent()) {
            payment = existingPaymentOpt.get();
            payment.setTransactionCode(requestId);
            payment.setStatus("PENDING");
            payment.setAmount(order.getTotalAmount());
        } else {
            payment = Payment.builder()
                    .order(order)
                    .transactionCode(requestId)
                    .paymentGateway("MOMO")
                    .paymentMethod("MOMO")
                    .amount(order.getTotalAmount())
                    .currency("VND")
                    .status("PENDING")
                    .paymentInfo("Thanh toán ví điện tử MoMo Sandbox")
                    .build();
        }
        paymentRepository.save(payment);

        order.setPaymentMethod("MOMO");
        orderRepository.save(order);

        MoMoCreateResponse response = MoMoCreateResponse.builder()
                .success(true)
                .payUrl(payUrl)
                .requestId(requestId)
                .orderId(momoOrderId)
                .message("Tạo đơn hàng MoMo thành công")
                .resultCode(0)
                .build();

        return ApiResponse.success("Khởi tạo thanh toán MoMo Sandbox thành công", response);
    }

    @Override
    @Transactional
    public Map<String, Object> handleIPN(MoMoIPNRequest ipnRequest) {
        Map<String, Object> result = new HashMap<>();
        try {
            // Verify signature khi nhận IPN Callback từ MoMo
            String rawSignature = String.format(
                    "accessKey=%s&amount=%d&extraData=%s&message=%s&orderId=%s&orderInfo=%s&orderType=%s&partnerCode=%s&payType=%s&requestId=%s&responseTime=%d&resultCode=%d&transId=%s",
                    moMoConfig.getAccessKey(),
                    ipnRequest.getAmount(),
                    ipnRequest.getExtraData() != null ? ipnRequest.getExtraData() : "",
                    ipnRequest.getMessage(),
                    ipnRequest.getOrderId(),
                    ipnRequest.getOrderInfo(),
                    ipnRequest.getOrderType() != null ? ipnRequest.getOrderType() : "",
                    ipnRequest.getPartnerCode(),
                    ipnRequest.getPayType() != null ? ipnRequest.getPayType() : "",
                    ipnRequest.getRequestId(),
                    ipnRequest.getResponseTime(),
                    ipnRequest.getResultCode(),
                    ipnRequest.getTransId()
            );

            String expectedSignature = MoMoSignatureUtil.computeHmacSha256(rawSignature, moMoConfig.getSecretKey());
            if (ipnRequest.getSignature() != null && !expectedSignature.equals(ipnRequest.getSignature())) {
                result.put("resultCode", 97);
                result.put("message", "Invalid signature");
                return result;
            }

            Optional<Payment> paymentOpt = paymentRepository.findByTransactionCode(ipnRequest.getRequestId());
            if (paymentOpt.isPresent()) {
                Payment payment = paymentOpt.get();
                if (ipnRequest.getResultCode() != null && ipnRequest.getResultCode() == 0) {
                    if (!"SUCCESS".equalsIgnoreCase(payment.getStatus())) {
                        payment.setStatus("SUCCESS");
                        payment.setGatewayTransactionNo(ipnRequest.getTransId());
                        payment.setPaidAt(LocalDateTime.now());
                        paymentRepository.save(payment);

                        Order order = payment.getOrder();
                        order.setPaymentStatus("PAID");
                        order.setPaymentMethod("MOMO");
                        orderRepository.save(order);
                    }
                } else {
                    payment.setStatus("FAILED");
                    paymentRepository.save(payment);
                }
            }

            result.put("resultCode", 0);
            result.put("message", "Success");
        } catch (Exception e) {
            result.put("resultCode", 99);
            result.put("message", e.getMessage());
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
            data.put("requestId", p.getTransactionCode());
            data.put("paymentGateway", p.getPaymentGateway());
            data.put("paymentStatusDetail", p.getStatus());
            data.put("gatewayTransactionNo", p.getGatewayTransactionNo());
        }

        return ApiResponse.success("Lấy trạng thái thanh toán MoMo thành công", data);
    }

    @Override
    @Transactional
    public ApiResponse<Map<String, Object>> confirmMockPayment(Long orderId, String requestId, String status) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new AppException(HttpStatus.NOT_FOUND, "Không tìm thấy đơn hàng với ID: " + orderId));

        Payment payment = paymentRepository.findByTransactionCode(requestId)
                .orElseGet(() -> {
                    Payment newP = Payment.builder()
                            .order(order)
                            .transactionCode(requestId)
                            .paymentGateway("MOMO")
                            .paymentMethod("MOMO")
                            .amount(order.getTotalAmount())
                            .currency("VND")
                            .status("PENDING")
                            .build();
                    return paymentRepository.save(newP);
                });

        if ("SUCCESS".equalsIgnoreCase(status)) {
            payment.setStatus("SUCCESS");
            payment.setGatewayTransactionNo("MM-MOCK-" + System.currentTimeMillis());
            payment.setPaidAt(LocalDateTime.now());
            paymentRepository.save(payment);

            order.setPaymentStatus("PAID");
            order.setPaymentMethod("MOMO");
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

        return ApiResponse.success("Đã xử lý thanh toán MoMo Sandbox ảo thành công", resData);
    }
}
