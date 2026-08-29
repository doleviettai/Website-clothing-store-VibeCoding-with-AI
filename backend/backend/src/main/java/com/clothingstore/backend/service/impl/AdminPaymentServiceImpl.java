package com.clothingstore.backend.service.impl;

import com.clothingstore.backend.dto.response.ApiResponse;
import com.clothingstore.backend.dto.response.PageResponse;
import com.clothingstore.backend.dto.response.PaymentResponse;
import com.clothingstore.backend.entity.Order;
import com.clothingstore.backend.entity.Payment;
import com.clothingstore.backend.exception.AppException;
import com.clothingstore.backend.repository.PaymentRepository;
import com.clothingstore.backend.service.AdminPaymentService;
import jakarta.persistence.criteria.Join;
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

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class AdminPaymentServiceImpl implements AdminPaymentService {

    private final PaymentRepository paymentRepository;

    @Override
    @Transactional(readOnly = true)
    public ApiResponse<PageResponse<PaymentResponse>> getPayments(
            String keyword, String paymentGateway, String status, int page, int size
    ) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("id").descending());

        Specification<Payment> spec = (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (keyword != null && !keyword.isBlank()) {
                String kw = "%" + keyword.trim().toLowerCase() + "%";
                Join<Payment, Order> orderJoin = root.join("order");

                predicates.add(cb.or(
                        cb.like(cb.lower(root.get("transactionCode")), kw),
                        cb.like(cb.lower(root.get("gatewayTransactionNo")), kw),
                        cb.like(cb.lower(orderJoin.get("orderCode")), kw),
                        cb.like(cb.lower(orderJoin.get("customerName")), kw)
                ));
            }

            if (paymentGateway != null && !paymentGateway.isBlank()) {
                predicates.add(cb.equal(root.get("paymentGateway"), paymentGateway.trim()));
            }

            if (status != null && !status.isBlank()) {
                predicates.add(cb.equal(root.get("status"), status.trim()));
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };

        Page<Payment> paymentPage = paymentRepository.findAll(spec, pageable);
        Page<PaymentResponse> responsePage = paymentPage.map(this::toPaymentResponse);

        return ApiResponse.success("Lấy danh sách lịch sử giao dịch thành công", PageResponse.from(responsePage));
    }

    @Override
    @Transactional(readOnly = true)
    public ApiResponse<PaymentResponse> getPaymentDetail(Long paymentId) {
        Payment payment = paymentRepository.findById(paymentId)
                .orElseThrow(() -> new AppException(HttpStatus.NOT_FOUND, "Không tìm thấy giao dịch với ID: " + paymentId));
        return ApiResponse.success("Lấy chi tiết giao dịch thành công", toPaymentResponse(payment));
    }

    private PaymentResponse toPaymentResponse(Payment p) {
        Order order = p.getOrder();
        return PaymentResponse.builder()
                .id(p.getId())
                .orderId(order != null ? order.getId() : null)
                .orderCode(order != null ? order.getOrderCode() : null)
                .customerName(order != null ? order.getCustomerName() : null)
                .transactionCode(p.getTransactionCode())
                .gatewayTransactionNo(p.getGatewayTransactionNo())
                .paymentGateway(p.getPaymentGateway())
                .paymentMethod(p.getPaymentMethod())
                .amount(p.getAmount())
                .currency(p.getCurrency())
                .status(p.getStatus())
                .paymentInfo(p.getPaymentInfo())
                .paidAt(p.getPaidAt())
                .createdAt(p.getCreatedAt())
                .build();
    }
}
