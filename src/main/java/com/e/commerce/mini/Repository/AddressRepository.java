package com.e.commerce.mini.Repository;

import com.e.commerce.mini.models.Address;
import com.e.commerce.mini.models.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface AddressRepository extends JpaRepository<Address, Long> {

    List<Address> findByUser(User user);

    Optional<Address> findByIdAndUser(Long id, User user);

    Optional<Address> findByUserAndDefaultAddressTrue(User user);

    boolean existsByIdAndUser(Long id, User user);
}