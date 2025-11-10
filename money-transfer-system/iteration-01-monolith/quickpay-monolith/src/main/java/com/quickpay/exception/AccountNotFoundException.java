package com.quickpay.exception;

public class AccountNotFoundException extends QuickPayException {
    public AccountNotFoundException(String message) {
        super(message);
    }
}