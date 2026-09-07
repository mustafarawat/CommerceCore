package com.e.commerce.mini.Repository;

import com.e.commerce.mini.models.ProductImage;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ProductImageRepository
        extends JpaRepository<ProductImage, Long> {

    List<ProductImage> findByProductIdOrderByPrimaryDescDisplayOrderAsc(
            Long productId
    );

    List<ProductImage> findByProductIdOrderByDisplayOrderAsc(
            Long productId
    );
}