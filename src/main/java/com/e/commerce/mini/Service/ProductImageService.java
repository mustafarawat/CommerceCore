package com.e.commerce.mini.services;

import com.e.commerce.mini.models.ProductImage;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface ProductImageService {

    ProductImage uploadImage(
            Long productId,
            MultipartFile file,
            Boolean isPrimary,
            Integer displayOrder
    );

    List<ProductImage> getProductImages(Long productId);

    ProductImage setPrimaryImage(
            Long productId,
            Long imageId
    );

    void deleteImage(
            Long productId,
            Long imageId
    );
}