package com.e.commerce.mini.Service;

import com.e.commerce.mini.DTO.request.ContactMessageRequestDTO;
import com.e.commerce.mini.DTO.response.ContactMessageResponseDTO;

import java.util.List;

public interface ContactMessageService {

    ContactMessageResponseDTO createContactMessage(ContactMessageRequestDTO request);

    List<ContactMessageResponseDTO> getAllContactMessages();
}