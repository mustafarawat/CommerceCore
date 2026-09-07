package com.e.commerce.mini.DTO.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class BrandRequestDTO {

    @NotBlank(message = "Brand name is required")
    @Size(
            min = 2,
            max = 100,
            message = "Brand name must be between 2 and 100 characters"
    )
    private String name;

    @Size(
            max = 500,
            message = "Brand description cannot exceed 500 characters"
    )
    private String description;
}