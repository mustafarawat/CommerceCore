package com.e.commerce.mini.Service.implementation;

import com.e.commerce.mini.models.Product;
import com.e.commerce.mini.models.ProductImage;
import com.e.commerce.mini.Repository.ProductImageRepository;
import com.e.commerce.mini.Repository.ProductRepository;
import com.e.commerce.mini.services.ProductImageService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@Transactional
public class ProductImageServiceImpl implements ProductImageService {

    private final ProductImageRepository productImageRepository;
    private final ProductRepository productRepository;

    @Value("${app.upload.dir:uploads/products}")
    private String uploadDirectory;

    public ProductImageServiceImpl(
            ProductImageRepository productImageRepository,
            ProductRepository productRepository
    ) {
        this.productImageRepository = productImageRepository;
        this.productRepository = productRepository;
    }

    @Override
    public ProductImage uploadImage(
            Long productId,
            MultipartFile file,
            Boolean isPrimary,
            Integer displayOrder
    ) {

        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException(
                    "Image file cannot be empty."
            );
        }

        Product product = productRepository.findById(productId)
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "Product not found with id: " + productId
                        )
                );

        validateImage(file);

        try {
            Path uploadPath = Paths.get(uploadDirectory)
                    .toAbsolutePath()
                    .normalize();

            Files.createDirectories(uploadPath);

            String originalFileName = file.getOriginalFilename();

            String extension = getFileExtension(
                    originalFileName
            );

            String generatedFileName =
                    UUID.randomUUID() + extension;

            Path targetPath = uploadPath.resolve(
                    generatedFileName
            );

            Files.copy(
                    file.getInputStream(),
                    targetPath,
                    StandardCopyOption.REPLACE_EXISTING
            );

            boolean makePrimary =
                    Boolean.TRUE.equals(isPrimary);

            List<ProductImage> existingImages =
                    productImageRepository
                            .findByProductIdOrderByDisplayOrderAsc(
                                    productId
                            );

            if (existingImages.isEmpty()) {
                makePrimary = true;
            }

            if (makePrimary) {
                existingImages.forEach(image ->
                        image.setPrimary(false)
                );

                productImageRepository.saveAll(
                        existingImages
                );
            }

            ProductImage productImage =
                    new ProductImage();

            productImage.setProduct(product);
            productImage.setImageUrl(
                    "/uploads/products/" +
                            generatedFileName
            );
            productImage.setPrimary(makePrimary);

            productImage.setDisplayOrder(
                    displayOrder != null
                            ? displayOrder
                            : existingImages.size()
            );

            productImage.setCreatedAt(
                    LocalDateTime.now()
            );

            return productImageRepository.save(
                    productImage
            );

        } catch (IOException exception) {

            throw new RuntimeException(
                    "Failed to store product image.",
                    exception
            );
        }
    }

    @Override
    @Transactional(readOnly = true)
    public List<ProductImage> getProductImages(
            Long productId
    ) {

        if (!productRepository.existsById(productId)) {
            throw new IllegalArgumentException(
                    "Product not found with id: " + productId
            );
        }

        return productImageRepository
                .findByProductIdOrderByPrimaryDescDisplayOrderAsc(
                        productId
                );
    }

    @Override
    public ProductImage setPrimaryImage(
            Long productId,
            Long imageId
    ) {

        ProductImage image =
                productImageRepository.findById(imageId)
                        .orElseThrow(() ->
                                new IllegalArgumentException(
                                        "Product image not found with id: "
                                                + imageId
                                )
                        );

        if (!image.getProduct().getId().equals(productId)) {
            throw new IllegalArgumentException(
                    "Image does not belong to this product."
            );
        }

        List<ProductImage> images =
                productImageRepository
                        .findByProductIdOrderByDisplayOrderAsc(
                                productId
                        );

        images.forEach(existingImage ->
                existingImage.setPrimary(
                        existingImage.getId()
                                .equals(imageId)
                )
        );

        productImageRepository.saveAll(images);

        image.setPrimary(true);

        return image;
    }

    @Override
    public void deleteImage(
            Long productId,
            Long imageId
    ) {

        ProductImage image =
                productImageRepository.findById(imageId)
                        .orElseThrow(() ->
                                new IllegalArgumentException(
                                        "Product image not found with id: "
                                                + imageId
                                )
                        );

        if (!image.getProduct().getId().equals(productId)) {
            throw new IllegalArgumentException(
                    "Image does not belong to this product."
            );
        }

        try {

            String imageUrl =
                    image.getImageUrl();

            if (imageUrl != null) {

                String fileName =
                        Paths.get(imageUrl)
                                .getFileName()
                                .toString();

                Path filePath =
                        Paths.get(uploadDirectory)
                                .toAbsolutePath()
                                .normalize()
                                .resolve(fileName);

                Files.deleteIfExists(filePath);
            }

        } catch (IOException exception) {

            throw new RuntimeException(
                    "Failed to delete image file.",
                    exception
            );
        }

        boolean wasPrimary =
                image.isPrimary();

        productImageRepository.delete(image);

        if (wasPrimary) {

            List<ProductImage> remainingImages =
                    productImageRepository
                            .findByProductIdOrderByDisplayOrderAsc(
                                    productId
                            );

            if (!remainingImages.isEmpty()) {

                ProductImage newPrimary =
                        remainingImages.get(0);

                newPrimary.setPrimary(true);

                productImageRepository.save(
                        newPrimary
                );
            }
        }
    }

    private void validateImage(
            MultipartFile file
    ) {

        String contentType =
                file.getContentType();

        if (contentType == null ||
                !(
                        contentType.equals(
                                "image/jpeg"
                        )
                                ||
                                contentType.equals(
                                        "image/png"
                                )
                                ||
                                contentType.equals(
                                        "image/webp"
                                )
                )
        ) {

            throw new IllegalArgumentException(
                    "Only JPG, PNG and WEBP images are allowed."
            );
        }

        long maxFileSize =
                5 * 1024 * 1024;

        if (file.getSize() > maxFileSize) {

            throw new IllegalArgumentException(
                    "Image size cannot exceed 5 MB."
            );
        }
    }

    private String getFileExtension(
            String fileName
    ) {

        if (fileName == null ||
                !fileName.contains(".")) {

            throw new IllegalArgumentException(
                    "Image file must have a valid extension."
            );
        }

        return fileName.substring(
                fileName.lastIndexOf(".")
        ).toLowerCase();
    }
}