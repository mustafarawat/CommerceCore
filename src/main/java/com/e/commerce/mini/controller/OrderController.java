package com.e.commerce.mini.controller;

import com.e.commerce.mini.DTO.request.CreateOrderRequestDTO;
import com.e.commerce.mini.DTO.response.OrderResponseDTO;
import com.e.commerce.mini.Service.OrderService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;

    @PostMapping("/orders")
    public OrderResponseDTO createOrder(
            Authentication authentication,
            @Valid @RequestBody CreateOrderRequestDTO dto
    ) {

        return orderService.createOrder(
                authentication.getName(),
                dto
        );
    }

    @GetMapping("/orders")
    public List<OrderResponseDTO> getMyOrders(
            Authentication authentication
    ) {

        return orderService.getMyOrders(
                authentication.getName()
        );
    }

    @GetMapping("/orders/{id}")
    public OrderResponseDTO getMyOrderById(
            Authentication authentication,
            @PathVariable Long id
    ) {

        return orderService.getMyOrderById(
                authentication.getName(),
                id
        );
    }

    @PatchMapping("/orders/{id}/cancel")
    public OrderResponseDTO cancelOrder(
            Authentication authentication,
            @PathVariable Long id
    ) {

        return orderService.cancelOrder(
                authentication.getName(),
                id
        );
    }

    @GetMapping("/admin/orders")
    public List<OrderResponseDTO> getAllOrdersForAdmin() {

        return orderService.getAllOrdersForAdmin();
    }

    @PatchMapping("/admin/orders/{id}/confirm")
    public OrderResponseDTO confirmOrderForAdmin(
            @PathVariable Long id
    ) {

        return orderService.confirmOrderForAdmin(id);
    }

    @PatchMapping("/admin/orders/{id}/payment/paid")
    public OrderResponseDTO markCodPaymentPaidForAdmin(
            @PathVariable Long id
    ) {

        return orderService.markCodPaymentPaidForAdmin(id);
    }

    @GetMapping("/admin/orders/{id}")
    public OrderResponseDTO getOrderByIdForAdmin(
            @PathVariable Long id
    ) {

        return orderService.getOrderByIdForAdmin(id);
    }
}