package com.e.commerce.mini.controller;

import com.e.commerce.mini.Enums.Role;
import com.e.commerce.mini.Mapper.UserMapper;
import com.e.commerce.mini.Repository.UserRepository;
import com.e.commerce.mini.DTO.response.UserResponseDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin/customers")
@RequiredArgsConstructor
public class AdminCustomerController {

    private final UserRepository userRepository;

    @GetMapping
    public List<UserResponseDTO> getCustomers() {

        return userRepository
                .findByRoleOrderByCreatedAtDesc(Role.CUSTOMER)
                .stream()
                .map(UserMapper::toDTO)
                .toList();
    }

    @DeleteMapping("/{id}")
    public void deleteCustomer(
            @PathVariable Long id
    ) {
        userRepository.deleteById(id);
    }
}