package com.quickpay.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class AccountBalanceResponse {
    private String accountNumber;
    private BigDecimal balance;
    private String currency;
    private String status;
}