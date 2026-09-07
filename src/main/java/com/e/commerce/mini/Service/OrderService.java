package com.e.commerce.mini.Service;

import com.e.commerce.mini.DTO.request.CreateOrderRequestDTO;
import com.e.commerce.mini.DTO.response.OrderResponseDTO;

import java.util.List;

public interface OrderService {

    OrderResponseDTO createOrder(
            String username,
            CreateOrderRequestDTO dto
    );

    List<OrderResponseDTO> getMyOrders(
            String username
    );

    OrderResponseDTO getMyOrderById(
            String username,
            Long orderId
    );

    OrderResponseDTO cancelOrder(
            String username,
            Long orderId
    );

    List<OrderResponseDTO> getAllOrdersForAdmin();

    OrderResponseDTO confirmOrderForAdmin(
            Long orderId
    );

    OrderResponseDTO markCodPaymentPaidForAdmin(
            Long orderId
    );

    OrderResponseDTO getOrderByIdForAdmin(
            Long orderId
    );
}