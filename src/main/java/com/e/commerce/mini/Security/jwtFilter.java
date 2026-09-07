package com.e.commerce.mini.Security;

import com.e.commerce.mini.Util.Jwtutil;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
@RequiredArgsConstructor
public class jwtFilter extends OncePerRequestFilter {

    private final Jwtutil jwtutil;

    private final UserDetailsService userDetailsService;

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {

        String authHeader =
                request.getHeader("Authorization");

        if (
                authHeader == null
                        || !authHeader.startsWith("Bearer ")
        ) {

            filterChain.doFilter(
                    request,
                    response
            );

            return;
        }

        String token =
                authHeader.substring(7).trim();

        if (token.isBlank()) {

            sendUnauthorizedResponse(
                    response,
                    "JWT token is missing"
            );

            return;
        }

        try {

            String username =
                    jwtutil.extractUsername(token);

            if (username == null) {

                sendUnauthorizedResponse(
                        response,
                        "Invalid JWT token"
                );

                return;
            }

            if (!jwtutil.validateToken(
                    token,
                    username
            )) {

                sendUnauthorizedResponse(
                        response,
                        "Invalid or expired JWT token"
                );

                return;
            }

            UserDetails userDetails =
                    userDetailsService
                            .loadUserByUsername(username);

            UsernamePasswordAuthenticationToken authentication =
                    new UsernamePasswordAuthenticationToken(
                            userDetails,
                            null,
                            userDetails.getAuthorities()
                    );

            SecurityContextHolder
                    .getContext()
                    .setAuthentication(authentication);

            /*
             * TEMPORARY DEBUG
             *
             * We will remove this after testing.
             */
            System.out.println(
                    "----------------------------------------"
            );

            System.out.println(
                    "JWT USER: "
                            + authentication.getName()
            );

            System.out.println(
                    "JWT AUTHORITIES: "
                            + authentication.getAuthorities()
            );

            System.out.println(
                    "REQUEST URI: "
                            + request.getRequestURI()
            );

            System.out.println(
                    "AUTHENTICATED: "
                            + authentication.isAuthenticated()
            );

            System.out.println(
                    "----------------------------------------"
            );

        } catch (Exception exception) {

            SecurityContextHolder
                    .clearContext();

            sendUnauthorizedResponse(
                    response,
                    "Invalid or expired JWT token"
            );

            return;
        }

        /*
         * IMPORTANT:
         *
         * This is outside the try/catch.
         *
         * Authorization exceptions must be handled by
         * Spring Security's AccessDeniedHandler.
         */
        filterChain.doFilter(
                request,
                response
        );
    }

    private void sendUnauthorizedResponse(
            HttpServletResponse response,
            String message
    ) throws IOException {

        response.setStatus(
                HttpServletResponse.SC_UNAUTHORIZED
        );

        response.setContentType(
                "application/json"
        );

        response.getWriter().write(
                """
                {
                    "status": false,
                    "message": "%s"
                }
                """.formatted(message)
        );
    }
}