package com.e.commerce.mini.DTO.response;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.math.BigDecimal;

@Getter
@AllArgsConstructor
public class RazorpayCheckoutResponseDTO {

    private String keyId;

    private String razorpayOrderId;

    private BigDecimal amount;

    private String currency;

    private Long orderId;

    private String orderNumber;
}