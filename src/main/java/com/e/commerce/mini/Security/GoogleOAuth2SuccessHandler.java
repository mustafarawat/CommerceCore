package com.e.commerce.mini.Security;

import com.e.commerce.mini.Enums.Role;
import com.e.commerce.mini.Mapper.UserMapper;
import com.e.commerce.mini.Repository.UserRepository;
import com.e.commerce.mini.Service.RefreshTokenService;
import com.e.commerce.mini.Util.Jwtutil;
import com.e.commerce.mini.models.RefreshToken;
import com.e.commerce.mini.models.User;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class GoogleOAuth2SuccessHandler implements AuthenticationSuccessHandler {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final Jwtutil jwtutil;
    private final RefreshTokenService refreshTokenService;

    @Override
    public void onAuthenticationSuccess(
            HttpServletRequest request,
            HttpServletResponse response,
            Authentication authentication
    ) throws IOException, ServletException {

        OAuth2User googleUser =
                (OAuth2User) authentication.getPrincipal();

        String providerId =
                googleUser.getAttribute("sub");

        String email =
                googleUser.getAttribute("email");

        String fullName =
                googleUser.getAttribute("name");

        Boolean emailVerified =
                googleUser.getAttribute("email_verified");

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

        String accessToken =
                jwtutil.generateToken(user);

        RefreshToken refreshToken =
                refreshTokenService.createRefreshToken(user);

        /*
         * Google OAuth authentication is complete.
         *
         * Instead of returning JSON directly to the browser,
         * redirect the user back to the React application.
         *
         * Tokens are placed inside the URL fragment (#).
         * The fragment is not sent to the backend/server.
         *
         * React will read these tokens from the OAuth callback
         * page and save them using the existing auth.js logic.
         */

        String frontendCallbackUrl =
                "http://localhost:5173/oauth/callback";

        String redirectUrl =
                frontendCallbackUrl
                        + "#accessToken="
                        + URLEncoder.encode(
                        accessToken,
                        StandardCharsets.UTF_8
                )
                        + "&refreshToken="
                        + URLEncoder.encode(
                        refreshToken.getToken(),
                        StandardCharsets.UTF_8
                )
                        + "&tokenType="
                        + URLEncoder.encode(
                        "Bearer",
                        StandardCharsets.UTF_8
                );

        response.sendRedirect(redirectUrl);
    }

    private User createOrLinkGoogleUser(
            String providerId,
            String email,
            String fullName
    ) {

        User user =
                userRepository
                        .findByEmail(email)
                        .orElse(null);

        if (user != null) {

            user.setAuthProvider("GOOGLE");
            user.setProviderId(providerId);

            if (fullName != null && !fullName.isBlank()) {
                user.setFullName(fullName);
            }

            return userRepository.save(user);
        }

        user = new User();

        user.setUsername(
                generateUniqueUsername(email)
        );

        user.setPassword(
                passwordEncoder.encode(
                        UUID.randomUUID().toString()
                )
        );

        user.setEmail(email);
        user.setFullName(fullName);
        user.setRole(Role.CUSTOMER);
        user.setEnabled(true);
        user.setAuthProvider("GOOGLE");
        user.setProviderId(providerId);

        return userRepository.save(user);
    }

    private String generateUniqueUsername(
            String email
    ) {

        String baseUsername =
                email.substring(
                        0,
                        email.indexOf("@")
                ).replaceAll(
                        "[^a-zA-Z0-9]",
                        ""
                );

        if (baseUsername.isBlank()) {
            baseUsername = "googleuser";
        }

        if (baseUsername.length() > 40) {
            baseUsername =
                    baseUsername.substring(0, 40);
        }

        String username = baseUsername;
        int counter = 1;

        while (
                userRepository.existsByUsername(username)
        ) {
            username =
                    baseUsername + counter;

            counter++;
        }

        return username;
    }
}