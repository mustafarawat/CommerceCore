package com.e.commerce.mini.DTO.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class VerifyOtpRequestDTO {

    @NotBlank(message = "Email or phone number is required")
    private String identifier;

    @NotBlank(message = "OTP is required")
    @Size(
            min = 6,
            max = 6,
            message = "OTP must be exactly 6 digits"
    )
    @Pattern(
            regexp = "\\d{6}",
            message = "OTP must contain only 6 digits"
    )
    private String otp;

    @NotBlank(message = "OTP type is required")
    @Pattern(
            regexp = "EMAIL|PHONE",
            message = "OTP type must be EMAIL or PHONE"
    )
    private String otpType;
}