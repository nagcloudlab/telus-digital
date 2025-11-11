package com.quickpay.transfer.dto;

import com.quickpay.transfer.entity.TransferStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TransferResponse {

    private String transferId;
    private TransferStatus status;
    private Long fromAccountId;
    private Long toAccountId;
    private BigDecimal amount;
    private BigDecimal fee;
    private BigDecimal totalAmount;
    private BigDecimal fromAccountBalanceBefore;
    private BigDecimal fromAccountBalanceAfter;
    private BigDecimal toAccountBalanceBefore;
    private BigDecimal toAccountBalanceAfter;
    private LocalDateTime timestamp;
    private String message;
}