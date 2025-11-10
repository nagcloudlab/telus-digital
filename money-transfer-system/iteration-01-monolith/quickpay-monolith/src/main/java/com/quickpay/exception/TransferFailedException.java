package com.quickpay.exception;

public class TransferFailedException extends QuickPayException {
    public TransferFailedException(String message) {
        super(message);
    }

    public TransferFailedException(String message, Throwable cause) {
        super(message, cause);
    }
}