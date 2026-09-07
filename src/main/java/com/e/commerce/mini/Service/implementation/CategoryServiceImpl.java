package com.e.commerce.mini.Service.implementation;

import com.e.commerce.mini.DTO.request.CategoryRequestDTO;
import com.e.commerce.mini.DTO.response.CategoryResponseDTO;
import com.e.commerce.mini.Exception.CategoryAlreadyExistsException;
import com.e.commerce.mini.Exception.CategoryNotFoundException;
import com.e.commerce.mini.Mapper.CategoryMapper;
import com.e.commerce.mini.Repository.CategoryRepository;
import com.e.commerce.mini.Service.CategoryService;
import com.e.commerce.mini.models.Category;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class CategoryServiceImpl implements CategoryService {

    private final CategoryRepository categoryRepository;

    @Override
    public CategoryResponseDTO createCategory(CategoryRequestDTO dto) {

        String categoryName = dto.getName().trim();

        if (categoryRepository.existsByNameIgnoreCase(categoryName)) {
            throw new CategoryAlreadyExistsException(
                    "Category already exists: " + categoryName
            );
        }

        Category category = CategoryMapper.toEntity(dto);
        category.setName(categoryName);

        Category savedCategory = categoryRepository.save(category);

        return CategoryMapper.toDTO(savedCategory);
    }

    @Override
    @Transactional(readOnly = true)
    public List<CategoryResponseDTO> getAllCategories() {

        return categoryRepository
                .findAll()
                .stream()
                .map(CategoryMapper::toDTO)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public CategoryResponseDTO getCategoryById(Long id) {

        Category category = categoryRepository
                .findById(id)
                .orElseThrow(() ->
                        new CategoryNotFoundException(
                                "Category not found with id: " + id
                        )
                );

        return CategoryMapper.toDTO(category);
    }

    @Override
    public CategoryResponseDTO updateCategory(
            Long id,
            CategoryRequestDTO dto
    ) {

        Category category = categoryRepository
                .findById(id)
                .orElseThrow(() ->
                        new RuntimeException("Category not found")
                );

        String categoryName = dto.getName().trim();

        if (categoryRepository.existsByNameIgnoreCase(categoryName)
                && !category.getName().equalsIgnoreCase(categoryName)) {

            throw new CategoryAlreadyExistsException(
                    "Category already exists: " + categoryName
            );
        }

        category.setName(categoryName);
        category.setDescription(dto.getDescription());

        Category updatedCategory = categoryRepository.save(category);

        return CategoryMapper.toDTO(updatedCategory);
    }

    @Override
    public void deleteCategory(Long id) {

        Category category = categoryRepository
                .findById(id)
                .orElseThrow(() ->
                        new RuntimeException("Category not found")
                );

        categoryRepository.delete(category);
    }
}