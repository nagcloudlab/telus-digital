package com.quickpay.transfer.util;

import com.quickpay.transfer.dto.TransferRequest;

import java.math.BigDecimal;

public class TransferRequestTestBuilder {

    private Long fromAccountId = 1L;
    private Long toAccountId = 2L;
    private BigDecimal amount = new BigDecimal("1000.00");
    private String description = "Test transfer";

    public static TransferRequestTestBuilder builder() {
        return new TransferRequestTestBuilder();
    }

    public TransferRequestTestBuilder fromAccountId(Long fromAccountId) {
        this.fromAccountId = fromAccountId;
        return this;
    }

    public TransferRequestTestBuilder toAccountId(Long toAccountId) {
        this.toAccountId = toAccountId;
        return this;
    }

    public TransferRequestTestBuilder amount(String amount) {
        this.amount = new BigDecimal(amount);
        return this;
    }

    public TransferRequestTestBuilder amount(BigDecimal amount) {
        this.amount = amount;
        return this;
    }

    public TransferRequestTestBuilder description(String description) {
        this.description = description;
        return this;
    }

    public TransferRequest build() {
        TransferRequest request = new TransferRequest();
        request.setFromAccountId(fromAccountId);
        request.setToAccountId(toAccountId);
        request.setAmount(amount);
        request.setDescription(description);
        return request;
    }
}