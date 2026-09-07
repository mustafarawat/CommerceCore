package com.e.commerce.mini.Repository;

import com.e.commerce.mini.models.User;
import com.e.commerce.mini.Enums.Role;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByUsername(String username);

    Optional<User> findByEmail(String email);

    Optional<User> findByContactNo(String contactNo);

    Optional<User> findByAuthProviderAndProviderId(
            String authProvider,
            String providerId
    );

    boolean existsByUsername(String username);

    boolean existsByEmail(String email);

    boolean existsByContactNo(String contactNo);

    List<User> findByRoleOrderByCreatedAtDesc(Role role);
}