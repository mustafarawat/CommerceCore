package com.e.commerce.mini.Service.implementation;

import com.e.commerce.mini.DTO.request.AddToWishlistRequestDTO;
import com.e.commerce.mini.DTO.response.WishlistItemResponseDTO;
import com.e.commerce.mini.DTO.response.WishlistResponseDTO;
import com.e.commerce.mini.Exception.productNotFoundException;
import com.e.commerce.mini.Exception.userNotFoundException;
import com.e.commerce.mini.Repository.ProductImageRepository;
import com.e.commerce.mini.Repository.ProductRepository;
import com.e.commerce.mini.Repository.UserRepository;
import com.e.commerce.mini.Repository.WishlistItemRepository;
import com.e.commerce.mini.Repository.WishlistRepository;
import com.e.commerce.mini.Service.WishlistService;
import com.e.commerce.mini.models.Product;
import com.e.commerce.mini.models.ProductImage;
import com.e.commerce.mini.models.User;
import com.e.commerce.mini.models.Wishlist;
import com.e.commerce.mini.models.WishlistItem;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Transactional
public class WishlistServiceImpl implements WishlistService {

    private final UserRepository userRepository;
    private final ProductRepository productRepository;
    private final ProductImageRepository productImageRepository;
    private final WishlistRepository wishlistRepository;
    private final WishlistItemRepository wishlistItemRepository;

    @Override
    @Transactional(readOnly = true)
    public WishlistResponseDTO getWishlist(String username) {

        User user = getUser(username);

        Wishlist wishlist = wishlistRepository
                .findByUser(user)
                .orElse(null);

        if (wishlist == null) {
            return buildEmptyWishlistResponse();
        }

        return buildWishlistResponse(wishlist);
    }

    @Override
    public WishlistResponseDTO addToWishlist(
            String username,
            AddToWishlistRequestDTO dto
    ) {

        User user = getUser(username);

        Product product = productRepository
                .findById(dto.getProductId())
                .orElseThrow(() ->
                        new productNotFoundException(
                                "Product not found with ID: "
                                        + dto.getProductId()
                        )
                );

        Wishlist wishlist = wishlistRepository
                .findByUser(user)
                .orElseGet(() -> {
                    Wishlist newWishlist = new Wishlist();
                    newWishlist.setUser(user);
                    return wishlistRepository.save(newWishlist);
                });

        if (wishlistItemRepository
                .existsByWishlistAndProduct(wishlist, product)) {

            throw new RuntimeException(
                    "Product is already in wishlist"
            );
        }

        WishlistItem wishlistItem = new WishlistItem();

        wishlistItem.setWishlist(wishlist);

        wishlistItem.setProduct(product);

        wishlistItemRepository.save(wishlistItem);

        return buildWishlistResponse(wishlist);
    }

    @Override
    public void removeFromWishlist(
            String username,
            Long wishlistItemId
    ) {

        User user = getUser(username);

        Wishlist wishlist = wishlistRepository
                .findByUser(user)
                .orElseThrow(() ->
                        new RuntimeException(
                                "Wishlist not found"
                        )
                );

        WishlistItem wishlistItem = wishlistItemRepository
                .findById(wishlistItemId)
                .orElseThrow(() ->
                        new RuntimeException(
                                "Wishlist item not found"
                        )
                );

        validateWishlistOwnership(
                wishlist,
                wishlistItem
        );

        wishlistItemRepository.delete(wishlistItem);
    }

    @Override
    public void removeProductFromWishlist(
            String username,
            Long productId
    ) {

        User user = getUser(username);

        Wishlist wishlist = wishlistRepository
                .findByUser(user)
                .orElseThrow(() ->
                        new RuntimeException(
                                "Wishlist not found"
                        )
                );

        Product product = productRepository
                .findById(productId)
                .orElseThrow(() ->
                        new productNotFoundException(
                                "Product not found with ID: "
                                        + productId
                        )
                );

        WishlistItem wishlistItem = wishlistItemRepository
                .findByWishlistAndProduct(
                        wishlist,
                        product
                )
                .orElseThrow(() ->
                        new RuntimeException(
                                "Product is not in wishlist"
                        )
                );

        wishlistItemRepository.delete(wishlistItem);
    }

