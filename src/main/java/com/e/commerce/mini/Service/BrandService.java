package com.e.commerce.mini.Service;

import com.e.commerce.mini.DTO.request.BrandRequestDTO;
import com.e.commerce.mini.DTO.response.BrandResponseDTO;

import java.util.List;

public interface BrandService {

    BrandResponseDTO createBrand(BrandRequestDTO dto);
    List<BrandResponseDTO> getAllBrands();
    BrandResponseDTO getBrandById(Long id);
    BrandResponseDTO updateBrand(Long id, BrandRequestDTO dto);
    void deleteBrand(Long id);
}