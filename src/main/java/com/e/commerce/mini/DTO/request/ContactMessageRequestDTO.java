package com.e.commerce.mini.DTO.request;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ContactMessageRequestDTO {

    private String name;
    private String email;
    private String phone;
    private String subject;
    private String message;
}