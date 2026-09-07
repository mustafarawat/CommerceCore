package com.e.commerce.mini.controller;

import com.e.commerce.mini.DTO.request.AddToWishlistRequestDTO;
import com.e.commerce.mini.DTO.response.WishlistResponseDTO;
import com.e.commerce.mini.Service.WishlistService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/wishlist")
@RequiredArgsConstructor
public class WishlistController {

    private final WishlistService wishlistService;

    @GetMapping
    public ResponseEntity<WishlistResponseDTO> getWishlist(
            Authentication authentication
    ) {

        WishlistResponseDTO response =
                wishlistService.getWishlist(
                        authentication.getName()
                );

        return ResponseEntity.ok(response);
    }

    @PostMapping
    public ResponseEntity<WishlistResponseDTO> addToWishlistRoot(
            Authentication authentication,
            @Valid @RequestBody AddToWishlistRequestDTO dto
    ) {

        WishlistResponseDTO response =
                wishlistService.addToWishlist(
                        authentication.getName(),
                        dto
                );

        return ResponseEntity.ok(response);
    }

    @PostMapping("/items")
    public ResponseEntity<WishlistResponseDTO> addToWishlist(
            Authentication authentication,
            @Valid @RequestBody AddToWishlistRequestDTO dto
    ) {

        WishlistResponseDTO response =
                wishlistService.addToWishlist(
                        authentication.getName(),
                        dto
                );

        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{productId}/remove")
    public ResponseEntity<Void> removeProductFromWishlist(
            Authentication authentication,
            @PathVariable Long productId
    ) {

        wishlistService.removeProductFromWishlist(
                authentication.getName(),
                productId
        );

        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/items/{wishlistItemId}")
    public ResponseEntity<Void> removeFromWishlist(
            Authentication authentication,
            @PathVariable Long wishlistItemId
    ) {

        wishlistService.removeFromWishlist(
                authentication.getName(),
                wishlistItemId
        );

        return ResponseEntity.noContent().build();
    }

    @DeleteMapping
    public ResponseEntity<Void> clearWishlist(
            Authentication authentication
    ) {

        wishlistService.clearWishlist(
                authentication.getName()
        );

        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{productId}/check")
    public ResponseEntity<Map<String, Boolean>> checkWishlist(
            Authentication authentication,
            @PathVariable Long productId
    ) {

        boolean wishlisted =
                wishlistService.isProductWishlisted(
                        authentication.getName(),
                        productId
                );

        return ResponseEntity.ok(
                Map.of("is_wishlisted", wishlisted)
        );
    }

    @GetMapping("/items/{productId}/exists")
    public ResponseEntity<Map<String, Boolean>> isProductWishlisted(
            Authentication authentication,
            @PathVariable Long productId
    ) {

        boolean wishlisted =
                wishlistService.isProductWishlisted(
                        authentication.getName(),
                        productId
                );

        return ResponseEntity.ok(
                Map.of("wishlisted", wishlisted)
        );
    }
}