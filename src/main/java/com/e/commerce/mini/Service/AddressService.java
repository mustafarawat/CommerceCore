package com.e.commerce.mini.Service;

import com.e.commerce.mini.DTO.request.AddressRequestDTO;
import com.e.commerce.mini.DTO.response.AddressResponseDTO;

import java.util.List;

public interface AddressService {

    AddressResponseDTO createAddress(String username, AddressRequestDTO dto);

    List<AddressResponseDTO> getAllAddresses(String username);

    AddressResponseDTO getAddressById(String username, Long id);

    AddressResponseDTO updateAddress(String username, Long id, AddressRequestDTO dto);

    void deleteAddress(String username, Long id);

    AddressResponseDTO setDefaultAddress(String username, Long id);
}