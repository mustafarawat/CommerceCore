package com.e.commerce.mini.Service.implementation;

import com.e.commerce.mini.DTO.request.AddToCartRequestDTO;
import com.e.commerce.mini.DTO.response.CartItemResponseDTO;
import com.e.commerce.mini.DTO.response.CartResponseDTO;
import com.e.commerce.mini.Enums.Status;
import com.e.commerce.mini.Exception.invalidRequestException;
import com.e.commerce.mini.Repository.CartItemRepository;
import com.e.commerce.mini.Repository.CartRepository;
import com.e.commerce.mini.Repository.ProductRepository;
import com.e.commerce.mini.Repository.UserRepository;
import com.e.commerce.mini.Service.CartService;
import com.e.commerce.mini.models.Cart;
import com.e.commerce.mini.models.CartItem;
import com.e.commerce.mini.models.Product;
import com.e.commerce.mini.models.User;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class CartServiceImpl implements CartService {

    private final CartRepository cartRepository;
    private final CartItemRepository cartItemRepository;
    private final ProductRepository productRepository;
    private final UserRepository userRepository;

    @Override
    public CartResponseDTO addToCart(
            String username,
            AddToCartRequestDTO dto
    ) {

        User user = getUser(username);

        Product product = productRepository.findById(dto.getProductId())
                .orElseThrow(() ->
                        new invalidRequestException(
                                "Product not found"
                        )
                );

        validateProduct(product, dto.getQuantity());

        Cart cart = getOrCreateCart(user);

        CartItem cartItem = cartItemRepository
                .findByCartAndProduct(cart, product)
                .orElse(null);

        if (cartItem != null) {

            int newQuantity =
                    cartItem.getQuantity() + dto.getQuantity();

            validateProduct(product, newQuantity);

            cartItem.setQuantity(newQuantity);

        } else {

            cartItem = new CartItem();

            cartItem.setCart(cart);
            cartItem.setProduct(product);
            cartItem.setQuantity(dto.getQuantity());
        }

        cartItemRepository.save(cartItem);

        return buildCartResponse(cart);
    }

    @Override
    @Transactional(readOnly = true)
    public CartResponseDTO getCart(
            String username
    ) {

        User user = getUser(username);

        Cart cart = cartRepository.findByUser(user)
                .orElseThrow(() ->
                        new invalidRequestException(
                                "Cart not found"
                        )
                );

        return buildCartResponse(cart);
    }

    @Override
    public CartResponseDTO updateCartItem(
            String username,
            Long cartItemId,
            Integer quantity
    ) {

        if (quantity == null || quantity < 1) {
            throw new invalidRequestException(
                    "Quantity must be at least 1"
            );
        }

        User user = getUser(username);

        Cart cart = cartRepository.findByUser(user)
                .orElseThrow(() ->
                        new invalidRequestException(
                                "Cart not found"
                        )
                );

        CartItem cartItem = cartItemRepository.findById(cartItemId)
                .orElseThrow(() ->
                        new invalidRequestException(
                                "Cart item not found"
                        )
                );

        if (!cartItem.getCart().getId().equals(cart.getId())) {
            throw new invalidRequestException(
                    "Cart item does not belong to this user"
            );
        }

        Product product = cartItem.getProduct();

        validateProduct(product, quantity);

        cartItem.setQuantity(quantity);

        cartItemRepository.save(cartItem);

        return buildCartResponse(cart);
    }

    @Override
    public void removeCartItem(
            String username,
            Long cartItemId
    ) {

        User user = getUser(username);

        Cart cart = cartRepository.findByUser(user)
                .orElseThrow(() ->
                        new invalidRequestException(
                                "Cart not found"
                        )
                );

        CartItem cartItem = cartItemRepository.findById(cartItemId)
                .orElseThrow(() ->
                        new invalidRequestException(
                                "Cart item not found"
                        )
                );

        if (!cartItem.getCart().getId().equals(cart.getId())) {
            throw new invalidRequestException(
                    "Cart item does not belong to this user"
            );
        }

        cartItemRepository.delete(cartItem);
    }

    @Override
    public void clearCart(String username) {
        User user = getUser(username);
        Cart cart = cartRepository.findByUser(user)
                .orElseThrow(() ->
                        new invalidRequestException(
                                "Cart not found"
                        )
                );
        cartItemRepository.deleteByCart(cart);
    }

    private User getUser(String username) {

        return userRepository.findByUsername(username)
                .orElseThrow(() ->
                        new invalidRequestException("User not found"));
    }

    private Cart getOrCreateCart(User user) {
        return cartRepository.findByUser(user)
                .orElseGet(() -> {
                    Cart cart = new Cart();
                    cart.setUser(user);
                    return cartRepository.save(cart);
                });
    }

    private void validateProduct(
            Product product,
            Integer requestedQuantity
    ) {

        if (product.getStatus() != Status.ACTIVE) {
            throw new invalidRequestException(
                    "Product is not available"
            );
        }

        if (requestedQuantity > product.getQuantity()) {
            throw new invalidRequestException(
                    "Requested quantity is greater than available stock"
            );
        }
    }

    private CartResponseDTO buildCartResponse(
            Cart cart
    ) {

        List<CartItem> cartItems =
                cartItemRepository.findByCart(cart);

        List<CartItemResponseDTO> items = cartItems
                .stream()
                .map(this::toCartItemResponse)
                .toList();

        int totalItems = cartItems
                .stream()
                .mapToInt(CartItem::getQuantity)
                .sum();

        BigDecimal subtotal = items
                .stream()
                .map(CartItemResponseDTO::getSubtotal)
                .reduce(
                        BigDecimal.ZERO,
                        BigDecimal::add
                );

        CartResponseDTO response = new CartResponseDTO();

        response.setCartId(cart.getId());
        response.setItems(items);
        response.setTotalItems(totalItems);
        response.setSubtotal(subtotal);

        return response;
    }

    private CartItemResponseDTO toCartItemResponse(CartItem cartItem) {

        Product product = cartItem.getProduct();

        BigDecimal subtotal = product.getPrice()
                .multiply(BigDecimal.valueOf(cartItem.getQuantity()));
        CartItemResponseDTO response = new CartItemResponseDTO();

        response.setCartItemId(cartItem.getId());
        response.setProductId(product.getId());
        response.setProductName(product.getName());
        response.setSku(product.getSku());
        response.setPrice(product.getPrice());
        response.setQuantity(cartItem.getQuantity());
        response.setSubtotal(subtotal);

        return response;
    }
}