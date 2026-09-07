package com.e.commerce.mini.Mapper;

import com.e.commerce.mini.DTO.request.UserRequestDto;
import com.e.commerce.mini.DTO.response.UserResponseDTO;
import com.e.commerce.mini.Enums.Role;
import com.e.commerce.mini.models.User;

public class UserMapper {

    public static UserResponseDTO toDTO(User user) {

        UserResponseDTO dto = new UserResponseDTO();

        dto.setId(user.getId());
        dto.setUsername(user.getUsername());
        dto.setEmail(user.getEmail());
        dto.setFullName(user.getFullName());
        dto.setContactNo(user.getContactNo());
        dto.setRole(user.getRole());
        dto.setEnabled(user.getEnabled());
        dto.setCreatedAt(user.getCreatedAt());
        dto.setUpdatedAt(user.getUpdatedAt());

        return dto;
    }

    public static User toEntity(UserRequestDto dto) {

        User user = new User();

        user.setUsername(dto.getUsername());
        user.setEmail(dto.getEmail());
        user.setFullName(dto.getFullName());
        user.setContactNo(dto.getContactNo());
        user.setPassword(dto.getPassword());

        user.setRole(Role.CUSTOMER);
        user.setEnabled(true);

        return user;
    }
}