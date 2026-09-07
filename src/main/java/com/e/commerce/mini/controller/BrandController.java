package com.e.commerce.mini.controller;

import com.e.commerce.mini.DTO.request.BrandRequestDTO;
import com.e.commerce.mini.DTO.response.BrandResponseDTO;
import com.e.commerce.mini.Service.BrandService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
public class BrandController {

    private final BrandService brandService;

    @PostMapping("/api/admin/brands")
    public ResponseEntity<BrandResponseDTO> createBrand(
            @Valid @RequestBody BrandRequestDTO dto
    ) {

        BrandResponseDTO response = brandService.createBrand(dto);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(response);
    }

    @GetMapping("/api/brands")
    public ResponseEntity<List<BrandResponseDTO>> getAllBrands() {

        return ResponseEntity.ok(
                brandService.getAllBrands()
        );
    }

    @GetMapping("/api/brands/{id}")
    public ResponseEntity<BrandResponseDTO> getBrandById(
            @PathVariable Long id
    ) {

        return ResponseEntity.ok(
                brandService.getBrandById(id)
        );
    }

    @PutMapping("/api/admin/brands/{id}")
    public ResponseEntity<BrandResponseDTO> updateBrand(
            @PathVariable Long id,
            @Valid @RequestBody BrandRequestDTO dto
    ) {

        return ResponseEntity.ok(
                brandService.updateBrand(id, dto)
        );
    }

    @DeleteMapping("/api/admin/brands/{id}")
    public ResponseEntity<Void> deleteBrand(
            @PathVariable Long id
    ) {

        brandService.deleteBrand(id);

        return ResponseEntity.noContent().build();
    }
}