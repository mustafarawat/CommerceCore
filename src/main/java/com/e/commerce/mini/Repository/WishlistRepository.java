package com.e.commerce.mini.Repository;

import com.e.commerce.mini.models.User;
import com.e.commerce.mini.models.Wishlist;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface WishlistRepository extends JpaRepository<Wishlist, Long> {

    Optional<Wishlist> findByUser(User user);

    boolean existsByUser(User user);
}