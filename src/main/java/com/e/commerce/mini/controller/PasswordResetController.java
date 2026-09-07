package com.e.commerce.mini.controller;

import com.e.commerce.mini.DTO.request.ForgotPasswordRequestDTO;
import com.e.commerce.mini.DTO.request.ResetPasswordRequestDTO;
import com.e.commerce.mini.DTO.request.VerifyOtpRequestDTO;
import com.e.commerce.mini.DTO.response.VerifyOtpResponseDTO;
import com.e.commerce.mini.Service.PasswordResetService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/password-reset")
@RequiredArgsConstructor
public class PasswordResetController {

    private final PasswordResetService passwordResetService;

    @PostMapping("/request")
    public ResponseEntity<Map<String, String>> requestOtp(
            @Valid @RequestBody ForgotPasswordRequestDTO request
    ) {
        passwordResetService.requestOtp(
                request.getIdentifier(),
                request.getOtpType()
        );

        return ResponseEntity.ok(
                Map.of(
                        "message",
                        "If an account exists for this recovery method, an OTP has been sent."
                )
        );
    }

    @PostMapping("/verify")
    public ResponseEntity<VerifyOtpResponseDTO> verifyOtp(
            @Valid @RequestBody VerifyOtpRequestDTO request
    ) {
        VerifyOtpResponseDTO response =
                passwordResetService.verifyOtp(
                        request.getIdentifier(),
                        request.getOtp(),
                        request.getOtpType()
                );

        return ResponseEntity.ok(response);
    }

    @PostMapping("/reset")
    public ResponseEntity<Map<String, String>> resetPassword(
            @Valid @RequestBody ResetPasswordRequestDTO request
    ) {
        passwordResetService.resetPassword(
                request.getResetToken(),
                request.getNewPassword(),
                request.getConfirmPassword()
        );

        return ResponseEntity.ok(
                Map.of(
                        "message",
                        "Password has been reset successfully."
                )
        );
    }
}