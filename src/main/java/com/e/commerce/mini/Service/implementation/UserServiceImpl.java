package com.e.commerce.mini.Service.implementation;

import com.e.commerce.mini.DTO.request.LoginRequestDto;
import com.e.commerce.mini.DTO.request.RefreshTokenRequestDTO;
import com.e.commerce.mini.DTO.request.UserRequestDto;
import com.e.commerce.mini.DTO.response.AuthResponseDTO;
import com.e.commerce.mini.DTO.response.UserResponseDTO;
import com.e.commerce.mini.Enums.Role;
import com.e.commerce.mini.Exception.invalidRequestException;
import com.e.commerce.mini.Mapper.UserMapper;
import com.e.commerce.mini.Repository.OrderRepository;
import com.e.commerce.mini.Repository.UserRepository;
import com.e.commerce.mini.Service.RefreshTokenService;
import com.e.commerce.mini.Service.Userservice;
import com.e.commerce.mini.Util.Jwtutil;
import com.e.commerce.mini.models.RefreshToken;
import com.e.commerce.mini.models.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;

@Service
public class UserServiceImpl implements Userservice {

    private final UserRepository repo;
    private final PasswordEncoder encoder;
    private final AuthenticationManager authenticationManager;
    private final Jwtutil jwtutil;
    private final RefreshTokenService refreshTokenService;


    public UserServiceImpl(
            UserRepository repo,
            PasswordEncoder encoder,
            AuthenticationManager authenticationManager,
            Jwtutil jwtutil,
            RefreshTokenService refreshTokenService
    ) {
        this.repo = repo;
        this.encoder = encoder;
        this.authenticationManager = authenticationManager;
        this.jwtutil = jwtutil;
        this.refreshTokenService = refreshTokenService;
    }

    @Override
    public UserResponseDTO createNewUser(UserRequestDto dto) {

        if (repo.existsByUsername(dto.getUsername())) {
            throw new invalidRequestException(
                    "Username already exists"
            );
        }

        if (repo.existsByEmail(dto.getEmail())) {
            throw new invalidRequestException(
                    "Email already exists"
            );
        }

        if (
                dto.getContactNo() != null
                        && !dto.getContactNo().isBlank()
                        && repo.existsByContactNo(dto.getContactNo())
        ) {
            throw new invalidRequestException(
                    "Contact number already exists"
            );
        }

        User user = UserMapper.toEntity(dto);

        user.setPassword(
                encoder.encode(dto.getPassword())
        );

        user.setRole(Role.CUSTOMER);
        user.setEnabled(true);

        User savedUser = repo.save(user);

        return UserMapper.toDTO(savedUser);
    }

    @Override
    public AuthResponseDTO verifyUser(LoginRequestDto dto) {

        Authentication authentication =
                authenticationManager.authenticate(
                        new UsernamePasswordAuthenticationToken(
                                dto.getUsername(),
                                dto.getPassword()
                        )
                );

        if (!authentication.isAuthenticated()) {
            throw new invalidRequestException(
                    "Invalid username or password"
            );
        }

        User user = repo.findByUsername(
                        dto.getUsername()
                )
                .orElseThrow(
                        () -> new UsernameNotFoundException(
                                "User not found"
                        )
                );

        String accessToken =
                jwtutil.generateToken(user);

        RefreshToken refreshToken =
                refreshTokenService.createRefreshToken(user);

        return new AuthResponseDTO(
                accessToken,
                refreshToken.getToken(),
                "Bearer"
        );
    }

    @Override
    public AuthResponseDTO refreshToken(
            RefreshTokenRequestDTO dto
    ) {

        RefreshToken refreshToken =
                refreshTokenService.verifyRefreshToken(
                        dto.getRefreshToken()
                );

        User user = refreshToken.getUser();

        String accessToken =
                jwtutil.generateToken(user);

        return new AuthResponseDTO(
                accessToken,
                refreshToken.getToken(),
                "Bearer"
        );
    }

    @Override
    public List<UserResponseDTO> getAllUser(
            int page,
            int size
    ) {

        Pageable pageable =
                PageRequest.of(page, size);

        Page<User> users =
                repo.findAll(pageable);

        return users
                .stream()
                .map(UserMapper::toDTO)
                .toList();
    }

    @Override
    public UserResponseDTO getUserById(
            Long id
    ) {

        User user = repo.findById(id)
                .orElseThrow(
                        () -> new UsernameNotFoundException(
                                "User not found with id: " + id
                        )
                );

        return UserMapper.toDTO(user);
    }

    @Override
    public UserResponseDTO findByUsername(
            String username
    ) {

        User user = repo.findByUsername(username)
                .orElseThrow(
                        () -> new UsernameNotFoundException(
                                "Username not found: " + username
                        )
                );

        return UserMapper.toDTO(user);
    }

    @Override
    public UserResponseDTO updateUser(
            Long id,
            UserRequestDto dto
    ) {

        User user = repo.findById(id)
                .orElseThrow(
                        () -> new invalidRequestException(
                                "User not found with id: " + id
                        )
                );

        if (
                !user.getUsername().equals(dto.getUsername())
                        && repo.existsByUsername(dto.getUsername())
        ) {
            throw new invalidRequestException(
                    "Username already exists"
            );
        }

        if (
                !user.getEmail().equals(dto.getEmail())
                        && repo.existsByEmail(dto.getEmail())
        ) {
            throw new invalidRequestException(
                    "Email already exists"
            );
        }

        user.setUsername(dto.getUsername());
        user.setEmail(dto.getEmail());
        user.setFullName(dto.getFullName());
        user.setContactNo(dto.getContactNo());

        if (
                dto.getPassword() != null
                        && !dto.getPassword().isBlank()
        ) {
            user.setPassword(
                    encoder.encode(dto.getPassword())
            );
        }

        User savedUser = repo.save(user);

        return UserMapper.toDTO(savedUser);
    }

    @Override
    public UserResponseDTO getCurrentUser(
            String username
    ) {

        User user = repo.findByUsername(username)
                .orElseThrow(
                        () -> new RuntimeException(
                                "user not found "
                        )
                );

        return UserMapper.toDTO(user);
    }

    @Override
    public void deleteUser(
            Long id
    ) {

        if (!repo.existsById(id)) {
            throw new invalidRequestException(
                    "User not found with id: " + id
            );
        }

        repo.deleteById(id);
    }
}