    @Override
    public void clearWishlist(String username) {

        User user = getUser(username);

        Wishlist wishlist = wishlistRepository
                .findByUser(user)
                .orElse(null);

        if (wishlist != null) {
            wishlistItemRepository.deleteByWishlist(
                    wishlist
            );
        }
    }

    @Override
    @Transactional(readOnly = true)
    public boolean isProductWishlisted(
            String username,
            Long productId
    ) {

        User user = getUser(username);

        Wishlist wishlist = wishlistRepository
                .findByUser(user)
                .orElse(null);

        if (wishlist == null) {
            return false;
        }

        Product product = productRepository
                .findById(productId)
                .orElseThrow(() ->
                        new productNotFoundException(
                                "Product not found with ID: "
                                        + productId
                        )
                );

        return wishlistItemRepository
                .existsByWishlistAndProduct(
                        wishlist,
                        product
                );
    }

    private User getUser(String username) {

        return userRepository
                .findByUsername(username)
                .orElseThrow(() ->
                        new userNotFoundException(
                                "User not found with username: "
                                        + username
                        )
                );
    }

    private void validateWishlistOwnership(
            Wishlist wishlist,
            WishlistItem wishlistItem
    ) {

        if (!wishlistItem.getWishlist()
                .getId()
                .equals(wishlist.getId())) {

            throw new RuntimeException(
                    "Wishlist item does not belong to this user"
            );
        }
    }

    private WishlistResponseDTO buildWishlistResponse(
            Wishlist wishlist
    ) {

        List<WishlistItemResponseDTO> itemResponses =
                wishlistItemRepository
                        .findByWishlist(wishlist)
                        .stream()
                        .map(this::buildWishlistItemResponse)
                        .toList();

        WishlistResponseDTO response =
                new WishlistResponseDTO();

        response.setWishlistId(
                wishlist.getId()
        );

        response.setItems(
                itemResponses
        );

        response.setWishlist(
                itemResponses
        );

        response.setTotalItems(
                itemResponses.size()
        );

        return response;
    }

    private WishlistResponseDTO buildEmptyWishlistResponse() {

        WishlistResponseDTO response =
                new WishlistResponseDTO();

        List<WishlistItemResponseDTO> emptyItems =
                List.of();

        response.setWishlistId(null);

        response.setItems(
                emptyItems
        );

        response.setWishlist(
                emptyItems
        );

        response.setTotalItems(0);

        return response;
    }

    private WishlistItemResponseDTO buildWishlistItemResponse(
            WishlistItem wishlistItem
    ) {

        Product product =
                wishlistItem.getProduct();

        WishlistItemResponseDTO response =
                new WishlistItemResponseDTO();

        response.setWishlistItemId(
                wishlistItem.getId()
        );

        response.setProductId(
                product.getId()
        );

        response.setProductName(
                product.getName()
        );

        response.setSku(
                product.getSku()
        );

        response.setPrice(
                product.getPrice()
        );

        response.setAvailableQuantity(
                product.getQuantity()
        );

        response.setStatus(
                product.getStatus().name()
        );

        response.setAddedAt(
                wishlistItem.getCreatedAt()
        );

        ProductImage primaryImage =
                productImageRepository
                        .findByProductIdOrderByPrimaryDescDisplayOrderAsc(
                                product.getId()
                        )
                        .stream()
                        .findFirst()
                        .orElse(null);

        if (primaryImage != null) {

            response.setImage(
                    primaryImage.getImageUrl()
            );

            response.setImageUrl(
                    primaryImage.getImageUrl()
            );
        }

        Map<String, Object> category =
                new LinkedHashMap<>();

        if (product.getCategory() != null) {

            category.put(
                    "id",
                    product.getCategory().getId()
            );

            category.put(
                    "name",
                    product.getCategory().getName()
            );
        }

        response.setCategory(
                category
        );

        return response;
    }
}