package com.e.commerce.mini.DTO.response;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.List;

@Getter
@Setter
public class CartResponseDTO {

    private Long cartId;
    private List<CartItemResponseDTO> items;
    private Integer totalItems;
    private BigDecimal subtotal;
}