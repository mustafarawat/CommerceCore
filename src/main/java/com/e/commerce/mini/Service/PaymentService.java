package com.e.commerce.mini.Service;

import com.e.commerce.mini.DTO.request.RazorpayVerifyRequestDTO;
import com.e.commerce.mini.DTO.response.PaymentResponseDTO;
import com.e.commerce.mini.DTO.response.RazorpayCheckoutResponseDTO;

public interface PaymentService {

    PaymentResponseDTO createPayment(
            String username,
            Long orderId
    );

    PaymentResponseDTO getPaymentByOrder(
            String username,
            Long orderId
    );

    RazorpayCheckoutResponseDTO getCheckoutDetails(
            String username,
            Long orderId
    );

    PaymentResponseDTO verifyRazorpayPayment(
            String username,
            RazorpayVerifyRequestDTO request
    );


    PaymentResponseDTO refundPayment(
            String username,
            Long orderId
    );
}