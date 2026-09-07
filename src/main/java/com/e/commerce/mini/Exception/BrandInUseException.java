package com.e.commerce.mini.Exception;

public class BrandInUseException extends RuntimeException {
    public BrandInUseException(String message) {
        super(message);
    }
}
