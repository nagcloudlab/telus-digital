package com.quickpay.service;

import com.quickpay.model.Account;
import com.quickpay.repository.TransactionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.LocalTime;

@Service
@RequiredArgsConstructor
@Slf4j
public class FraudDetectionService {

    private final TransactionRepository transactionRepository;

    public double calculateRiskScore(Account fromAccount, Account toAccount, BigDecimal amount) {
        double riskScore = 0.0;

        // Rule 1: High amount transfer (> 100,000)
        if (amount.compareTo(new BigDecimal("100000")) > 0) {
            riskScore += 0.3;
            log.debug("High amount detected: {}", amount);
        }

        // Rule 2: Unusual time (midnight to 6 AM)
        LocalTime now = LocalTime.now();
        if (now.isAfter(LocalTime.MIDNIGHT) && now.isBefore(LocalTime.of(6, 0))) {
            riskScore += 0.2;
            log.debug("Unusual time detected: {}", now);
        }

        // Rule 3: Multiple transactions in short time (last 30 minutes)
        LocalDateTime last30Min = LocalDateTime.now().minusMinutes(30);
        long recentTransactions = transactionRepository
                .countByFromAccountAndCreatedAtAfter(fromAccount, last30Min);

        if (recentTransactions > 5) {
            riskScore += 0.4;
            log.debug("Multiple transactions detected: {}", recentTransactions);
        }

        // Rule 4: New account (created within 7 days)
        if (fromAccount.getCreatedAt().isAfter(LocalDateTime.now().minusDays(7))) {
            riskScore += 0.2;
            log.debug("New account detected");
        }

        // Cap at 1.0
        riskScore = Math.min(riskScore, 1.0);

        log.info("Fraud risk score calculated: {} for amount: {}", riskScore, amount);
        return riskScore;
    }
}