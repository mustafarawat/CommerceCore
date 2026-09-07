package com.e.commerce.mini.DTO.response;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Map;

@Getter
@Setter
public class WishlistItemResponseDTO {

    private Long wishlistItemId;

    private Long productId;

    private String productName;

    private String sku;

    private BigDecimal price;

    private BigDecimal oldPrice;

    private Integer availableQuantity;

    private String status;

    private String image;

    private String imageUrl;

    private Map<String, Object> category;

    private LocalDateTime addedAt;
}