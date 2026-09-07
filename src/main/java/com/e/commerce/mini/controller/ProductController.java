package com.e.commerce.mini.controller;

import com.e.commerce.mini.DTO.request.ProductRequestDTO;
import com.e.commerce.mini.DTO.response.ProductResponseDTO;
import com.e.commerce.mini.Enums.Status;
import com.e.commerce.mini.Service.ProductService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/api/products")
@RequiredArgsConstructor
public class ProductController {

    private final ProductService service;

    @PostMapping
    public ProductResponseDTO createNewProduct(
            @Valid @RequestBody ProductRequestDTO dto
    ) {

        return service.createNewProduct(dto);
    }

    @PostMapping(
            value = "/with-images",
            consumes = MediaType.MULTIPART_FORM_DATA_VALUE
    )
    public ProductResponseDTO createProductWithImages(
            @Valid @ModelAttribute ProductRequestDTO dto,
            @RequestParam(
                    value = "images",
                    required = false
            )
            List<MultipartFile> images,
            @RequestParam(
                    value = "primaryImageIndex",
                    required = false,
                    defaultValue = "0"
            )
            Integer primaryImageIndex
    ) {

        return service.createProductWithImages(
                dto,
                images,
                primaryImageIndex
        );
    }

    @GetMapping
    public List<ProductResponseDTO> getAllProduct(
            @RequestParam(
                    defaultValue = "0"
            )
            int page,

            @RequestParam(
                    defaultValue = "5"
            )
            int size
    ) {

        return service.getAllProduct(
                page,
                size
        );
    }

    @GetMapping("/Status/{status}")
    public List<ProductResponseDTO> getProductByStatus(
            @PathVariable Status status
    ) {

        return service.getProductByStatus(
                status
        );
    }

    @PutMapping("/{id}")
    public ProductResponseDTO updateProduct(
            @PathVariable Long id,
            @Valid @RequestBody ProductRequestDTO dto
    ) {

        return service.updateProduct(
                id,
                dto
        );
    }

    @PatchMapping("/{id}/Stock-Over")
    public ProductResponseDTO markStockOver(
            @PathVariable Long id
    ) {

        return service.markStockOver(id);
    }

    @PatchMapping("/{id}/Not-Availaible")
    public ProductResponseDTO markNotAvailaible(
            @PathVariable Long id
    ) {

        return service.markNotAvailaible(id);
    }

    @DeleteMapping("/{id}")
    public void deleteProductById(
            @PathVariable Long id
    ) {

        service.deleteProduct(id);
    }
}