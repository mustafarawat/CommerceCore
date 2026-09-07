package com.e.commerce.mini.Exception;

public class productNotFoundException extends RuntimeException {
    public productNotFoundException(String message) {
        super(message);
    }
}
