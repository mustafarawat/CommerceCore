package com.e.commerce.mini.controller;

import com.e.commerce.mini.DTO.request.LoginRequestDto;
import com.e.commerce.mini.DTO.request.RefreshTokenRequestDTO;
import com.e.commerce.mini.DTO.request.UserRequestDto;
import com.e.commerce.mini.DTO.response.AuthResponseDTO;
import com.e.commerce.mini.DTO.response.UserResponseDTO;
import com.e.commerce.mini.Service.Userservice;
import jakarta.validation.Valid;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/users")
public class UserController {

    private final Userservice service;

    public UserController(Userservice service) {
        this.service = service;
    }

    @PostMapping("/register")
    public UserResponseDTO createNewUser(@Valid @RequestBody UserRequestDto dto) {
        return service.createNewUser(dto);
    }

    @PostMapping("/login")
    public AuthResponseDTO verifyUser(@Valid @RequestBody LoginRequestDto dto) {
        return service.verifyUser(dto);
    }

    @PostMapping("/refresh")
    public AuthResponseDTO refreshToken(
            @Valid @RequestBody RefreshTokenRequestDTO dto) {
        return service.refreshToken(dto);
    }

    @GetMapping
    public List<UserResponseDTO> getAllUser(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        return service.getAllUser(page, size);
    }

    @GetMapping("/{id}")
    public UserResponseDTO getUserById(
            @PathVariable Long id) {
        return service.getUserById(id);
    }

    @GetMapping("/username/{username}")
    public UserResponseDTO findByUsername(
            @PathVariable String username) {
        return service.findByUsername(username);
    }

    @GetMapping("/me")
    public UserResponseDTO getCurrentUser(Authentication authentication) {
        return service.getCurrentUser(authentication.getName());
    }

    @PutMapping("/{id}")
    public UserResponseDTO updateUser(
            @PathVariable Long id,
            @Valid @RequestBody UserRequestDto dto) {
        return service.updateUser(id, dto);
    }

    @DeleteMapping("/{id}")
    public void deleteUser(@PathVariable Long id) {
        service.deleteUser(id);
    }
}