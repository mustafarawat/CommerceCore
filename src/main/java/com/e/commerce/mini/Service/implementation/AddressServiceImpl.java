package com.e.commerce.mini.Service.implementation;

import com.e.commerce.mini.DTO.request.AddressRequestDTO;
import com.e.commerce.mini.DTO.response.AddressResponseDTO;
import com.e.commerce.mini.Exception.userNotFoundException;
import com.e.commerce.mini.Repository.AddressRepository;
import com.e.commerce.mini.Repository.UserRepository;
import com.e.commerce.mini.Service.AddressService;
import com.e.commerce.mini.models.Address;
import com.e.commerce.mini.models.User;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class AddressServiceImpl implements AddressService {

    private final AddressRepository addressRepository;
    private final UserRepository userRepository;

    @Override
    public AddressResponseDTO createAddress(String username, AddressRequestDTO dto) {

        User user = getUser(username);
        Address address = new Address();
        address.setUser(user);

        mapRequestToEntity(dto, address);

        if (Boolean.TRUE.equals(dto.getDefaultAddress())) {
            removeExistingDefaultAddress(user);
            address.setDefaultAddress(true);
        } else if (!addressRepository.findByUser(user).isEmpty()) {
            address.setDefaultAddress(false);
        } else {
            address.setDefaultAddress(true);
        }

        Address savedAddress = addressRepository.save(address);

        return mapToResponse(savedAddress);
    }

    @Override
    @Transactional(readOnly = true)
    public List<AddressResponseDTO> getAllAddresses(String username) {
        User user = getUser(username);
        return addressRepository
                .findByUser(user)
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public AddressResponseDTO getAddressById(String username, Long id) {
        User user = getUser(username);
        Address address = getUserAddress(id, user);
        return mapToResponse(address);
    }

    @Override
    public AddressResponseDTO updateAddress(String username, Long id, AddressRequestDTO dto) {
        User user = getUser(username);
        Address address = getUserAddress(id, user);
        mapRequestToEntity(dto, address);

        if (Boolean.TRUE.equals(dto.getDefaultAddress())) {

            removeExistingDefaultAddress(user);
            address.setDefaultAddress(true);

        } else {
            address.setDefaultAddress(false);
            boolean hasOtherDefaultAddress =
                    addressRepository
                            .findByUserAndDefaultAddressTrue(user)
                            .filter(defaultAddress ->
                                    !defaultAddress.getId().equals(address.getId()))
                            .isPresent();

            if (!hasOtherDefaultAddress) {
                address.setDefaultAddress(true);
            }
        }

        Address updatedAddress = addressRepository.save(address);
        return mapToResponse(updatedAddress);
    }

    @Override
    public void deleteAddress(String username, Long id) {
        User user = getUser(username);
        Address address = getUserAddress(id, user);
        boolean wasDefault = Boolean.TRUE.equals(
                address.getDefaultAddress());

        addressRepository.delete(address);

        if (wasDefault) {
            List<Address> remainingAddresses = addressRepository.findByUser(user);
            if (!remainingAddresses.isEmpty()) {
                Address newDefault = remainingAddresses.get(0);
                newDefault.setDefaultAddress(true);
                addressRepository.save(newDefault);
            }
        }
    }

    @Override
    public AddressResponseDTO setDefaultAddress(String username, Long id) {

        User user = getUser(username);

        Address address = getUserAddress(id, user);

        removeExistingDefaultAddress(user);

        address.setDefaultAddress(true);

        Address updatedAddress = addressRepository.save(address);

        return mapToResponse(updatedAddress);
    }

    private User getUser(String username) {

        return userRepository
                .findByUsername(username)
                .orElseThrow(() ->
                        new userNotFoundException(
                                "User not found with username: "
                                        + username
                        )
                );
    }

    private Address getUserAddress(Long id, User user) {
        return addressRepository
                .findByIdAndUser(id, user)
                .orElseThrow(() ->
                        new RuntimeException(
                                "Address not found"
                        )
                );
    }

    private void removeExistingDefaultAddress(User user) {
        addressRepository
                .findByUserAndDefaultAddressTrue(user)
                .ifPresent(address -> {

                    address.setDefaultAddress(false);

                    addressRepository.save(address);
                });
    }

    private void mapRequestToEntity(AddressRequestDTO dto, Address address) {

        address.setFullName(dto.getFullName());
        address.setContactNo(dto.getContactNo());
        address.setAddressLine(dto.getAddressLine());
        address.setCity(dto.getCity());
        address.setState(dto.getState());
        address.setPincode(dto.getPincode());

        if (dto.getCountry() == null ||
                dto.getCountry().isBlank()) {

            address.setCountry("India");

        } else {

            address.setCountry(dto.getCountry());
        }
    }

    private AddressResponseDTO mapToResponse(Address address) {

        AddressResponseDTO response = new AddressResponseDTO();

        response.setId(address.getId());
        response.setFullName(address.getFullName());
        response.setContactNo(address.getContactNo());
        response.setAddressLine(address.getAddressLine());
        response.setCity(address.getCity());
        response.setState(address.getState());
        response.setPincode(address.getPincode());
        response.setCountry(address.getCountry());
        response.setDefaultAddress(address.getDefaultAddress());
        response.setCreatedAt(address.getCreatedAt());
        response.setUpdatedAt(address.getUpdatedAt());

        return response;
    }
}