package com.e.commerce.mini.DTO.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ForgotPasswordRequestDTO {

    @NotBlank(message = "Email or phone number is required")
    private String identifier;

    @NotBlank(message = "OTP type is required")
    @Pattern(
            regexp = "EMAIL|PHONE",
            message = "OTP type must be EMAIL or PHONE"
    )
    private String otpType;
}