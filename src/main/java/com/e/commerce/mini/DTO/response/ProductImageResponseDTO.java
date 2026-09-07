package com.e.commerce.mini.DTO.response;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ProductImageResponseDTO {

    private Long id;

    private String imageUrl;

    private boolean primary;

    private Integer displayOrder;
}