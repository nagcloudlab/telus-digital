package com.quickpay.exception;

public class InsufficientBalanceException extends QuickPayException {
    public InsufficientBalanceException(String message) {
        super(message);
    }
}