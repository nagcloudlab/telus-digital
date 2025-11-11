package com.quickpay.transfer.service;

import com.quickpay.transfer.dto.TransferRequest;
import com.quickpay.transfer.entity.Account;
import com.quickpay.transfer.entity.AccountStatus;
import com.quickpay.transfer.exception.InsufficientBalanceException;
import com.quickpay.transfer.exception.ValidationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

@Service
@Slf4j
public class ValidationService {

    private static final BigDecimal MAX_TRANSFER_AMOUNT = new BigDecimal("100000.00");

    public void validate(TransferRequest request, Account fromAccount, Account toAccount, BigDecimal totalAmount) {
        log.debug("Validating transfer request");

        // Validate accounts exist
        if (fromAccount == null) {
            throw new ValidationException("Source account not found");
        }
        if (toAccount == null) {
            throw new ValidationException("Destination account not found");
        }

        // Validate not same account
        if (fromAccount.getId().equals(toAccount.getId())) {
            throw new ValidationException("Cannot transfer to same account");
        }

        // Validate account status
        if (fromAccount.getStatus() != AccountStatus.ACTIVE) {
            throw new ValidationException("Source account is not active: " + fromAccount.getStatus());
        }
        if (toAccount.getStatus() != AccountStatus.ACTIVE) {
            throw new ValidationException("Destination account is not active: " + toAccount.getStatus());
        }

        // Validate amount
        if (request.getAmount().compareTo(BigDecimal.ZERO) <= 0) {
            throw new ValidationException("Transfer amount must be greater than zero");
        }

        if (request.getAmount().compareTo(MAX_TRANSFER_AMOUNT) > 0) {
            throw new ValidationException("Transfer amount exceeds maximum limit: " + MAX_TRANSFER_AMOUNT);
        }

        // Validate sufficient balance
        if (!fromAccount.hasSufficientBalance(totalAmount)) {
            throw new InsufficientBalanceException(
                    String.format("Insufficient balance in account %s. Required: %s, Available: %s",
                            fromAccount.getAccountNumber(), totalAmount, fromAccount.getBalance()));
        }

        // Validate minimum balance after transfer
        BigDecimal balanceAfterTransfer = fromAccount.getBalance().subtract(totalAmount);
        if (balanceAfterTransfer.compareTo(fromAccount.getMinimumBalance()) < 0) {
            throw new ValidationException(
                    String.format("Transfer would violate minimum balance requirement. Minimum: %s, After transfer: %s",
                            fromAccount.getMinimumBalance(), balanceAfterTransfer));
        }

        // Validate daily limit
        BigDecimal dailyTransferredAfter = fromAccount.getDailyTransferred().add(request.getAmount());
        if (dailyTransferredAfter.compareTo(fromAccount.getDailyLimit()) > 0) {
            throw new ValidationException(
                    String.format("Transfer would exceed daily limit. Limit: %s, Used: %s, Requested: %s",
                            fromAccount.getDailyLimit(), fromAccount.getDailyTransferred(), request.getAmount()));
        }

        log.debug("Validation passed");
    }
}