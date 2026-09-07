package com.e.commerce.mini.Mapper;

import com.e.commerce.mini.DTO.response.OrderItemResponseDTO;
import com.e.commerce.mini.DTO.response.OrderResponseDTO;
import com.e.commerce.mini.models.Order;
import com.e.commerce.mini.models.OrderItem;
import com.e.commerce.mini.models.Payment;

import java.util.List;

public class OrderMapper {

    public static OrderResponseDTO toOrderDTO(
            Order order
    ) {

        OrderResponseDTO dto =
                new OrderResponseDTO();

        dto.setId(
                order.getId()
        );

        dto.setOrderNumber(
                order.getOrderNumber()
        );

        dto.setStatus(
                order.getStatus()
        );

        dto.setTotalAmount(
                order.getTotalAmount()
        );

        if (order.getUser() != null) {

            dto.setCustomerName(
                    order.getUser().getFullName()
            );

            dto.setCustomerEmail(
                    order.getUser().getEmail()
            );

            dto.setCustomerMobile(
                    order.getUser().getContactNo()
            );
        }

        Payment payment =
                order.getPayment();

        if (payment != null) {

            dto.setPaymentMethod(
                    payment
                            .getPaymentMethod()
                            .name()
            );

            dto.setPaymentStatus(
                    payment
                            .getPaymentStatus()
                            .name()
            );

            dto.setPaymentAmount(
                    payment.getAmount()
            );

            dto.setRazorpayOrderId(
                    payment.getRazorpayOrderId()
            );

            dto.setRazorpayPaymentId(
                    payment.getRazorpayPaymentId()
            );

            dto.setPaidAt(
                    payment.getPaidAt()
            );
        }

        dto.setShippingFullName(
                order.getShippingFullName()
        );

        dto.setShippingContactNo(
                order.getShippingContactNo()
        );

        dto.setShippingAddressLine(
                order.getShippingAddressLine()
        );

        dto.setShippingCity(
                order.getShippingCity()
        );

        dto.setShippingState(
                order.getShippingState()
        );

        dto.setShippingPincode(
                order.getShippingPincode()
        );

        dto.setShippingCountry(
                order.getShippingCountry()
        );

        dto.setCreatedAt(
                order.getCreatedAt()
        );

        dto.setUpdatedAt(
                order.getUpdatedAt()
        );

        List<OrderItemResponseDTO> items =
                order.getOrderItems()
                        .stream()
                        .map(
                                OrderMapper::toOrderItemDTO
                        )
                        .toList();

        dto.setOrderItems(
                items
        );

        return dto;
    }

    public static OrderItemResponseDTO toOrderItemDTO(
            OrderItem item
    ) {

        OrderItemResponseDTO dto =
                new OrderItemResponseDTO();

        dto.setId(
                item.getId()
        );

        dto.setProductId(
                item.getProduct().getId()
        );

        dto.setProductName(
                item.getProductName()
        );

        dto.setSku(
                item.getSku()
        );

        dto.setQuantity(
                item.getQuantity()
        );

        dto.setUnitPrice(
                item.getUnitPrice()
        );

        dto.setSubtotal(
                item.getSubtotal()
        );

        return dto;
    }
}