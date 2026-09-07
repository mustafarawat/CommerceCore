package com.e.commerce.mini.Repository;

import com.e.commerce.mini.models.Cart;
import com.e.commerce.mini.models.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface CartRepository extends JpaRepository<Cart, Long> {

    Optional<Cart> findByUser(User user);

    boolean existsByUser(User user);
}