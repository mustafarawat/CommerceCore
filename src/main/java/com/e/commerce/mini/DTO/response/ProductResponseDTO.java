package com.e.commerce.mini.DTO.response;

import com.e.commerce.mini.Enums.Status;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
public class ProductResponseDTO {

    private Long id;

    private String name;

    private String sku;

    private BigDecimal price;

    private Integer quantity;

    private String description;

    private Long categoryId;

    private String categoryName;

    private Long brandId;

    private String brandName;

    private Status status;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

    private List<ProductImageResponseDTO> images;
}