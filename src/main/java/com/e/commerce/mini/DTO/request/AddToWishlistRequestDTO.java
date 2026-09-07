package com.e.commerce.mini.DTO.request;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AddToWishlistRequestDTO {

    @NotNull(message = "Product ID is required")
    private Long productId;
}