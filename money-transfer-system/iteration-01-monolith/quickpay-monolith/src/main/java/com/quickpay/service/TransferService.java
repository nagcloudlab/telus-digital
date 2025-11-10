package com.quickpay.service;

import com.quickpay.dto.TransferRequest;
import com.quickpay.dto.TransferResponse;
import com.quickpay.dto.TransactionHistoryResponse;
import com.quickpay.exception.*;
import com.quickpay.model.*;
import com.quickpay.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class TransferService {

    private final AccountRepository accountRepository;
    private final TransactionRepository transactionRepository;
    private final FraudDetectionService fraudDetectionService;
    private final NotificationService notificationService;
    private final AuditService auditService;

    @Transactional
    public TransferResponse transferMoney(TransferRequest request) {
        log.info("Processing transfer: {} to {}, amount: {}",
                request.getFromAccountNumber(),
                request.getToAccountNumber(),
                request.getAmount());

        // 1. Validate accounts
        Account fromAccount = accountRepository
                .findByAccountNumber(request.getFromAccountNumber())
                .orElseThrow(() -> new AccountNotFoundException(
                        "Source account not found: " + request.getFromAccountNumber()));

        Account toAccount = accountRepository
                .findByAccountNumber(request.getToAccountNumber())
                .orElseThrow(() -> new AccountNotFoundException(
                        "Destination account not found: " + request.getToAccountNumber()));

        // 2. Validate same account transfer
        if (fromAccount.getId().equals(toAccount.getId())) {
            throw new TransferFailedException("Cannot transfer to the same account");
        }

        // 3. Validate account status
        if (fromAccount.getStatus() != AccountStatus.ACTIVE) {
            throw new TransferFailedException("Source account is not active");
        }

        if (toAccount.getStatus() != AccountStatus.ACTIVE) {
            throw new TransferFailedException("Destination account is not active");
        }

        // 4. Validate balance
        if (fromAccount.getBalance().compareTo(request.getAmount()) < 0) {
            log.warn("Insufficient balance. Available: {}, Requested: {}",
                    fromAccount.getBalance(), request.getAmount());
            throw new InsufficientBalanceException("Insufficient balance");
        }

        // 5. Fraud detection
        double fraudScore = fraudDetectionService.calculateRiskScore(
                fromAccount, toAccount, request.getAmount());

        if (fraudScore > 0.8) {
            log.warn("High fraud risk detected. Score: {}", fraudScore);
            throw new FraudDetectedException(
                    "Transaction blocked due to fraud risk. Score: " + fraudScore);
        }

        // 6. Create transaction
        Transaction transaction = new Transaction();
        transaction.setTransactionId(UUID.randomUUID().toString());
        transaction.setFromAccount(fromAccount);
        transaction.setToAccount(toAccount);
        transaction.setAmount(request.getAmount());
        transaction.setCurrency("INR");
        transaction.setDescription(request.getDescription());
        transaction.setFraudScore(fraudScore);
        transaction.setStatus(TransactionStatus.PENDING);

        try {
            // 7. Update balances
            fromAccount.setBalance(fromAccount.getBalance().subtract(request.getAmount()));
            fromAccount.setUpdatedAt(LocalDateTime.now());

            toAccount.setBalance(toAccount.getBalance().add(request.getAmount()));
            toAccount.setUpdatedAt(LocalDateTime.now());

            accountRepository.save(fromAccount);
            accountRepository.save(toAccount);

            // 8. Complete transaction
            transaction.setStatus(TransactionStatus.COMPLETED);
            transaction.setCompletedAt(LocalDateTime.now());
            transactionRepository.save(transaction);

            log.info("Transfer completed successfully. Transaction ID: {}",
                    transaction.getTransactionId());

            // 9. Send notifications (async)
            notificationService.sendTransferNotification(transaction);

            // 10. Audit logging (async)
            auditService.logTransfer(transaction);

            return new TransferResponse(
                    transaction.getTransactionId(),
                    TransactionStatus.COMPLETED,
                    "Transfer completed successfully",
                    true);

        } catch (Exception e) {
            transaction.setStatus(TransactionStatus.FAILED);
            transaction.setRemarks(e.getMessage());
            transactionRepository.save(transaction);

            log.error("Transfer failed", e);
            throw new TransferFailedException("Transfer failed: " + e.getMessage(), e);
        }
    }

    @Transactional(readOnly = true)
    public List<TransactionHistoryResponse> getTransactionHistory(String accountNumber) {
        log.info("Fetching transaction history for account: {}", accountNumber);

        Account account = accountRepository.findByAccountNumber(accountNumber)
                .orElseThrow(() -> new AccountNotFoundException(
                        "Account not found: " + accountNumber));

        List<Transaction> transactions = transactionRepository
                .findByFromAccountOrToAccountOrderByCreatedAtDesc(account, account);

        return transactions.stream()
                .map(t -> new TransactionHistoryResponse(
                        t.getTransactionId(),
                        t.getFromAccount().getAccountNumber(),
                        t.getToAccount().getAccountNumber(),
                        t.getAmount(),
                        t.getCurrency(),
                        t.getStatus(),
                        t.getDescription(),
                        t.getCreatedAt(),
                        t.getFromAccount().getId().equals(account.getId()) ? "DEBIT" : "CREDIT"))
                .collect(Collectors.toList());
    }
}