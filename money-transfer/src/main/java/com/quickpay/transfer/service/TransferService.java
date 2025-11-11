package com.quickpay.transfer.service;

import com.quickpay.transfer.dto.TransferRequest;
import com.quickpay.transfer.dto.TransferResponse;
import com.quickpay.transfer.entity.*;
import com.quickpay.transfer.repository.TransactionHistoryRepository;
import com.quickpay.transfer.repository.TransferRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Service
@RequiredArgsConstructor
@Slf4j
public class TransferService {

    private final AccountService accountService;
    private final ValidationService validationService;
    private final FeeCalculationService feeCalculationService;
    private final FraudDetectionService fraudDetectionService;
    private final NotificationService notificationService;
    private final TransferRepository transferRepository;
    private final TransactionHistoryRepository transactionHistoryRepository;

    @Transactional
    public TransferResponse transfer(TransferRequest request) {
        log.info("Transfer initiated: {} -> {}, Amount: {}",
                request.getFromAccountId(), request.getToAccountId(), request.getAmount());

        // 1. Load accounts
        Account fromAccount = accountService.findById(request.getFromAccountId());
        Account toAccount = accountService.findById(request.getToAccountId());

        // Store initial balances for response
        BigDecimal fromBalanceBefore = fromAccount.getBalance();
        BigDecimal toBalanceBefore = toAccount.getBalance();

        // 2. Calculate fee
        BigDecimal fee = feeCalculationService.calculateFee(request.getAmount());
        BigDecimal totalAmount = request.getAmount().add(fee);

        // 3. Validate
        validationService.validate(request, fromAccount, toAccount, totalAmount);

        // 4. Fraud check
        fraudDetectionService.checkFraud(request);

        // 5. Execute transfer
        fromAccount.debit(totalAmount);
        toAccount.credit(request.getAmount());

        // 6. Update accounts
        accountService.update(fromAccount);
        accountService.update(toAccount);

        // 7. Create transfer record
        Transfer transfer = createTransferRecord(request, fee, totalAmount);
        transfer.setStatus(TransferStatus.COMPLETED);
        transfer.setCompletedAt(LocalDateTime.now());
        transferRepository.save(transfer);

        // 8. Create transaction history
        createTransactionHistory(transfer, fromAccount, toAccount, fromBalanceBefore, toBalanceBefore);

        // 9. Send notification
        notificationService.sendNotification(transfer);

        log.info("Transfer completed successfully: {}", transfer.getTransferReference());

        // 10. Build and return response
        return buildResponse(transfer, fromBalanceBefore, toBalanceBefore, fromAccount, toAccount);
    }

    private Transfer createTransferRecord(TransferRequest request, BigDecimal fee, BigDecimal totalAmount) {
        Transfer transfer = new Transfer();
        transfer.setTransferReference(generateTransferReference());
        transfer.setFromAccountId(request.getFromAccountId());
        transfer.setToAccountId(request.getToAccountId());
        transfer.setAmount(request.getAmount());
        transfer.setFee(fee);
        transfer.setTotalAmount(totalAmount);
        transfer.setCurrency("USD");
        transfer.setDescription(request.getDescription());
        transfer.setStatus(TransferStatus.PROCESSING);
        return transfer;
    }

    private void createTransactionHistory(Transfer transfer, Account fromAccount, Account toAccount,
            BigDecimal fromBalanceBefore, BigDecimal toBalanceBefore) {
        // Debit entry
        TransactionHistory debitEntry = new TransactionHistory();
        debitEntry.setTransferId(transfer.getId());
        debitEntry.setAccountId(fromAccount.getId());
        debitEntry.setTransactionType(TransactionType.DEBIT);
        debitEntry.setAmount(transfer.getTotalAmount());
        debitEntry.setBalanceBefore(fromBalanceBefore);
        debitEntry.setBalanceAfter(fromAccount.getBalance());
        transactionHistoryRepository.save(debitEntry);

        // Credit entry
        TransactionHistory creditEntry = new TransactionHistory();
        creditEntry.setTransferId(transfer.getId());
        creditEntry.setAccountId(toAccount.getId());
        creditEntry.setTransactionType(TransactionType.CREDIT);
        creditEntry.setAmount(transfer.getAmount());
        creditEntry.setBalanceBefore(toBalanceBefore);
        creditEntry.setBalanceAfter(toAccount.getBalance());
        transactionHistoryRepository.save(creditEntry);
    }

    private String generateTransferReference() {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMdd-HHmmss");
        String timestamp = LocalDateTime.now().format(formatter);
        return "TXN-" + timestamp;
    }

    private TransferResponse buildResponse(Transfer transfer, BigDecimal fromBalanceBefore,
            BigDecimal toBalanceBefore, Account fromAccount, Account toAccount) {
        return TransferResponse.builder()
                .transferId(transfer.getTransferReference())
                .status(transfer.getStatus())
                .fromAccountId(transfer.getFromAccountId())
                .toAccountId(transfer.getToAccountId())
                .amount(transfer.getAmount())
                .fee(transfer.getFee())
                .totalAmount(transfer.getTotalAmount())
                .fromAccountBalanceBefore(fromBalanceBefore)
                .fromAccountBalanceAfter(fromAccount.getBalance())
                .toAccountBalanceBefore(toBalanceBefore)
                .toAccountBalanceAfter(toAccount.getBalance())
                .timestamp(transfer.getCompletedAt())
                .message("Transfer completed successfully")
                .build();
    }
}
