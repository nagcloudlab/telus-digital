package com.quickpay.exception;

public class QuickPayException extends RuntimeException {
    public QuickPayException(String message) {
        super(message);
    }

    public QuickPayException(String message, Throwable cause) {
        super(message, cause);
    }
}