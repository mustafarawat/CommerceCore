package com.e.commerce.mini.Service;

import com.e.commerce.mini.DTO.request.LoginRequestDto;
import com.e.commerce.mini.DTO.request.RefreshTokenRequestDTO;
import com.e.commerce.mini.DTO.request.UserRequestDto;
import com.e.commerce.mini.DTO.response.AuthResponseDTO;
import com.e.commerce.mini.DTO.response.UserResponseDTO;

import java.util.List;

public interface Userservice {

    UserResponseDTO createNewUser(UserRequestDto dto);

    List<UserResponseDTO> getAllUser(int page, int size);

    UserResponseDTO getUserById(Long id);

    UserResponseDTO findByUsername(String username);

    UserResponseDTO updateUser(Long id, UserRequestDto dto);

    UserResponseDTO getCurrentUser(String username);

    AuthResponseDTO verifyUser(LoginRequestDto dto);

    AuthResponseDTO refreshToken(RefreshTokenRequestDTO dto);

    void deleteUser(Long id);
}