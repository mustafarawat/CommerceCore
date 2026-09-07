package com.e.commerce.mini.controller;

import com.e.commerce.mini.DTO.request.AddressRequestDTO;
import com.e.commerce.mini.DTO.response.AddressResponseDTO;
import com.e.commerce.mini.Service.AddressService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/addresses")
@RequiredArgsConstructor
public class AddressController {

    private final AddressService addressService;

    @PostMapping
    public ResponseEntity<AddressResponseDTO> createAddress(
            Authentication authentication,
            @Valid @RequestBody AddressRequestDTO dto
    ) {
        AddressResponseDTO response = addressService.createAddress(authentication.getName(), dto);
        return ResponseEntity.ok(response);
    }

    @GetMapping
    public ResponseEntity<List<AddressResponseDTO>> getAllAddresses(
            Authentication authentication
    ) {
        List<AddressResponseDTO> response = addressService.getAllAddresses(authentication.getName());
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{id}")
    public ResponseEntity<AddressResponseDTO> getAddressById(
            Authentication authentication,
            @PathVariable Long id
    ) {
        AddressResponseDTO response = addressService.getAddressById(authentication.getName(), id);
        return ResponseEntity.ok(response);
    }

    @PutMapping("/{id}")
    public ResponseEntity<AddressResponseDTO> updateAddress(
            Authentication authentication,
            @PathVariable Long id,
            @Valid @RequestBody AddressRequestDTO dto
    ) {
        AddressResponseDTO response = addressService.updateAddress(authentication.getName(), id, dto);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteAddress(
            Authentication authentication,
            @PathVariable Long id) {

        addressService.deleteAddress(authentication.getName(), id);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/{id}/default")
    public ResponseEntity<AddressResponseDTO> setDefaultAddress(
            Authentication authentication, @PathVariable Long id) {

        AddressResponseDTO response = addressService.setDefaultAddress(authentication.getName(), id);
        return ResponseEntity.ok(response);
    }
}