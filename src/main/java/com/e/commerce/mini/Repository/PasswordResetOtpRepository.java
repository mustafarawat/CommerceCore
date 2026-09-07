package com.e.commerce.mini.Repository;

import com.e.commerce.mini.models.PasswordResetOtp;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface PasswordResetOtpRepository
        extends JpaRepository<PasswordResetOtp, Long> {

    Optional<PasswordResetOtp> findTopByEmailOrderByCreatedAtDesc(
            String email
    );

    Optional<PasswordResetOtp> findTopByPhoneNumberOrderByCreatedAtDesc(
            String phoneNumber
    );

    Optional<PasswordResetOtp> findByResetTokenHash(
            String resetTokenHash
    );
}