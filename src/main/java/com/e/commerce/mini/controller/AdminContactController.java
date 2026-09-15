package com.e.commerce.mini.controller;

import com.e.commerce.mini.DTO.response.ContactMessageResponseDTO;
import com.e.commerce.mini.Service.ContactMessageService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin/contact")
@RequiredArgsConstructor
public class AdminContactController {

    private final ContactMessageService contactMessageService;

    @GetMapping
    public ResponseEntity<List<ContactMessageResponseDTO>> getAllContactMessages() {

        return ResponseEntity.ok(
                contactMessageService.getAllContactMessages()
        );
    }
}