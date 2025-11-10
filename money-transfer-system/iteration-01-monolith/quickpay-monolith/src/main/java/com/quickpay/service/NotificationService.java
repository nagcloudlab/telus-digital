package com.quickpay.service;

import com.quickpay.model.Transaction;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class NotificationService {

    @Async
    public void sendTransferNotification(Transaction transaction) {
        try {
            // Simulate sending email to sender
            String senderEmail = transaction.getFromAccount().getUser().getEmail();
            String senderMessage = String.format(
                    "You have sent ₹%s to account %s. Transaction ID: %s",
                    transaction.getAmount(),
                    transaction.getToAccount().getAccountNumber(),
                    transaction.getTransactionId());
            log.info("Email sent to {}: {}", senderEmail, senderMessage);

            // Simulate sending email to receiver
            String receiverEmail = transaction.getToAccount().getUser().getEmail();
            String receiverMessage = String.format(
                    "You have received ₹%s from account %s. Transaction ID: %s",
                    transaction.getAmount(),
                    transaction.getFromAccount().getAccountNumber(),
                    transaction.getTransactionId());
            log.info("Email sent to {}: {}", receiverEmail, receiverMessage);

            log.info("Notifications sent successfully for transaction: {}",
                    transaction.getTransactionId());

        } catch (Exception e) {
            log.error("Failed to send notification for transaction: {}",
                    transaction.getTransactionId(), e);
            // Don't fail the transaction due to notification failure
        }
    }
}