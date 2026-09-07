package com.e.commerce.mini.controller;

import com.e.commerce.mini.DTO.request.AddToCartRequestDTO;
import com.e.commerce.mini.DTO.response.CartResponseDTO;
import com.e.commerce.mini.Service.CartService;
import jakarta.validation.Valid;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/cart")
public class CartController {

    private final CartService cartService;

    public CartController(CartService cartService) {
        this.cartService = cartService;
    }

    @PostMapping("/items")
    public CartResponseDTO addToCart(
            Authentication authentication,
            @Valid @RequestBody AddToCartRequestDTO dto) {
        return cartService.addToCart(authentication.getName(), dto);
    }

    @GetMapping
    public CartResponseDTO getCart(Authentication authentication) {
        return cartService.getCart(authentication.getName()
        );
    }

    @PutMapping("/items/{cartItemId}")
    public CartResponseDTO updateCartItem(
            Authentication authentication,
            @PathVariable Long cartItemId,
            @RequestParam Integer quantity
    ) {

        return cartService.updateCartItem(
                authentication.getName(),
                cartItemId,
                quantity
        );
    }

    @DeleteMapping("/items/{cartItemId}")
    public void removeCartItem(Authentication authentication, @PathVariable Long cartItemId) {
        cartService.removeCartItem(authentication.getName(), cartItemId);
    }

    @DeleteMapping
    public void clearCart(Authentication authentication) {
        cartService.clearCart(authentication.getName());
    }
}