package com.e.commerce.mini.Service.implementation;

import com.e.commerce.mini.DTO.request.ProductRequestDTO;
import com.e.commerce.mini.DTO.response.ProductResponseDTO;
import com.e.commerce.mini.Enums.Status;
import com.e.commerce.mini.Exception.invalidRequestException;
import com.e.commerce.mini.Mapper.ProductMapper;
import com.e.commerce.mini.Repository.BrandRepository;
import com.e.commerce.mini.Repository.CategoryRepository;
import com.e.commerce.mini.Repository.ProductImageRepository;
import com.e.commerce.mini.Repository.ProductRepository;
import com.e.commerce.mini.Service.ProductService;
import com.e.commerce.mini.models.Brand;
import com.e.commerce.mini.models.Category;
import com.e.commerce.mini.models.Product;
import com.e.commerce.mini.models.ProductImage;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ProductServiceimpl implements ProductService {

    private final ProductRepository repo;

    private final CategoryRepository categoryRepository;

    private final BrandRepository brandRepository;

    private final ProductImageRepository productImageRepository;

    @Value("${app.upload.dir:uploads/products}")
    private String uploadDirectory;

    @Override
    public ProductResponseDTO createNewProduct(
            ProductRequestDTO dto
    ) {

        Category category = categoryRepository
                .findById(dto.getCategoryId())
                .orElseThrow(() ->
                        new invalidRequestException(
                                "Category not found with id "
                                        + dto.getCategoryId()
                        )
                );

        Product product = ProductMapper.toEntity(dto);

        product.setCategory(category);

        if (dto.getBrandId() != null) {

            Brand brand = brandRepository
                    .findById(dto.getBrandId())
                    .orElseThrow(() ->
                            new invalidRequestException(
                                    "Brand not found with id "
                                            + dto.getBrandId()
                            )
                    );

            product.setBrand(brand);
        }

        Product savedProduct = repo.save(product);

        return ProductMapper.toDTO(savedProduct);
    }

    @Override
    @Transactional
    public ProductResponseDTO createProductWithImages(
            ProductRequestDTO dto,
            List<MultipartFile> images,
            Integer primaryImageIndex
    ) {

        Category category = categoryRepository
                .findById(dto.getCategoryId())
                .orElseThrow(() ->
                        new invalidRequestException(
                                "Category not found with id "
                                        + dto.getCategoryId()
                        )
                );

        Product product = ProductMapper.toEntity(dto);

        product.setCategory(category);

        if (dto.getBrandId() != null) {

            Brand brand = brandRepository
                    .findById(dto.getBrandId())
                    .orElseThrow(() ->
                            new invalidRequestException(
                                    "Brand not found with id "
                                            + dto.getBrandId()
                            )
                    );

            product.setBrand(brand);
        }

        Product savedProduct = repo.save(product);

        if (images == null || images.isEmpty()) {
            return ProductMapper.toDTO(savedProduct);
        }

        if (images.size() > 5) {
            throw new invalidRequestException(
                    "A product can have a maximum of 5 images."
            );
        }

        int primaryIndex = primaryImageIndex == null
                ? 0
                : primaryImageIndex;

        if (primaryIndex < 0 ||
                primaryIndex >= images.size()) {

            throw new invalidRequestException(
                    "Invalid primary image index."
            );
        }

        List<Path> uploadedFiles = new ArrayList<>();

        try {

            Path uploadPath = Paths
                    .get(uploadDirectory)
                    .toAbsolutePath()
                    .normalize();

            Files.createDirectories(uploadPath);

            for (int i = 0; i < images.size(); i++) {

                MultipartFile file = images.get(i);

                validateImage(file);

                String extension =
                        getFileExtension(
                                file.getOriginalFilename()
                        );

                String generatedFileName =
                        UUID.randomUUID() + extension;

                Path targetPath =
                        uploadPath.resolve(
                                generatedFileName
                        );

                Files.copy(
                        file.getInputStream(),
                        targetPath,
                        StandardCopyOption.REPLACE_EXISTING
                );

                uploadedFiles.add(targetPath);

                ProductImage productImage =
                        new ProductImage();

                productImage.setProduct(savedProduct);

                productImage.setImageUrl(
                        "/uploads/products/"
                                + generatedFileName
                );

                productImage.setPrimary(
                        i == primaryIndex
                );

                productImage.setDisplayOrder(i);

                productImage.setCreatedAt(
                        LocalDateTime.now()
                );

                productImageRepository.save(
                        productImage
                );
            }

            return ProductMapper.toDTO(savedProduct);

        } catch (IOException exception) {

            for (Path uploadedFile : uploadedFiles) {

                try {
                    Files.deleteIfExists(
                            uploadedFile
                    );
                } catch (IOException ignored) {
                }
            }

            throw new RuntimeException(
                    "Failed to store product images.",
                    exception
            );
        }
    }

    @Override
    public List<ProductResponseDTO> getAllProduct(
            int page,
            int size
    ) {

        Pageable pageable =
                PageRequest.of(page, size);

        Page<Product> record =
                repo.findAll(pageable);

        return record
                .stream()
                .map(ProductMapper::toDTO)
                .toList();
    }

    @Override
    public List<ProductResponseDTO> getProductByStatus(
            Status status
    ) {

        return repo.findByStatus(status)
                .stream()
                .map(ProductMapper::toDTO)
                .toList();
    }

    @Override
    public ProductResponseDTO updateProduct(
            Long id,
            ProductRequestDTO dto
    ) {

        Product product = repo.findById(id)
                .orElseThrow(() ->
                        new invalidRequestException(
                                "Product not found by id "
                                        + id
                        )
                );

        Category category = categoryRepository
                .findById(dto.getCategoryId())
                .orElseThrow(() ->
                        new invalidRequestException(
                                "Category not found with id "
                                        + dto.getCategoryId()
                        )
                );

        product.setName(dto.getName());
        product.setSku(dto.getSku());
        product.setPrice(dto.getPrice());
        product.setQuantity(dto.getQuantity());
        product.setDescription(dto.getDescription());
        product.setCategory(category);

        if (dto.getBrandId() != null) {

            Brand brand = brandRepository
                    .findById(dto.getBrandId())
                    .orElseThrow(() ->
                            new invalidRequestException(
                                    "Brand not found with id "
                                            + dto.getBrandId()
                            )
                    );

            product.setBrand(brand);

        } else {

            product.setBrand(null);
        }

        Product savedProduct =
                repo.save(product);

        return ProductMapper.toDTO(savedProduct);
    }

    @Override
    public ProductResponseDTO markStockOver(
            Long id
    ) {

        Product product = repo.findById(id)
                .orElseThrow(() ->
                        new invalidRequestException(
                                "Product not found by id "
                                        + id
                        )
                );

        product.setStatus(
                Status.OUT_OF_STOCK
        );

        Product savedProduct =
                repo.save(product);

        return ProductMapper.toDTO(savedProduct);
    }

    @Override
    public ProductResponseDTO markNotAvailaible(
            Long id
    ) {

        Product product = repo.findById(id)
                .orElseThrow(() ->
                        new invalidRequestException(
                                "Product not found by id "
                                        + id
                        )
                );

        product.setStatus(
                Status.DISCONTINUED
        );

        Product savedProduct =
                repo.save(product);

        return ProductMapper.toDTO(savedProduct);
    }

    @Override
    public void deleteProduct(
            Long id
    ) {

        Product product = repo.findById(id)
                .orElseThrow(() ->
                        new invalidRequestException(
                                "Product not found by id "
                                        + id
                        )
                );

        repo.delete(product);
    }

    private void validateImage(
            MultipartFile file
    ) {

        if (file == null || file.isEmpty()) {
            throw new invalidRequestException(
                    "Image file cannot be empty."
            );
        }

        long maxFileSize =
                5 * 1024 * 1024;

        if (file.getSize() > maxFileSize) {
            throw new invalidRequestException(
                    "Each image must be 5 MB or smaller."
            );
        }

        String fileName =
                file.getOriginalFilename();

        if (fileName == null ||
                !fileName.contains(".")) {

            throw new invalidRequestException(
                    "Image file must have a valid extension."
            );
        }

        String extension =
                fileName
                        .substring(
                                fileName.lastIndexOf(".")
                        )
                        .toLowerCase();

        boolean validExtension =
                extension.equals(".jpg")
                        ||
                        extension.equals(".jpeg")
                        ||
                        extension.equals(".jfif")
                        ||
                        extension.equals(".png")
                        ||
                        extension.equals(".webp");

        if (!validExtension) {

            throw new invalidRequestException(
                    "Only JPG, JPEG, JFIF, PNG and WEBP images are allowed."
            );
        }
    }

    private String getFileExtension(
            String fileName
    ) {

        if (fileName == null ||
                !fileName.contains(".")) {

            throw new invalidRequestException(
                    "Image file must have a valid extension."
            );
        }

        String extension =
                fileName
                        .substring(
                                fileName.lastIndexOf(".")
                        )
                        .toLowerCase();

        if (extension.equals(".jfif") ||
                extension.equals(".jpeg")) {

            return ".jpg";
        }

        return extension;
    }
}