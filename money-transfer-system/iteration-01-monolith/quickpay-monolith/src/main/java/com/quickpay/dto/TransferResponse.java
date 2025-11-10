package com.quickpay.dto;

import com.quickpay.model.TransactionStatus;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class TransferResponse {
    private String transactionId;
    private TransactionStatus status;
    private String message;
    private boolean success;
}