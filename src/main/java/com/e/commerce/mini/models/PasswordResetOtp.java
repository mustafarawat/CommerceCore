package com.e.commerce.mini.models;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(
        name = "password_reset_otps",
        indexes = {
                @Index(name = "idx_password_reset_email", columnList = "email"),
                @Index(name = "idx_password_reset_token", columnList = "reset_token_hash")
        }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class PasswordResetOtp {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(length = 150)
    private String email;

    @Column(length = 20)
    private String phoneNumber;

    @Column
    private Long userId;

    @Column(nullable = false, length = 10)
    private String otpType;

    @Column(nullable = false, length = 255)
    private String otpHash;

    @Column(nullable = false)
    private LocalDateTime expiresAt;

    @Column(nullable = false)
    private int attempts = 0;

    private LocalDateTime verifiedAt;

    @Column(name = "reset_token_hash", length = 255)
    private String resetTokenHash;

    private LocalDateTime resetTokenExpiresAt;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }
}