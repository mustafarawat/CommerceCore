package com.e.commerce.mini.Mapper;

import com.e.commerce.mini.DTO.request.CategoryRequestDTO;
import com.e.commerce.mini.DTO.response.CategoryResponseDTO;
import com.e.commerce.mini.models.Category;

public class CategoryMapper {

    private CategoryMapper() {
    }

    public static Category toEntity(CategoryRequestDTO dto) {
        Category category = new Category();
        category.setName(dto.getName());
        category.setDescription(dto.getDescription());
        return category;}

    public static CategoryResponseDTO toDTO(Category category) {
        CategoryResponseDTO dto = new CategoryResponseDTO();
        dto.setId(category.getId());
        dto.setName(category.getName());
        dto.setDescription(category.getDescription());
        return dto;
    }
}