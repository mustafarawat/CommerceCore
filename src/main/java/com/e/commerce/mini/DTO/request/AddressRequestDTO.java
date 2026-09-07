package com.e.commerce.mini.DTO.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AddressRequestDTO {

    @NotBlank(message = "Full name is required")
    @Size(
            min = 2,
            max = 100,
            message = "Full name must be between 2 and 100 characters")
    private String fullName;

    @NotBlank(message = "Contact number is required")
    @Pattern(
            regexp = "^[6-9]\\d{9}$",
            message = "Contact number must be a valid 10-digit Indian mobile number")
    private String contactNo;

    @NotBlank(message = "Address line is required")
    @Size(
            min = 5,
            max = 255,
            message = "Address line must be between 5 and 255 characters")
    private String addressLine;

    @NotBlank(message = "City is required")
    @Size(
            min = 2,
            max = 100,
            message = "City must be between 2 and 100 characters")
    private String city;

    @NotBlank(message = "State is required")
    @Size(
            min = 2,
            max = 100,
            message = "State must be between 2 and 100 characters")
    private String state;

    @NotBlank(message = "Pincode is required")
    @Pattern(regexp = "^[1-9][0-9]{5}$", message = "Pincode must be a valid 6-digit Indian pincode")
    private String pincode;

    @Size(max = 100, message = "Country cannot exceed 100 characters")
    private String country;

    private Boolean defaultAddress;
}