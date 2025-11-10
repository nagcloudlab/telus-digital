package com.quickpay.service;

import com.quickpay.model.AuditLog;
import com.quickpay.model.Transaction;
import com.quickpay.repository.AuditLogRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuditService {

    private final AuditLogRepository auditLogRepository;

    @Async
    public void logTransfer(Transaction transaction) {
        try {
            AuditLog auditLog = new AuditLog();
            auditLog.setAction("MONEY_TRANSFER");
            auditLog.setEntityType("Transaction");
            auditLog.setEntityId(transaction.getId());
            auditLog.setUserId(transaction.getFromAccount().getUser().getId());
            auditLog.setDetails(String.format(
                    "Transfer of ₹%s from %s to %s",
                    transaction.getAmount(),
                    transaction.getFromAccount().getAccountNumber(),
                    transaction.getToAccount().getAccountNumber()));

            auditLogRepository.save(auditLog);
            log.debug("Audit log created for transaction: {}", transaction.getTransactionId());

        } catch (Exception e) {
            log.error("Failed to create audit log for transaction: {}",
                    transaction.getTransactionId(), e);
        }
    }
}