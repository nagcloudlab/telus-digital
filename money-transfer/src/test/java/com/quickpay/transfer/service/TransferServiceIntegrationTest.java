package com.quickpay.transfer.service;

import com.quickpay.transfer.dto.TransferRequest;
import com.quickpay.transfer.dto.TransferResponse;
import com.quickpay.transfer.entity.Account;
import com.quickpay.transfer.entity.Transfer;
import com.quickpay.transfer.entity.TransferStatus;
import com.quickpay.transfer.exception.InsufficientBalanceException;
import com.quickpay.transfer.repository.AccountRepository;
import com.quickpay.transfer.repository.TransferRepository;
import com.quickpay.transfer.repository.TransactionHistoryRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
@Transactional
@DisplayName("Transfer Service Integration Tests")
class TransferServiceIntegrationTest {

    @Autowired
    private TransferService transferService;

    @Autowired
    private AccountRepository accountRepository;

    @Autowired
    private TransferRepository transferRepository;

    @Autowired
    private TransactionHistoryRepository transactionHistoryRepository;

    private Account fromAccount;
    private Account toAccount;

    @BeforeEach
    void setUp() {
        // Data is loaded from data.sql automatically
        fromAccount = accountRepository.findById(1L).orElseThrow();
        toAccount = accountRepository.findById(2L).orElseThrow();
    }

    @Test
    @DisplayName("Should successfully transfer money with real database")
    void testSuccessfulTransfer_WithRealDatabase() {
        // Given
        TransferRequest request = new TransferRequest();
        request.setFromAccountId(1L);
        request.setToAccountId(2L);
        request.setAmount(new BigDecimal("500.00")); // Amount < 1000 for 0 fee
        request.setDescription("Integration test transfer");

        BigDecimal fromBalanceBefore = fromAccount.getBalance();
        BigDecimal toBalanceBefore = toAccount.getBalance();

        // When
        TransferResponse response = transferService.transfer(request);

        // Then - Verify response
        assertThat(response).isNotNull();
        assertThat(response.getStatus()).isEqualTo(TransferStatus.COMPLETED);
        assertThat(response.getAmount()).isEqualByComparingTo("500.00");
        assertThat(response.getFee()).isEqualByComparingTo("0.00");

        // Then - Verify database changes
        Account updatedFromAccount = accountRepository.findById(1L).orElseThrow();
        Account updatedToAccount = accountRepository.findById(2L).orElseThrow();

        assertThat(updatedFromAccount.getBalance())
                .isEqualByComparingTo(fromBalanceBefore.subtract(new BigDecimal("500.00")));
        assertThat(updatedToAccount.getBalance())
                .isEqualByComparingTo(toBalanceBefore.add(new BigDecimal("500.00")));

        // Then - Verify transfer record created
        List<Transfer> transfers = transferRepository.findByFromAccountId(1L);
        assertThat(transfers).isNotEmpty();

        // Then - Verify transaction history created
        List<com.quickpay.transfer.entity.TransactionHistory> history = transactionHistoryRepository
                .findByAccountId(1L);
        assertThat(history).isNotEmpty();
    }

    @Test
    @DisplayName("Should rollback transaction on failure")
    void testTransactionRollback_OnFailure() {
        // Given
        TransferRequest request = new TransferRequest();
        request.setFromAccountId(5L); // Account with only $100
        request.setToAccountId(2L);
        request.setAmount(new BigDecimal("5000.00")); // More than available

        Account poorAccount = accountRepository.findById(5L).orElseThrow();
        BigDecimal balanceBefore = poorAccount.getBalance();

        // When & Then
        assertThatThrownBy(() -> transferService.transfer(request))
                .isInstanceOf(InsufficientBalanceException.class);

        // Verify database unchanged (transaction rolled back)
        Account unchangedAccount = accountRepository.findById(5L).orElseThrow();
        assertThat(unchangedAccount.getBalance()).isEqualByComparingTo(balanceBefore);

        // Verify no transfer record created
        List<Transfer> transfers = transferRepository.findByFromAccountId(5L);
        assertThat(transfers).isEmpty();
    }

    @Test
    @DisplayName("Should persist transfer with all details")
    void testTransferPersistence() {
        // Given
        TransferRequest request = new TransferRequest();
        request.setFromAccountId(1L);
        request.setToAccountId(2L);
        request.setAmount(new BigDecimal("5000.00"));
        request.setDescription("Detailed transfer test");

        // When
        TransferResponse response = transferService.transfer(request);

        // Then - Verify transfer entity
        Transfer savedTransfer = transferRepository
                .findByTransferReference(response.getTransferId())
                .orElseThrow();

        assertThat(savedTransfer.getFromAccountId()).isEqualTo(1L);
        assertThat(savedTransfer.getToAccountId()).isEqualTo(2L);
        assertThat(savedTransfer.getAmount()).isEqualByComparingTo("5000.00");
        assertThat(savedTransfer.getFee()).isEqualByComparingTo("50.00");
        assertThat(savedTransfer.getTotalAmount()).isEqualByComparingTo("5050.00");
        assertThat(savedTransfer.getStatus()).isEqualTo(TransferStatus.COMPLETED);
        assertThat(savedTransfer.getDescription()).isEqualTo("Detailed transfer test");
        assertThat(savedTransfer.getCreatedAt()).isNotNull();
        assertThat(savedTransfer.getCompletedAt()).isNotNull();
    }

    @Test
    @DisplayName("Should update daily transferred amount")
    void testDailyTransferredTracking() {
        // Given
        Account account = accountRepository.findById(1L).orElseThrow();
        BigDecimal initialDailyTransferred = account.getDailyTransferred();

        TransferRequest request = new TransferRequest();
        request.setFromAccountId(1L);
        request.setToAccountId(2L);
        request.setAmount(new BigDecimal("2000.00"));

        // When
        transferService.transfer(request);

        // Then
        Account updatedAccount = accountRepository.findById(1L).orElseThrow();
        BigDecimal expectedDailyTransferred = initialDailyTransferred
                .add(new BigDecimal("2000.00")); // Amount only, not including fee

        assertThat(updatedAccount.getDailyTransferred())
                .isEqualByComparingTo(expectedDailyTransferred);
    }

    @Test
    @DisplayName("Should create transaction history for both accounts")
    void testTransactionHistoryCreation() {
        // Given
        TransferRequest request = new TransferRequest();
        request.setFromAccountId(1L);
        request.setToAccountId(2L);
        request.setAmount(new BigDecimal("800.00")); // Less than 1000 for simplicity

        int fromHistoryCountBefore = transactionHistoryRepository
                .findByAccountId(1L).size();
        int toHistoryCountBefore = transactionHistoryRepository
                .findByAccountId(2L).size();

        // When
        transferService.transfer(request);

        // Then
        int fromHistoryCountAfter = transactionHistoryRepository
                .findByAccountId(1L).size();
        int toHistoryCountAfter = transactionHistoryRepository
                .findByAccountId(2L).size();

        assertThat(fromHistoryCountAfter).isEqualTo(fromHistoryCountBefore + 1);
        assertThat(toHistoryCountAfter).isEqualTo(toHistoryCountBefore + 1);
    }
}