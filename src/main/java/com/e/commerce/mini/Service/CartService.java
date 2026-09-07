package com.e.commerce.mini.Service;

import com.e.commerce.mini.DTO.request.AddToCartRequestDTO;
import com.e.commerce.mini.DTO.response.CartResponseDTO;

public interface CartService {

    CartResponseDTO addToCart(String username, AddToCartRequestDTO dto);

    CartResponseDTO getCart(String username);

    CartResponseDTO updateCartItem(String username, Long cartItemId, Integer quantity);

    void removeCartItem(String username, Long cartItemId);

    void clearCart(String username);
}