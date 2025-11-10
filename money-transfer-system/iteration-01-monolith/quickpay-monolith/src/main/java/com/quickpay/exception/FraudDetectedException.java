package com.quickpay.exception;

public class FraudDetectedException extends QuickPayException {
    public FraudDetectedException(String message) {
        super(message);
    }
}