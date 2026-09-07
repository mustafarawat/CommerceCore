package com.e.commerce.mini.Service;

import com.e.commerce.mini.DTO.request.AddToWishlistRequestDTO;
import com.e.commerce.mini.DTO.response.WishlistResponseDTO;

public interface WishlistService {

    WishlistResponseDTO getWishlist(String username);

    WishlistResponseDTO addToWishlist(
            String username,
            AddToWishlistRequestDTO dto
    );

    void removeFromWishlist(
            String username,
            Long wishlistItemId
    );

    void removeProductFromWishlist(
            String username,
            Long productId
    );

    void clearWishlist(String username);

    boolean isProductWishlisted(
            String username,
            Long productId
    );
}