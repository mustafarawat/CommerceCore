package com.e.commerce.mini.Mapper;

import com.e.commerce.mini.DTO.request.BrandRequestDTO;
import com.e.commerce.mini.DTO.response.BrandResponseDTO;
import com.e.commerce.mini.models.Brand;

public class BrandMapper {

    private BrandMapper() {}
    public static Brand toEntity(BrandRequestDTO dto) {
        Brand brand = new Brand();

        brand.setName(dto.getName());
        brand.setDescription(dto.getDescription());
        return brand;
    }

    public static BrandResponseDTO toDTO(Brand brand) {
        BrandResponseDTO dto = new BrandResponseDTO();

        dto.setId(brand.getId());
        dto.setName(brand.getName());
        dto.setDescription(brand.getDescription());
        return dto;
    }
}