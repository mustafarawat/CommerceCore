package com.e.commerce.mini.Service.implementation;

import com.e.commerce.mini.DTO.request.BrandRequestDTO;
import com.e.commerce.mini.DTO.response.BrandResponseDTO;
import com.e.commerce.mini.Exception.BrandAlreadyExistsException;
import com.e.commerce.mini.Exception.BrandInUseException;
import com.e.commerce.mini.Exception.BrandNotFoundException;
import com.e.commerce.mini.Mapper.BrandMapper;
import com.e.commerce.mini.Repository.BrandRepository;
import com.e.commerce.mini.Service.BrandService;
import com.e.commerce.mini.models.Brand;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class BrandServiceImpl implements BrandService {

    private final BrandRepository brandRepository;

    @Override
    public BrandResponseDTO createBrand(BrandRequestDTO dto) {

        String brandName = dto.getName().trim();

        if (brandRepository.existsByNameIgnoreCase(brandName)) {

            throw new BrandAlreadyExistsException(
                    "Brand already exists: " + brandName
            );
        }

        Brand brand = BrandMapper.toEntity(dto);

        brand.setName(brandName);

        Brand savedBrand = brandRepository.save(brand);

        return BrandMapper.toDTO(savedBrand);
    }

    @Override
    @Transactional(readOnly = true)
    public List<BrandResponseDTO> getAllBrands() {

        return brandRepository
                .findAll()
                .stream()
                .map(BrandMapper::toDTO)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public BrandResponseDTO getBrandById(Long id) {

        Brand brand = brandRepository
                .findById(id)
                .orElseThrow(() ->
                        new BrandNotFoundException(
                                "Brand not found with id: " + id
                        )
                );

        return BrandMapper.toDTO(brand);
    }

    @Override
    public BrandResponseDTO updateBrand(
            Long id,
            BrandRequestDTO dto
    ) {

        Brand brand = brandRepository
                .findById(id)
                .orElseThrow(() ->
                        new BrandNotFoundException(
                                "Brand not found with id: " + id
                        )
                );

        String brandName = dto.getName().trim();

        if (brandRepository.existsByNameIgnoreCase(brandName)
                && !brand.getName().equalsIgnoreCase(brandName)) {

            throw new BrandAlreadyExistsException(
                    "Brand already exists: " + brandName
            );
        }

        brand.setName(brandName);
        brand.setDescription(dto.getDescription());

        Brand updatedBrand = brandRepository.save(brand);

        return BrandMapper.toDTO(updatedBrand);
    }

    @Override
    public void deleteBrand(Long id) {

        Brand brand = brandRepository
                .findById(id)
                .orElseThrow(() ->
                        new BrandNotFoundException(
                                "Brand not found with id: " + id
                        )
                );

        if (brandRepository.existsByIdAndProductsIsNotEmpty(id)) {

            throw new BrandInUseException(
                    "Brand cannot be deleted because products are associated with it."
            );
        }

        brandRepository.delete(brand);
    }
}