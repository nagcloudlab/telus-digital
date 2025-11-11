package com.quickpay.transfer.service;

import com.quickpay.transfer.dto.TransferRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

@Service
@Slf4j
public class FraudDetectionService {

    private static final BigDecimal HIGH_VALUE_THRESHOLD = new BigDecimal("50000.00");

    public void checkFraud(TransferRequest request) {
        log.info("Fraud check initiated for transfer: {} -> {}, Amount: {}",
                request.getFromAccountId(), request.getToAccountId(), request.getAmount());

        if (request.getAmount().compareTo(HIGH_VALUE_THRESHOLD) > 0) {
            log.warn("HIGH VALUE TRANSFER ALERT: Amount {} exceeds threshold {}",
                    request.getAmount(), HIGH_VALUE_THRESHOLD);
        }

        // Mock: Always approve for now
        log.info("Fraud check: APPROVED");
    }
}