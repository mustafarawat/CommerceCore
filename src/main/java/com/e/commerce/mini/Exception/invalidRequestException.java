package com.e.commerce.mini.Exception;

public class invalidRequestException extends RuntimeException {
    public invalidRequestException(String message) {
        super(message);
    }
}
