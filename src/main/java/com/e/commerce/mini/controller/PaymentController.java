package com.e.commerce.mini.controller;

import com.e.commerce.mini.DTO.request.RazorpayVerifyRequestDTO;
import com.e.commerce.mini.DTO.response.PaymentResponseDTO;
import com.e.commerce.mini.DTO.response.RazorpayCheckoutResponseDTO;
import com.e.commerce.mini.Service.PaymentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/payments")
@RequiredArgsConstructor
public class PaymentController {

    private final PaymentService paymentService;

    @PostMapping("/{orderId}")
    public ResponseEntity<PaymentResponseDTO> createPayment(
            @PathVariable Long orderId,
            Authentication authentication
    ) {

        String username = authentication.getName();

        PaymentResponseDTO response =
                paymentService.createPayment(
                        username,
                        orderId
                );

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(response);
    }

    @GetMapping("/order/{orderId}")
    public PaymentResponseDTO getPaymentByOrder(
            Authentication authentication,
            @PathVariable Long orderId
    ) {

        return paymentService.getPaymentByOrder(
                authentication.getName(),
                orderId
        );
    }

    @GetMapping("/checkout/{orderId}")
    public RazorpayCheckoutResponseDTO getCheckoutDetails(
            Authentication authentication,
            @PathVariable Long orderId
    ) {

        return paymentService.getCheckoutDetails(
                authentication.getName(),
                orderId
        );
    }

    @PostMapping("/verify")
    public ResponseEntity<PaymentResponseDTO> verifyRazorpayPayment(
            Authentication authentication,
            @Valid @RequestBody RazorpayVerifyRequestDTO request
    ) {

        PaymentResponseDTO response =
                paymentService.verifyRazorpayPayment(
                        authentication.getName(),
                        request
                );

        return ResponseEntity.ok(response);
    }
}