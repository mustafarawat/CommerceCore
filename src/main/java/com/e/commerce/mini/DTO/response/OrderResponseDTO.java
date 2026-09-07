package com.e.commerce.mini.DTO.response;

import com.e.commerce.mini.Enums.OrderStatus;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
public class OrderResponseDTO {

    private Long id;

    private String orderNumber;

    private OrderStatus status;

    private BigDecimal totalAmount;

    private String customerName;

    private String customerEmail;

    private String customerMobile;

    private String paymentMethod;

    private String paymentStatus;

    private BigDecimal paymentAmount;

    private String razorpayOrderId;

    private String razorpayPaymentId;

    private LocalDateTime paidAt;

    private String shippingFullName;

    private String shippingContactNo;

    private String shippingAddressLine;

    private String shippingCity;

    private String shippingState;

    private String shippingPincode;

    private String shippingCountry;

    private List<OrderItemResponseDTO> orderItems;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;
}