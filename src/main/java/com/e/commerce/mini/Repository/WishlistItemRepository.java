package com.e.commerce.mini.Repository;

import com.e.commerce.mini.models.Product;
import com.e.commerce.mini.models.Wishlist;
import com.e.commerce.mini.models.WishlistItem;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface WishlistItemRepository extends JpaRepository<WishlistItem, Long> {

    Optional<WishlistItem> findByWishlistAndProduct(Wishlist wishlist, Product product);

    boolean existsByWishlistAndProduct(Wishlist wishlist, Product product);

    List<WishlistItem> findByWishlist(Wishlist wishlist);

    void deleteByWishlist(Wishlist wishlist);
}