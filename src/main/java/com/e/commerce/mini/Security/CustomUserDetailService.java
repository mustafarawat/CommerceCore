package com.e.commerce.mini.Security;

import com.e.commerce.mini.Repository.UserRepository;
import com.e.commerce.mini.models.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
public class CustomUserDetailService implements UserDetailsService {

    private final UserRepository repo;

    public CustomUserDetailService(
            UserRepository repo
    ) {
        this.repo = repo;
    }

    @Override
    public UserDetails loadUserByUsername(
            String username
    ) throws UsernameNotFoundException {

        User user =
                repo.findByUsername(username)
                        .orElseThrow(
                                () -> new UsernameNotFoundException(
                                        "Username not found: "
                                                + username
                                )
                        );

        return org.springframework.security.core.userdetails.User
                .builder()
                .username(user.getUsername())
                .password(user.getPassword())
                .roles(user.getRole().name())
                .build();
    }
}