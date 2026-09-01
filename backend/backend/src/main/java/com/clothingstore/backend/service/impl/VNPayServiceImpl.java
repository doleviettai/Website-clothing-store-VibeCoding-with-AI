package com.clothingstore.backend.service.impl;

import com.clothingstore.backend.config.VNPayConfig;
import com.clothingstore.backend.dto.request.VNPayCreateRequest;
import com.clothingstore.backend.dto.response.ApiResponse;
import com.clothingstore.backend.dto.response.VNPayCreateResponse;
import com.clothingstore.backend.entity.Order;
import com.clothingstore.backend.entity.Payment;
import com.clothingstore.backend.entity.User;
import com.clothingstore.backend.exception.AppException;
import com.clothingstore.backend.repository.OrderRepository;
import com.clothingstore.backend.repository.PaymentRepository;
import com.clothingstore.backend.repository.UserRepository;
import com.clothingstore.backend.service.VNPayService;
import com.clothingstore.backend.util.VNPayUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.util.*;

@Service
@RequiredArgsConstructor
public class VNPayServiceImpl implements VNPayService {

    private final VNPayConfig vnPayConfig;
    private final OrderRepository orderRepository;
    private final PaymentRepository paymentRepository;
    private final UserRepository userRepository;

    @Override
    @Transactional
    public ApiResponse<VNPayCreateResponse> createPayment(String userEmail, VNPayCreateRequest request) {
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

        long amount = order.getTotalAmount().longValue() * 100;
        String vnpTxnRef = String.format("VNP_%d_%d", order.getId(), System.currentTimeMillis() % 100000);
        String bankCode = (request.getBankCode() != null && !request.getBankCode().isBlank()) ? request.getBankCode().trim() : "";

        Calendar csn = Calendar.getInstance(TimeZone.getTimeZone("Etc/GMT+7"));
        SimpleDateFormat formatter = new SimpleDateFormat("yyyyMMddHHmmss");
        String vnpCreateDate = formatter.format(csn.getTime());

        Map<String, String> vnpParams = new HashMap<>();
        vnpParams.put("vnp_Version", "2.1.0");
        vnpParams.put("vnp_Command", "pay");
        vnpParams.put("vnp_TmnCode", vnPayConfig.getTmnCode());
        vnpParams.put("vnp_Amount", String.valueOf(amount));
        vnpParams.put("vnp_CurrCode", "VND");
        vnpParams.put("vnp_TxnRef", vnpTxnRef);
        vnpParams.put("vnp_OrderInfo", "Thanh toan don hang #" + order.getOrderCode());
        vnpParams.put("vnp_OrderType", "other");
        vnpParams.put("vnp_Locale", "vn");
        vnpParams.put("vnp_ReturnUrl", vnPayConfig.getReturnUrl());
        vnpParams.put("vnp_IpAddr", "127.0.0.1");
        vnpParams.put("vnp_CreateDate", vnpCreateDate);

        if (!bankCode.isEmpty()) {
            vnpParams.put("vnp_BankCode", bankCode);
        }

        // Tạo Secure Hash
        String vnpSecureHash = VNPayUtil.hashAllFields(vnpParams, vnPayConfig.getHashSecret());

        // Đường dẫn Cổng VNPAY Sandbox Demo (cho phép chọn ngân hàng + quét QR + nhập thẻ test)
        String paymentUrl = String.format("%s/vnpay-pay?order_id=%d&vnp_TxnRef=%s&amount=%d&bank_code=%s&hash=%s",
                "http://localhost:5173",
                order.getId(),
                vnpTxnRef,
                order.getTotalAmount().longValue(),
                bankCode,
                vnpSecureHash
        );

        // Lưu Payment entity
        Optional<Payment> existingPaymentOpt = paymentRepository.findByOrderIdOrderByCreatedAtDesc(order.getId())
                .stream().filter(p -> "VNPAY".equalsIgnoreCase(p.getPaymentGateway())).findFirst();

        Payment payment;
        if (existingPaymentOpt.isPresent()) {
            payment = existingPaymentOpt.get();
            payment.setTransactionCode(vnpTxnRef);
            payment.setStatus("PENDING");
            payment.setAmount(order.getTotalAmount());
        } else {
            payment = Payment.builder()
                    .order(order)
                    .transactionCode(vnpTxnRef)
                    .paymentGateway("VNPAY")
                    .paymentMethod("VNPAY")
                    .amount(order.getTotalAmount())
                    .currency("VND")
                    .status("PENDING")
                    .paymentInfo("Thanh toán cổng VNPAY Sandbox")
                    .build();
        }
        paymentRepository.save(payment);

        order.setPaymentMethod("VNPAY");
        orderRepository.save(order);

        VNPayCreateResponse response = VNPayCreateResponse.builder()
                .success(true)
                .paymentUrl(paymentUrl)
                .txnRef(vnpTxnRef)
                .message("Khởi tạo thanh toán VNPAY thành công")
                .build();

        return ApiResponse.success("Tạo liên kết thanh toán VNPAY thành công", response);
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
            data.put("txnRef", p.getTransactionCode());
            data.put("paymentGateway", p.getPaymentGateway());
            data.put("paymentStatusDetail", p.getStatus());
            data.put("gatewayTransactionNo", p.getGatewayTransactionNo());
        }

        return ApiResponse.success("Lấy trạng thái thanh toán VNPAY thành công", data);
    }

    @Override
    @Transactional
    public ApiResponse<Map<String, Object>> confirmMockPayment(Long orderId, String txnRef, String status) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new AppException(HttpStatus.NOT_FOUND, "Không tìm thấy đơn hàng với ID: " + orderId));

        Payment payment = paymentRepository.findByTransactionCode(txnRef)
                .orElseGet(() -> {
                    Payment newP = Payment.builder()
                            .order(order)
                            .transactionCode(txnRef)
                            .paymentGateway("VNPAY")
                            .paymentMethod("VNPAY")
                            .amount(order.getTotalAmount())
                            .currency("VND")
                            .status("PENDING")
                            .build();
                    return paymentRepository.save(newP);
                });

        if ("SUCCESS".equalsIgnoreCase(status)) {
            payment.setStatus("SUCCESS");
            payment.setGatewayTransactionNo("VNP-MOCK-" + System.currentTimeMillis());
            payment.setPaidAt(LocalDateTime.now());
            paymentRepository.save(payment);

            order.setPaymentStatus("PAID");
            order.setPaymentMethod("VNPAY");
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

        return ApiResponse.success("Đã xử lý thanh toán VNPAY Sandbox ảo thành công", resData);
    }
}
