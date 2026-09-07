package com.e.commerce.mini.Mapper;

import com.e.commerce.mini.DTO.request.ProductRequestDTO;
import com.e.commerce.mini.DTO.response.ProductImageResponseDTO;
import com.e.commerce.mini.DTO.response.ProductResponseDTO;
import com.e.commerce.mini.Enums.Status;
import com.e.commerce.mini.models.Product;
import com.e.commerce.mini.models.ProductImage;

public class ProductMapper {

    private ProductMapper() {
    }

    public static Product toEntity(ProductRequestDTO dto) {

        Product product = new Product();

        product.setName(dto.getName());
        product.setSku(dto.getSku());
        product.setPrice(dto.getPrice());
        product.setQuantity(dto.getQuantity());
        product.setDescription(dto.getDescription());
        product.setStatus(Status.ACTIVE);

        return product;
    }

    public static ProductResponseDTO toDTO(Product product) {

        ProductResponseDTO dto = new ProductResponseDTO();

        dto.setId(product.getId());
        dto.setName(product.getName());
        dto.setSku(product.getSku());
        dto.setPrice(product.getPrice());
        dto.setQuantity(product.getQuantity());
        dto.setDescription(product.getDescription());
        dto.setStatus(product.getStatus());
        dto.setCreatedAt(product.getCreatedAt());
        dto.setUpdatedAt(product.getUpdatedAt());

        if (product.getCategory() != null) {

            dto.setCategoryId(product.getCategory().getId());
            dto.setCategoryName(product.getCategory().getName());
        }

        if (product.getBrand() != null) {

            dto.setBrandId(product.getBrand().getId());
            dto.setBrandName(product.getBrand().getName());
        }

        if (product.getImages() != null) {

            dto.setImages(
                    product.getImages()
                            .stream()
                            .map(ProductMapper::toImageDTO)
                            .toList()
            );
        }

        return dto;
    }

    private static ProductImageResponseDTO toImageDTO(
            ProductImage image
    ) {

        ProductImageResponseDTO dto =
                new ProductImageResponseDTO();

        dto.setId(image.getId());
        dto.setImageUrl(image.getImageUrl());
        dto.setPrimary(image.isPrimary());
        dto.setDisplayOrder(image.getDisplayOrder());

        return dto;
    }
}