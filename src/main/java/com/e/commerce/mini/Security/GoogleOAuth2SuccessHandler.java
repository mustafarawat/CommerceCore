package com.e.commerce.mini.Security;

import com.e.commerce.mini.Enums.Role;
import com.e.commerce.mini.Repository.UserRepository;
import com.e.commerce.mini.Service.RefreshTokenService;
import com.e.commerce.mini.Util.Jwtutil;
import com.e.commerce.mini.models.RefreshToken;
import com.e.commerce.mini.models.User;
import tools.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;
import com.e.commerce.mini.Mapper.UserMapper;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class GoogleOAuth2SuccessHandler implements AuthenticationSuccessHandler {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final Jwtutil jwtutil;
    private final RefreshTokenService refreshTokenService;
    private final ObjectMapper objectMapper;

    @Override
    public void onAuthenticationSuccess(
            HttpServletRequest request,
            HttpServletResponse response,
            Authentication authentication
    ) throws IOException, ServletException {

        OAuth2User googleUser = (OAuth2User) authentication.getPrincipal();

        String providerId = googleUser.getAttribute("sub");

        String email = googleUser.getAttribute("email");

        String fullName = googleUser.getAttribute("name");

        Boolean emailVerified = googleUser.getAttribute("email_verified");

        if (providerId == null || email == null) {
            response.setStatus(
                    HttpServletResponse.SC_BAD_REQUEST
            );
            response.setContentType("application/json");
            response.getWriter().write(
                    """
                    {
                        "status": false,
                        "message": "Google account information is incomplete."
                    }
                    """
            );
            return;
        }

        if (!Boolean.TRUE.equals(emailVerified)) {
            response.setStatus(
                    HttpServletResponse.SC_UNAUTHORIZED
            );
            response.setContentType("application/json");
            response.getWriter().write(
                    """
                    {
                        "status": false,
                        "message": "Google email is not verified."
                    }
                    """
            );
            return;
        }

        User user =
                userRepository
                        .findByAuthProviderAndProviderId(
                                "GOOGLE",
                                providerId
                        )
                        .orElseGet(() ->
                                createOrLinkGoogleUser(
                                        providerId,
                                        email,
                                        fullName
                                )
                        );

        String accessToken = jwtutil.generateToken(user);

        RefreshToken refreshToken = refreshTokenService.createRefreshToken(user);

        Map<String, Object> responseBody = new LinkedHashMap<>();
        responseBody.put("status", true);
        responseBody.put(
                "message",
                "Google login successful"
        );
        responseBody.put(
                "accessToken",
                accessToken
        );
        responseBody.put(
                "refreshToken",
                refreshToken.getToken()
        );
        responseBody.put(
                "tokenType",
                "Bearer"
        );
        responseBody.put(
                "user",
                UserMapper.toDTO(user)
        );
        response.setStatus(HttpServletResponse.SC_OK);

        response.setContentType("application/json");
        objectMapper.writeValue(response.getWriter(), responseBody);
    }

    private User createOrLinkGoogleUser(
            String providerId,
            String email,
            String fullName
    ) {

        User user = userRepository.findByEmail(email).orElse(null);

        if (user != null) {

            user.setAuthProvider("GOOGLE");
            user.setProviderId(providerId);

            if (fullName != null && !fullName.isBlank()) {
                user.setFullName(fullName);
            }
            return userRepository.save(user);
        }
        user = new User();
        user.setUsername(generateUniqueUsername(email));

        user.setPassword(passwordEncoder.encode(UUID.randomUUID().toString()));

        user.setEmail(email);
        user.setFullName(fullName);
        user.setRole(Role.CUSTOMER);
        user.setEnabled(true);
        user.setAuthProvider("GOOGLE");
        user.setProviderId(providerId);
        return userRepository.save(user);
    }
    private String generateUniqueUsername(String email) {
        String baseUsername = email.substring(0, email.indexOf("@"))
                        .replaceAll(
                                "[^a-zA-Z0-9]",
                                ""
                        );

        if (baseUsername.isBlank()) {
            baseUsername = "googleuser";
        }

        if (baseUsername.length() > 40) {
            baseUsername = baseUsername.substring(0, 40);
        }

        String username = baseUsername;
        int counter = 1;
        while (userRepository.existsByUsername(username)) {
            username = baseUsername + counter;
            counter++;
        }
        return username;
    }
}