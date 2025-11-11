package com.quickpay.transfer.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;

@Service
@Slf4j
public class FeeCalculationService {

    private static final BigDecimal TIER1_LIMIT = new BigDecimal("1000");
    private static final BigDecimal TIER2_LIMIT = new BigDecimal("10000");
    private static final BigDecimal TIER3_LIMIT = new BigDecimal("50000");

    private static final BigDecimal TIER1_FEE_RATE = BigDecimal.ZERO;
    private static final BigDecimal TIER2_FEE_RATE = new BigDecimal("0.01"); // 1%
    private static final BigDecimal TIER3_FEE_RATE = new BigDecimal("0.005"); // 0.5%
    private static final BigDecimal TIER4_FEE_RATE = new BigDecimal("0.0025"); // 0.25%

    private static final BigDecimal MAX_FEE = new BigDecimal("500.00");

    public BigDecimal calculateFee(BigDecimal amount) {
        log.debug("Calculating fee for amount: {}", amount);

        BigDecimal fee;

        if (amount.compareTo(TIER1_LIMIT) <= 0) {
            fee = TIER1_FEE_RATE;
        } else if (amount.compareTo(TIER2_LIMIT) <= 0) {
            fee = amount.multiply(TIER2_FEE_RATE);
        } else if (amount.compareTo(TIER3_LIMIT) <= 0) {
            fee = amount.multiply(TIER3_FEE_RATE);
        } else {
            fee = amount.multiply(TIER4_FEE_RATE);
        }

        // Apply maximum fee cap
        if (fee.compareTo(MAX_FEE) > 0) {
            fee = MAX_FEE;
        }

        // Round to 2 decimal places
        fee = fee.setScale(2, RoundingMode.UP);

        log.debug("Calculated fee: {} for amount: {}", fee, amount);
        return fee;
    }
}