package com.e.commerce.mini.DTO.request;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
public class ProductRequestDTO {

    @NotBlank(message = "Product name is required")
    @Size(
            min = 2,
            max = 150,
            message = "Product name must be between 2 and 150 characters"
    )
    private String name;

    @NotBlank(message = "SKU is required")
    @Size(
            min = 3,
            max = 50,
            message = "SKU must be between 3 and 50 characters"
    )
    private String sku;

    @NotNull(message = "Price is required")
    @DecimalMin(
            value = "0.01",
            message = "Price must be greater than 0"
    )
    private BigDecimal price;

    @NotNull(message = "Quantity is required")
    @Min(
            value = 0,
            message = "Quantity cannot be negative"
    )
    private Integer quantity;

    @Size(
            max = 2000,
            message = "Description cannot exceed 2000 characters"
    )
    private String description;

    @NotNull(message = "Category ID is required")
    private Long categoryId;

    private Long brandId;
}