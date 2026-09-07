package com.e.commerce.mini.Service;

import com.e.commerce.mini.DTO.response.VerifyOtpResponseDTO;

public interface PasswordResetService {

    void requestOtp(
            String identifier,
            String otpType
    );

    VerifyOtpResponseDTO verifyOtp(
            String identifier,
            String otp,
            String otpType
    );

    void resetPassword(
            String resetToken,
            String newPassword,
            String confirmPassword
    );
}