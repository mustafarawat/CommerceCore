package com.e.commerce.mini.DTO.response;

import com.e.commerce.mini.Enums.Role;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
public class UserResponseDTO {

    private Long id;

    private String username;

    private String email;

    private String fullName;

    private String contactNo;

    private Role role;

    private Boolean enabled;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;
}