package com.e.commerce.mini.controller;

import com.e.commerce.mini.models.ProductImage;
import com.e.commerce.mini.services.ProductImageService;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/api/products/{productId}/images")
public class ProductImageController {

    private final ProductImageService productImageService;

    public ProductImageController(
            ProductImageService productImageService
    ) {
        this.productImageService =
                productImageService;
    }

    @PostMapping(
            consumes = MediaType.MULTIPART_FORM_DATA_VALUE
    )
    public ResponseEntity<ProductImage> uploadImage(
            @PathVariable Long productId,
            @RequestParam("file") MultipartFile file,
            @RequestParam(
                    value = "isPrimary",
                    required = false,
                    defaultValue = "false"
            )
            Boolean isPrimary,
            @RequestParam(
                    value = "displayOrder",
                    required = false,
                    defaultValue = "0"
            )
            Integer displayOrder
    ) {

        ProductImage image =
                productImageService.uploadImage(
                        productId,
                        file,
                        isPrimary,
                        displayOrder
                );

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(image);
    }

    @GetMapping
    public ResponseEntity<List<ProductImage>> getImages(
            @PathVariable Long productId
    ) {

        return ResponseEntity.ok(
                productImageService.getProductImages(
                        productId
                )
        );
    }

    @PatchMapping("/{imageId}/primary")
    public ResponseEntity<ProductImage> setPrimaryImage(
            @PathVariable Long productId,
            @PathVariable Long imageId
    ) {

        return ResponseEntity.ok(
                productImageService.setPrimaryImage(
                        productId,
                        imageId
                )
        );
    }

    @DeleteMapping("/{imageId}")
    public ResponseEntity<Void> deleteImage(
            @PathVariable Long productId,
            @PathVariable Long imageId
    ) {

        productImageService.deleteImage(
                productId,
                imageId
        );

        return ResponseEntity.noContent()
                .build();
    }
}