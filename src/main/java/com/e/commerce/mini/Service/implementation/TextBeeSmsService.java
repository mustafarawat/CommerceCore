package com.e.commerce.mini.Service.implementation;

import com.e.commerce.mini.Service.SmsService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class TextBeeSmsService implements SmsService {

    private static final String TEXTBEE_URL =
            "https://api.textbee.dev/api/v1/gateway/send-sms";

    @Value("${textbee.api-key}")
    private String apiKey;

    private final RestTemplate restTemplate = new RestTemplate();

    @Override
    public void sendOtp(
            String phoneNumber,
            String otp
    ) {
        String message =
                "Your CommerceCore password reset OTP is: "
                        + otp
                        + ". This OTP is valid for 5 minutes. "
                        + "Do not share this OTP with anyone.";

        HttpHeaders headers = new HttpHeaders();

        headers.set("x-api-key", apiKey);
        headers.setContentType(MediaType.APPLICATION_JSON);

        Map<String, Object> body = new HashMap<>();

        body.put(
                "recipients",
                List.of("+91" + phoneNumber)
        );

        body.put(
                "message",
                message
        );

        HttpEntity<Map<String, Object>> request =
                new HttpEntity<>(
                        body,
                        headers
                );

        try {
            ResponseEntity<String> response =
                    restTemplate.postForEntity(
                            TEXTBEE_URL,
                            request,
                            String.class
                    );

            if (!response.getStatusCode().is2xxSuccessful()) {
                throw new RuntimeException(
                        "TextBee SMS request failed."
                );
            }

            System.out.println(
                    "CommerceCore OTP SMS sent successfully: "
                            + response.getStatusCode()
            );

        } catch (Exception exception) {

            System.err.println(
                    "CommerceCore TextBee SMS failed: "
                            + exception.getMessage()
            );

            throw new RuntimeException(
                    "Failed to send OTP SMS."
            );
        }
    }
}