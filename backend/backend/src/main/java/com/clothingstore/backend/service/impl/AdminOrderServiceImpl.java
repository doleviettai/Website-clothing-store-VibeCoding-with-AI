package com.clothingstore.backend.service.impl;

import com.clothingstore.backend.dto.request.OrderStatusUpdateRequest;
import com.clothingstore.backend.dto.response.ApiResponse;
import com.clothingstore.backend.dto.response.OrderItemResponse;
import com.clothingstore.backend.dto.response.OrderResponse;
import com.clothingstore.backend.dto.response.PageResponse;
import com.clothingstore.backend.entity.Order;
import com.clothingstore.backend.entity.OrderItem;
import com.clothingstore.backend.exception.AppException;
import com.clothingstore.backend.repository.OrderRepository;
import com.clothingstore.backend.service.AdminOrderService;
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
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AdminOrderServiceImpl implements AdminOrderService {

    private final OrderRepository orderRepository;

    @Override
    @Transactional(readOnly = true)
    public ApiResponse<PageResponse<OrderResponse>> getOrders(
            String keyword, String status, String paymentStatus, int page, int size
    ) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("id").descending());

        Specification<Order> spec = (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (keyword != null && !keyword.isBlank()) {
                String kw = "%" + keyword.trim().toLowerCase() + "%";
                predicates.add(cb.or(
                        cb.like(cb.lower(root.get("orderCode")), kw),
                        cb.like(cb.lower(root.get("customerName")), kw),
                        cb.like(cb.lower(root.get("phone")), kw),
                        cb.like(cb.lower(root.get("email")), kw)
                ));
            }

            if (status != null && !status.isBlank()) {
                predicates.add(cb.equal(root.get("status"), status.trim()));
            }

            if (paymentStatus != null && !paymentStatus.isBlank()) {
                predicates.add(cb.equal(root.get("paymentStatus"), paymentStatus.trim()));
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };

        Page<Order> orderPage = orderRepository.findAll(spec, pageable);
        Page<OrderResponse> responsePage = orderPage.map(this::toOrderResponse);

        return ApiResponse.success("Lấy danh sách đơn hàng thành công", PageResponse.from(responsePage));
    }

    @Override
    @Transactional(readOnly = true)
    public ApiResponse<OrderResponse> getOrderDetail(Long orderId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new AppException(HttpStatus.NOT_FOUND, "Không tìm thấy đơn hàng với ID: " + orderId));
        return ApiResponse.success("Lấy chi tiết đơn hàng thành công", toOrderResponse(order));
    }

    @Override
    @Transactional
    public ApiResponse<OrderResponse> updateOrderStatus(Long orderId, OrderStatusUpdateRequest request) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new AppException(HttpStatus.NOT_FOUND, "Không tìm thấy đơn hàng với ID: " + orderId));

        String newStatus = request.getStatus().trim().toUpperCase();
        order.setStatus(newStatus);

        // Nếu chuyển sang DELIVERED và phương thức COD -> Tự động chuyển paymentStatus = PAID
        if ("DELIVERED".equals(newStatus) && "COD".equalsIgnoreCase(order.getPaymentMethod())) {
            order.setPaymentStatus("PAID");
        }

        orderRepository.save(order);
        return ApiResponse.success("Đã cập nhật trạng thái đơn hàng thành " + newStatus, toOrderResponse(order));
    }

    @Override
    @Transactional
    public ApiResponse<OrderResponse> updatePaymentStatus(Long orderId, String paymentStatus) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new AppException(HttpStatus.NOT_FOUND, "Không tìm thấy đơn hàng với ID: " + orderId));

        order.setPaymentStatus(paymentStatus.trim().toUpperCase());
        orderRepository.save(order);

        return ApiResponse.success("Đã cập nhật trạng thái thanh toán", toOrderResponse(order));
    }

    private OrderResponse toOrderResponse(Order order) {
        List<OrderItemResponse> itemResponses = order.getItems().stream().map(item -> OrderItemResponse.builder()
                .id(item.getId())
                .productId(item.getProduct() != null ? item.getProduct().getId() : null)
                .productName(item.getProductName())
                .productThumbnail(item.getProductThumbnail())
                .quantity(item.getQuantity())
                .price(item.getPrice())
                .size(item.getSize())
                .color(item.getColor())
                .totalPrice(item.getTotalPrice())
                .build()
        ).collect(Collectors.toList());

        String fullAddress = String.format("%s, %s, %s%s",
                order.getStreetAddress(),
                order.getWard(),
                order.getProvince(),
                (order.getDistrict() != null && !order.getDistrict().isBlank()) ? (", " + order.getDistrict()) : ""
        );

        return OrderResponse.builder()
                .id(order.getId())
                .orderCode(order.getOrderCode())
                .userId(order.getUser() != null ? order.getUser().getId() : null)
                .userEmail(order.getUser() != null ? order.getUser().getEmail() : null)
                .customerName(order.getCustomerName())
                .phone(order.getPhone())
                .email(order.getEmail())
                .province(order.getProvince())
                .district(order.getDistrict())
                .ward(order.getWard())
                .streetAddress(order.getStreetAddress())
                .fullAddress(fullAddress)
                .note(order.getNote())
                .subtotal(order.getSubtotal())
                .shippingFee(order.getShippingFee())
                .totalAmount(order.getTotalAmount())
                .paymentMethod(order.getPaymentMethod())
                .paymentStatus(order.getPaymentStatus())
                .status(order.getStatus())
                .items(itemResponses)
                .createdAt(order.getCreatedAt())
                .updatedAt(order.getUpdatedAt())
                .build();
    }
}
