package com.clothingstore.backend.service.impl;

import com.clothingstore.backend.dto.request.CheckoutRequest;
import com.clothingstore.backend.dto.response.ApiResponse;
import com.clothingstore.backend.dto.response.OrderItemResponse;
import com.clothingstore.backend.dto.response.OrderResponse;
import com.clothingstore.backend.entity.*;
import com.clothingstore.backend.exception.AppException;
import com.clothingstore.backend.repository.*;
import com.clothingstore.backend.service.OrderService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class OrderServiceImpl implements OrderService {

    private final UserRepository userRepository;
    private final CartRepository cartRepository;
    private final CartItemRepository cartItemRepository;
    private final OrderRepository orderRepository;
    private final OrderItemRepository orderItemRepository;
    private final ProductRepository productRepository;
    private final PaymentRepository paymentRepository;

    @Override
    @Transactional
    public ApiResponse<OrderResponse> checkout(String userEmail, CheckoutRequest request) {
        User user = getUserByEmail(userEmail);
        Cart cart = cartRepository.findByUserId(user.getId())
                .orElseThrow(() -> new AppException(HttpStatus.BAD_REQUEST, "Giỏ hàng của bạn đang rỗng"));

        if (cart.getItems() == null || cart.getItems().isEmpty()) {
            throw new AppException(HttpStatus.BAD_REQUEST, "Giỏ hàng của bạn đang rỗng, không thể lên đơn hàng");
        }

        // 1. Kiểm tra tồn kho cho từng món đồ trong giỏ
        for (CartItem item : cart.getItems()) {
            Product p = item.getProduct();
            if ("INACTIVE".equalsIgnoreCase(p.getStatus())) {
                throw new AppException(HttpStatus.BAD_REQUEST, "Sản phẩm '" + p.getName() + "' hiện ngưng kinh doanh");
            }
            if (p.getStockQuantity() < item.getQuantity()) {
                throw new AppException(HttpStatus.BAD_REQUEST, "Sản phẩm '" + p.getName() + "' chỉ còn " + p.getStockQuantity() + " trong kho");
            }
        }

        // 2. Tính tổng tiền
        BigDecimal subtotal = BigDecimal.ZERO;
        for (CartItem item : cart.getItems()) {
            BigDecimal price = item.getPrice() != null ? item.getPrice() : (item.getProduct().getSalePrice() != null ? item.getProduct().getSalePrice() : item.getProduct().getPrice());
            subtotal = subtotal.add(price.multiply(BigDecimal.valueOf(item.getQuantity())));
        }
        BigDecimal shippingFee = BigDecimal.ZERO; // Có thể mở rộng phí ship
        BigDecimal totalAmount = subtotal.add(shippingFee);

        // 3. Sinh Mã Đơn Hàng Độc Nhất (ORD-YYYYMMDD-XXXXXX)
        String dateStr = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        String randomSuffix = String.format("%06d", new Random().nextInt(1000000));
        String orderCode = "ORD-" + dateStr + "-" + randomSuffix;

        // 4. Tạo đối tượng Order
        Order order = Order.builder()
                .orderCode(orderCode)
                .user(user)
                .customerName(request.getCustomerName().trim())
                .phone(request.getPhone().trim())
                .email((request.getEmail() != null && !request.getEmail().isBlank()) ? request.getEmail().trim() : user.getEmail())
                .province(request.getProvince().trim())
                .district((request.getDistrict() != null && !request.getDistrict().isBlank()) ? request.getDistrict().trim() : "")
                .ward(request.getWard().trim())
                .streetAddress(request.getStreetAddress().trim())
                .note((request.getNote() != null) ? request.getNote().trim() : null)
                .subtotal(subtotal)
                .shippingFee(shippingFee)
                .totalAmount(totalAmount)
                .paymentMethod("COD".equalsIgnoreCase(request.getPaymentMethod()) ? "COD" : request.getPaymentMethod().trim())
                .paymentStatus("UNPAID")
                .status("PENDING")
                .items(new ArrayList<>())
                .payments(new ArrayList<>())
                .build();

        orderRepository.save(order);

        // 5. Tạo OrderItems và Trừ Tồn Kho sản phẩm
        List<OrderItem> orderItems = new ArrayList<>();
        for (CartItem cartItem : cart.getItems()) {
            Product p = cartItem.getProduct();
            BigDecimal itemPrice = cartItem.getPrice() != null ? cartItem.getPrice() : (p.getSalePrice() != null ? p.getSalePrice() : p.getPrice());
            BigDecimal itemTotal = itemPrice.multiply(BigDecimal.valueOf(cartItem.getQuantity()));

            OrderItem orderItem = OrderItem.builder()
                    .order(order)
                    .product(p)
                    .productName(p.getName())
                    .productThumbnail(p.getThumbnailUrl())
                    .quantity(cartItem.getQuantity())
                    .price(itemPrice)
                    .size(cartItem.getSize())
                    .color(cartItem.getColor())
                    .totalPrice(itemTotal)
                    .build();

            orderItems.add(orderItem);

            // Trừ số lượng kho hàng
            p.setStockQuantity(p.getStockQuantity() - cartItem.getQuantity());
            productRepository.save(p);
        }

        orderItemRepository.saveAll(orderItems);
        order.setItems(orderItems);

        // 6. Tạo bản ghi Giao dịch Thanh toán ban đầu (Payments)
        Payment payment = Payment.builder()
                .order(order)
                .transactionCode("TXN-" + orderCode)
                .paymentGateway(order.getPaymentMethod())
                .paymentMethod(order.getPaymentMethod())
                .amount(totalAmount)
                .currency("VND")
                .status("PENDING")
                .paymentInfo("Thanh toán khi nhận hàng (COD) cho đơn " + orderCode)
                .build();

        paymentRepository.save(payment);
        order.getPayments().add(payment);

        // 7. Xóa sạch giỏ hàng của người dùng
        cartItemRepository.deleteAll(cart.getItems());
        cart.getItems().clear();
        cartRepository.save(cart);

        return ApiResponse.success("Đặt hàng thành công! Đơn hàng của bạn đã được gửi tới hệ thống.", toOrderResponse(order));
    }

    @Override
    @Transactional(readOnly = true)
    public ApiResponse<List<OrderResponse>> getUserOrders(String userEmail) {
        User user = getUserByEmail(userEmail);
        List<Order> orders = orderRepository.findByUserIdOrderByCreatedAtDesc(user.getId());
        List<OrderResponse> responses = orders.stream().map(this::toOrderResponse).collect(Collectors.toList());
        return ApiResponse.success("Lấy danh sách đơn hàng thành công", responses);
    }

    @Override
    @Transactional(readOnly = true)
    public ApiResponse<OrderResponse> getUserOrderDetail(String userEmail, Long orderId) {
        User user = getUserByEmail(userEmail);
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new AppException(HttpStatus.NOT_FOUND, "Không tìm thấy đơn hàng với ID: " + orderId));

        if (!order.getUser().getId().equals(user.getId())) {
            throw new AppException(HttpStatus.FORBIDDEN, "Bạn không có quyền xem đơn hàng của người khác");
        }

        return ApiResponse.success("Lấy chi tiết đơn hàng thành công", toOrderResponse(order));
    }

    @Override
    @Transactional
    public ApiResponse<OrderResponse> cancelUserOrder(String userEmail, Long orderId) {
        User user = getUserByEmail(userEmail);
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new AppException(HttpStatus.NOT_FOUND, "Không tìm thấy đơn hàng với ID: " + orderId));

        if (!order.getUser().getId().equals(user.getId())) {
            throw new AppException(HttpStatus.FORBIDDEN, "Bạn không có quyền hủy đơn hàng của người khác");
        }

        if (!"PENDING".equalsIgnoreCase(order.getStatus())) {
            throw new AppException(HttpStatus.BAD_REQUEST, "Đơn hàng chỉ có thể hủy khi ở trạng thái 'Chờ xác nhận'");
        }

        order.setStatus("CANCELLED");

        // Hoàn trả số lượng tồn kho sản phẩm
        for (OrderItem item : order.getItems()) {
            Product p = item.getProduct();
            if (p != null) {
                p.setStockQuantity(p.getStockQuantity() + item.getQuantity());
                productRepository.save(p);
            }
        }

        orderRepository.save(order);
        return ApiResponse.success("Đã hủy đơn hàng thành công", toOrderResponse(order));
    }

    private User getUserByEmail(String email) {
        return userRepository.findByEmailAndDeletedAtIsNull(email)
                .orElseThrow(() -> new AppException(HttpStatus.UNAUTHORIZED, "Vui lòng đăng nhập trước khi thao tác đơn hàng"));
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
