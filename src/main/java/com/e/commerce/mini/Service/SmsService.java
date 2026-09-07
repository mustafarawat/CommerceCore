package com.e.commerce.mini.Service;

public interface SmsService {

    void sendOtp(
            String phoneNumber,
            String otp
    );
}