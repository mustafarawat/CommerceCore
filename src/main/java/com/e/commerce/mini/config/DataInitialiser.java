package com.e.commerce.mini.config;

import com.e.commerce.mini.Enums.Role;
import com.e.commerce.mini.Repository.UserRepository;
import com.e.commerce.mini.models.User;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration
public class DataInitialiser {

    @Bean
    public CommandLineRunner init(
            UserRepository repo,
            PasswordEncoder encoder
    ) {

        return args -> {

            if (repo.findByUsername("admin").isEmpty()) {

                User admin = new User();

                admin.setUsername("admin");
                admin.setPassword(
                        encoder.encode("admin123")
                );
                admin.setEmail("admin@aureon.com");
                admin.setFullName("AUREON Administrator");
                admin.setRole(Role.ADMIN);
                admin.setEnabled(true);

                repo.save(admin);

                System.out.println(
                        "Default admin created successfully."
                );
            }
        };
    }
}