package com.e.commerce.mini.controller;

import com.e.commerce.mini.DTO.request.ContactMessageRequestDTO;
import com.e.commerce.mini.DTO.response.ContactMessageResponseDTO;
import com.e.commerce.mini.Service.ContactMessageService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/contact")
@RequiredArgsConstructor
public class ContactController {

    private final ContactMessageService contactMessageService;

    @PostMapping
    public ResponseEntity<ContactMessageResponseDTO> createContactMessage(
            @RequestBody ContactMessageRequestDTO request) {

        return ResponseEntity.ok(
                contactMessageService.createContactMessage(request)
        );
    }
}