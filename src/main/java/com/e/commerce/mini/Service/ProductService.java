package com.e.commerce.mini.Service;

import com.e.commerce.mini.DTO.request.ProductRequestDTO;
import com.e.commerce.mini.DTO.response.ProductResponseDTO;
import com.e.commerce.mini.Enums.Status;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface ProductService {

    ProductResponseDTO createNewProduct(ProductRequestDTO dto);

    ProductResponseDTO createProductWithImages(
            ProductRequestDTO dto,
            List<MultipartFile> images,
            Integer primaryImageIndex
    );

    List<ProductResponseDTO> getAllProduct(int page, int size);

    List<ProductResponseDTO> getProductByStatus(Status status);

    ProductResponseDTO updateProduct(Long id, ProductRequestDTO dto);

    ProductResponseDTO markStockOver(Long id);

    ProductResponseDTO markNotAvailaible(Long id);

    void deleteProduct(Long id);
}