package com.e.commerce.mini.DTO.response;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
public class AddressResponseDTO {

    private Long id;

    private String fullName;

    private String contactNo;

    private String addressLine;

    private String city;

    private String state;

    private String pincode;

    private String country;

    private Boolean defaultAddress;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;
}