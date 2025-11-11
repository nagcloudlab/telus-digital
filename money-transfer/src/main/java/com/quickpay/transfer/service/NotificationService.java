package com.quickpay.transfer.service;

import com.quickpay.transfer.entity.Transfer;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class NotificationService {

    public void sendNotification(Transfer transfer) {
        log.info("NOTIFICATION: Transfer {} completed successfully", transfer.getTransferReference());
        log.info("NOTIFICATION: Amount {} transferred from account {} to account {}",
                transfer.getAmount(), transfer.getFromAccountId(), transfer.getToAccountId());

        // Mock: Just log for now
        // In real system, this would send email/SMS
    }
}