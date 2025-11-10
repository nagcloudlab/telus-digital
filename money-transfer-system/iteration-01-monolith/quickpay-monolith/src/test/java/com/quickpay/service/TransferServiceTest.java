package com.quickpay.service;

import com.quickpay.dto.TransferRequest;
import com.quickpay.dto.TransferResponse;
import com.quickpay.exception.AccountNotFoundException;
import com.quickpay.exception.InsufficientBalanceException;
import com.quickpay.model.*;
import com.quickpay.repository.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TransferServiceTest {

    @Mock
    private AccountRepository accountRepository;

    @Mock
    private TransactionRepository transactionRepository;

    @Mock
    private FraudDetectionService fraudDetectionService;

    @Mock
    private NotificationService notificationService;

    @Mock
    private AuditService auditService;

    @InjectMocks
    private TransferService transferService;

    private Account fromAccount;
    private Account toAccount;
    private User fromUser;
    private User toUser;

    @BeforeEach
    void setUp() {
        fromUser = new User();
        fromUser.setId(1L);
        fromUser.setEmail("ramesh@example.com");
        fromUser.setFullName("Ramesh Kumar");

        toUser = new User();
        toUser.setId(2L);
        toUser.setEmail("priya@example.com");
        toUser.setFullName("Priya Sharma");

        fromAccount = new Account();
        fromAccount.setId(1L);
        fromAccount.setAccountNumber("ACC123456");
        fromAccount.setUser(fromUser);
        fromAccount.setBalance(new BigDecimal("50000.00"));
        fromAccount.setStatus(AccountStatus.ACTIVE);

        toAccount = new Account();
        toAccount.setId(2L);
        toAccount.setAccountNumber("ACC987654");
        toAccount.setUser(toUser);
        toAccount.setBalance(new BigDecimal("30000.00"));
        toAccount.setStatus(AccountStatus.ACTIVE);
    }

    @Test
    void testSuccessfulTransfer() {
        // Arrange
        TransferRequest request = new TransferRequest();
        request.setFromAccountNumber("ACC123456");
        request.setToAccountNumber("ACC987654");
        request.setAmount(new BigDecimal("5000.00"));
        request.setDescription("Birthday gift");

        when(accountRepository.findByAccountNumber("ACC123456"))
                .thenReturn(Optional.of(fromAccount));
        when(accountRepository.findByAccountNumber("ACC987654"))
                .thenReturn(Optional.of(toAccount));
        when(fraudDetectionService.calculateRiskScore(any(), any(), any()))
                .thenReturn(0.2);
        when(transactionRepository.save(any(Transaction.class)))
                .thenReturn(new Transaction());

        // Act
        TransferResponse response = transferService.transferMoney(request);

        // Assert
        assertTrue(response.isSuccess());
        assertEquals(TransactionStatus.COMPLETED, response.getStatus());
        assertEquals(new BigDecimal("45000.00"), fromAccount.getBalance());
        assertEquals(new BigDecimal("35000.00"), toAccount.getBalance());

        verify(accountRepository, times(2)).save(any(Account.class));
        verify(transactionRepository, times(1)).save(any(Transaction.class));
        verify(notificationService, times(1)).sendTransferNotification(any());
        verify(auditService, times(1)).logTransfer(any());
    }

    @Test
    void testTransferWithInsufficientBalance() {
        // Arrange
        TransferRequest request = new TransferRequest();
        request.setFromAccountNumber("ACC123456");
        request.setToAccountNumber("ACC987654");
        request.setAmount(new BigDecimal("60000.00")); // More than balance

        when(accountRepository.findByAccountNumber("ACC123456"))
                .thenReturn(Optional.of(fromAccount));
        when(accountRepository.findByAccountNumber("ACC987654"))
                .thenReturn(Optional.of(toAccount));

        // Act & Assert
        assertThrows(InsufficientBalanceException.class, () -> {
            transferService.transferMoney(request);
        });

        verify(accountRepository, never()).save(any());
        verify(transactionRepository, never()).save(any());
    }

    @Test
    void testTransferWithNonExistentFromAccount() {
        // Arrange
        TransferRequest request = new TransferRequest();
        request.setFromAccountNumber("ACC999999");
        request.setToAccountNumber("ACC987654");
        request.setAmount(new BigDecimal("5000.00"));

        when(accountRepository.findByAccountNumber("ACC999999"))
                .thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(AccountNotFoundException.class, () -> {
            transferService.transferMoney(request);
        });
    }

    @Test
    void testTransferWithNonExistentToAccount() {
        // Arrange
        TransferRequest request = new TransferRequest();
        request.setFromAccountNumber("ACC123456");
        request.setToAccountNumber("ACC999999");
        request.setAmount(new BigDecimal("5000.00"));

        when(accountRepository.findByAccountNumber("ACC123456"))
                .thenReturn(Optional.of(fromAccount));
        when(accountRepository.findByAccountNumber("ACC999999"))
                .thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(AccountNotFoundException.class, () -> {
            transferService.transferMoney(request);
        });
    }
}