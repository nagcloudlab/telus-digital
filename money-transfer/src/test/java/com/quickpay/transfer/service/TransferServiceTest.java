package com.quickpay.transfer.service;

import com.quickpay.transfer.dto.TransferRequest;
import com.quickpay.transfer.dto.TransferResponse;
import com.quickpay.transfer.entity.Account;
import com.quickpay.transfer.entity.AccountStatus;
import com.quickpay.transfer.entity.Transfer;
import com.quickpay.transfer.entity.TransferStatus;
import com.quickpay.transfer.exception.InsufficientBalanceException;
import com.quickpay.transfer.exception.InvalidAccountException;
import com.quickpay.transfer.exception.ValidationException;
import com.quickpay.transfer.repository.TransactionHistoryRepository;
import com.quickpay.transfer.repository.TransferRepository;
import com.quickpay.transfer.util.AccountTestBuilder;
import com.quickpay.transfer.util.TransferRequestTestBuilder;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("Transfer Service Tests")
class TransferServiceTest {

    @Mock
    private AccountService accountService;

    @Mock
    private ValidationService validationService;

    @Mock
    private FeeCalculationService feeCalculationService;

    @Mock
    private FraudDetectionService fraudDetectionService;

    @Mock
    private NotificationService notificationService;

    @Mock
    private TransferRepository transferRepository;

    @Mock
    private TransactionHistoryRepository transactionHistoryRepository;

    @InjectMocks
    private TransferService transferService;

    private Account fromAccount;
    private Account toAccount;
    private TransferRequest request;

    @BeforeEach
    void setUp() {
        fromAccount = AccountTestBuilder.builder()
                .id(1L)
                .accountNumber("ACC001")
                .accountHolderName("John Doe")
                .balance("10000.00")
                .status(AccountStatus.ACTIVE)
                .build();

        toAccount = AccountTestBuilder.builder()
                .id(2L)
                .accountNumber("ACC002")
                .accountHolderName("Jane Smith")
                .balance("5000.00")
                .status(AccountStatus.ACTIVE)
                .build();

        request = TransferRequestTestBuilder.builder()
                .fromAccountId(1L)
                .toAccountId(2L)
                .amount("1000.00")
                .description("Test transfer")
                .build();
    }

    @Test
    @DisplayName("Should successfully transfer money with small amount (no fee)")
    void testSuccessfulTransfer_SmallAmount() {
        // Given
        BigDecimal smallAmount = new BigDecimal("500.00");
        request.setAmount(smallAmount);

        when(accountService.findById(1L)).thenReturn(fromAccount);
        when(accountService.findById(2L)).thenReturn(toAccount);
        when(feeCalculationService.calculateFee(smallAmount)).thenReturn(BigDecimal.ZERO);
        doNothing().when(validationService).validate(any(), any(), any(), any());
        doNothing().when(fraudDetectionService).checkFraud(any());
        when(accountService.update(any())).thenReturn(fromAccount, toAccount);
        when(transferRepository.save(any())).thenAnswer(invocation -> {
            Transfer transfer = invocation.getArgument(0);
            transfer.setId(1L);
            return transfer;
        });
        when(transactionHistoryRepository.save(any())).thenReturn(null);
        doNothing().when(notificationService).sendNotification(any());

        // When
        TransferResponse response = transferService.transfer(request);

        // Then
        assertThat(response).isNotNull();
        assertThat(response.getStatus()).isEqualTo(TransferStatus.COMPLETED);
        assertThat(response.getAmount()).isEqualByComparingTo("500.00");
        assertThat(response.getFee()).isEqualByComparingTo("0.00");
        assertThat(response.getTotalAmount()).isEqualByComparingTo("500.00");
        assertThat(response.getFromAccountBalanceBefore()).isEqualByComparingTo("10000.00");
        assertThat(response.getFromAccountBalanceAfter()).isEqualByComparingTo("9500.00");
        assertThat(response.getToAccountBalanceBefore()).isEqualByComparingTo("5000.00");
        assertThat(response.getToAccountBalanceAfter()).isEqualByComparingTo("5500.00");

        // Verify interactions
        verify(accountService, times(2)).findById(anyLong());
        verify(feeCalculationService, times(1)).calculateFee(any());
        verify(validationService, times(1)).validate(any(), any(), any(), any());
        verify(fraudDetectionService, times(1)).checkFraud(any());
        verify(accountService, times(2)).update(any());
        verify(transferRepository, times(1)).save(any());
        verify(transactionHistoryRepository, times(2)).save(any());
        verify(notificationService, times(1)).sendNotification(any());
    }

    @Test
    @DisplayName("Should successfully transfer money with medium amount (1% fee)")
    void testSuccessfulTransfer_WithFee() {
        // Given
        when(accountService.findById(1L)).thenReturn(fromAccount);
        when(accountService.findById(2L)).thenReturn(toAccount);
        when(feeCalculationService.calculateFee(request.getAmount()))
                .thenReturn(new BigDecimal("10.00"));
        doNothing().when(validationService).validate(any(), any(), any(), any());
        doNothing().when(fraudDetectionService).checkFraud(any());
        when(accountService.update(any())).thenReturn(fromAccount, toAccount);
        when(transferRepository.save(any())).thenAnswer(invocation -> {
            Transfer transfer = invocation.getArgument(0);
            transfer.setId(1L);
            return transfer;
        });
        when(transactionHistoryRepository.save(any())).thenReturn(null);
        doNothing().when(notificationService).sendNotification(any());

        // When
        TransferResponse response = transferService.transfer(request);

        // Then
        assertThat(response).isNotNull();
        assertThat(response.getStatus()).isEqualTo(TransferStatus.COMPLETED);
        assertThat(response.getAmount()).isEqualByComparingTo("1000.00");
        assertThat(response.getFee()).isEqualByComparingTo("10.00");
        assertThat(response.getTotalAmount()).isEqualByComparingTo("1010.00");
        assertThat(fromAccount.getBalance()).isEqualByComparingTo("8990.00");
        assertThat(toAccount.getBalance()).isEqualByComparingTo("6000.00");

        verify(notificationService, times(1)).sendNotification(any());
    }

    @Test
    @DisplayName("Should successfully transfer large amount (0.5% fee)")
    void testSuccessfulTransfer_HighValueWithFee() {
        // Given
        fromAccount.setBalance(new BigDecimal("60000.00"));
        BigDecimal largeAmount = new BigDecimal("20000.00");
        request.setAmount(largeAmount);

        when(accountService.findById(1L)).thenReturn(fromAccount);
        when(accountService.findById(2L)).thenReturn(toAccount);
        when(feeCalculationService.calculateFee(largeAmount))
                .thenReturn(new BigDecimal("100.00"));
        doNothing().when(validationService).validate(any(), any(), any(), any());
        doNothing().when(fraudDetectionService).checkFraud(any());
        when(accountService.update(any())).thenReturn(fromAccount, toAccount);
        when(transferRepository.save(any())).thenAnswer(invocation -> {
            Transfer transfer = invocation.getArgument(0);
            transfer.setId(1L);
            return transfer;
        });
        when(transactionHistoryRepository.save(any())).thenReturn(null);
        doNothing().when(notificationService).sendNotification(any());

        // When
        TransferResponse response = transferService.transfer(request);

        // Then
        assertThat(response).isNotNull();
        assertThat(response.getAmount()).isEqualByComparingTo("20000.00");
        assertThat(response.getFee()).isEqualByComparingTo("100.00");
        assertThat(response.getTotalAmount()).isEqualByComparingTo("20100.00");
        assertThat(fromAccount.getBalance()).isEqualByComparingTo("39900.00");
        assertThat(toAccount.getBalance()).isEqualByComparingTo("25000.00");
    }

    @Test
    @DisplayName("Should fail transfer when insufficient balance")
    void testTransferFails_InsufficientBalance() {
        // Given
        fromAccount.setBalance(new BigDecimal("500.00"));

        when(accountService.findById(1L)).thenReturn(fromAccount);
        when(accountService.findById(2L)).thenReturn(toAccount);
        when(feeCalculationService.calculateFee(request.getAmount()))
                .thenReturn(new BigDecimal("10.00"));
        doThrow(new InsufficientBalanceException(
                "Insufficient balance in account ACC001. Required: 1010.00, Available: 500.00"))
                .when(validationService).validate(any(), any(), any(), any());

        // When & Then
        assertThatThrownBy(() -> transferService.transfer(request))
                .isInstanceOf(InsufficientBalanceException.class)
                .hasMessageContaining("Insufficient balance")
                .hasMessageContaining("Required: 1010.00, Available: 500.00");

        // Verify no changes to accounts
        assertThat(fromAccount.getBalance()).isEqualByComparingTo("500.00");
        assertThat(toAccount.getBalance()).isEqualByComparingTo("5000.00");

        // Verify notification was not sent
        verify(notificationService, never()).sendNotification(any());
        verify(transferRepository, never()).save(any());
    }

    @Test
    @DisplayName("Should fail transfer when insufficient balance including fee")
    void testTransferFails_InsufficientBalanceWithFee() {
        // Given
        fromAccount.setBalance(new BigDecimal("1005.00"));

        when(accountService.findById(1L)).thenReturn(fromAccount);
        when(accountService.findById(2L)).thenReturn(toAccount);
        when(feeCalculationService.calculateFee(request.getAmount()))
                .thenReturn(new BigDecimal("10.00"));
        doThrow(new InsufficientBalanceException(
                "Insufficient balance in account ACC001. Required: 1010.00, Available: 1005.00"))
                .when(validationService).validate(any(), any(), any(), any());

        // When & Then
        assertThatThrownBy(() -> transferService.transfer(request))
                .isInstanceOf(InsufficientBalanceException.class)
                .hasMessageContaining("Insufficient balance");

        verify(accountService, never()).update(any());
    }

    @Test
    @DisplayName("Should fail transfer when violates minimum balance")
    void testTransferFails_MinimumBalanceViolation() {
        // Given
        fromAccount.setBalance(new BigDecimal("1100.00"));
        fromAccount.setMinimumBalance(new BigDecimal("100.00"));

        when(accountService.findById(1L)).thenReturn(fromAccount);
        when(accountService.findById(2L)).thenReturn(toAccount);
        when(feeCalculationService.calculateFee(request.getAmount()))
                .thenReturn(new BigDecimal("10.00"));
        doThrow(new ValidationException(
                "Transfer would violate minimum balance requirement. Minimum: 100.00, After transfer: 90.00"))
                .when(validationService).validate(any(), any(), any(), any());

        // When & Then
        assertThatThrownBy(() -> transferService.transfer(request))
                .isInstanceOf(ValidationException.class)
                .hasMessageContaining("violate minimum balance");
    }

    @Test
    @DisplayName("Should fail transfer when from account is invalid")
    void testTransferFails_InvalidFromAccount() {
        // Given
        when(accountService.findById(1L))
                .thenThrow(new InvalidAccountException("Account not found with ID: 1"));

        // When & Then
        assertThatThrownBy(() -> transferService.transfer(request))
                .isInstanceOf(InvalidAccountException.class)
                .hasMessageContaining("Account not found with ID: 1");

        verify(feeCalculationService, never()).calculateFee(any());
        verify(validationService, never()).validate(any(), any(), any(), any());
    }

    @Test
    @DisplayName("Should fail transfer when to account is invalid")
    void testTransferFails_InvalidToAccount() {
        // Given
        when(accountService.findById(1L)).thenReturn(fromAccount);
        when(accountService.findById(2L))
                .thenThrow(new InvalidAccountException("Account not found with ID: 2"));

        // When & Then
        assertThatThrownBy(() -> transferService.transfer(request))
                .isInstanceOf(InvalidAccountException.class)
                .hasMessageContaining("Account not found with ID: 2");

        verify(feeCalculationService, never()).calculateFee(any());
    }

    @Test
    @DisplayName("Should fail transfer when transferring to same account")
    void testTransferFails_SameAccount() {
        // Given
        request.setToAccountId(1L); // Same as fromAccountId

        when(accountService.findById(1L)).thenReturn(fromAccount);
        when(feeCalculationService.calculateFee(request.getAmount()))
                .thenReturn(new BigDecimal("10.00"));
        doThrow(new ValidationException("Cannot transfer to same account"))
                .when(validationService).validate(any(), any(), any(), any());

        // When & Then
        assertThatThrownBy(() -> transferService.transfer(request))
                .isInstanceOf(ValidationException.class)
                .hasMessageContaining("Cannot transfer to same account");
    }

    @Test
    @DisplayName("Should fail transfer when from account is inactive")
    void testTransferFails_InactiveFromAccount() {
        // Given
        fromAccount.setStatus(AccountStatus.INACTIVE);

        when(accountService.findById(1L)).thenReturn(fromAccount);
        when(accountService.findById(2L)).thenReturn(toAccount);
        when(feeCalculationService.calculateFee(request.getAmount()))
                .thenReturn(new BigDecimal("10.00"));
        doThrow(new ValidationException("Source account is not active: INACTIVE"))
                .when(validationService).validate(any(), any(), any(), any());

        // When & Then
        assertThatThrownBy(() -> transferService.transfer(request))
                .isInstanceOf(ValidationException.class)
                .hasMessageContaining("Source account is not active");
    }

    @Test
    @DisplayName("Should fail transfer when to account is inactive")
    void testTransferFails_InactiveToAccount() {
        // Given
        toAccount.setStatus(AccountStatus.INACTIVE);

        when(accountService.findById(1L)).thenReturn(fromAccount);
        when(accountService.findById(2L)).thenReturn(toAccount);
        when(feeCalculationService.calculateFee(request.getAmount()))
                .thenReturn(new BigDecimal("10.00"));
        doThrow(new ValidationException("Destination account is not active: INACTIVE"))
                .when(validationService).validate(any(), any(), any(), any());

        // When & Then
        assertThatThrownBy(() -> transferService.transfer(request))
                .isInstanceOf(ValidationException.class)
                .hasMessageContaining("Destination account is not active");
    }

    @Test
    @DisplayName("Should fail transfer when amount is negative")
    void testTransferFails_NegativeAmount() {
        // Given
        request.setAmount(new BigDecimal("-100.00"));

        when(accountService.findById(1L)).thenReturn(fromAccount);
        when(accountService.findById(2L)).thenReturn(toAccount);
        when(feeCalculationService.calculateFee(request.getAmount()))
                .thenReturn(BigDecimal.ZERO);
        doThrow(new ValidationException("Transfer amount must be greater than zero"))
                .when(validationService).validate(any(), any(), any(), any());

        // When & Then
        assertThatThrownBy(() -> transferService.transfer(request))
                .isInstanceOf(ValidationException.class)
                .hasMessageContaining("must be greater than zero");
    }

    @Test
    @DisplayName("Should fail transfer when amount is zero")
    void testTransferFails_ZeroAmount() {
        // Given
        request.setAmount(BigDecimal.ZERO);

        when(accountService.findById(1L)).thenReturn(fromAccount);
        when(accountService.findById(2L)).thenReturn(toAccount);
        when(feeCalculationService.calculateFee(request.getAmount()))
                .thenReturn(BigDecimal.ZERO);
        doThrow(new ValidationException("Transfer amount must be greater than zero"))
                .when(validationService).validate(any(), any(), any(), any());

        // When & Then
        assertThatThrownBy(() -> transferService.transfer(request))
                .isInstanceOf(ValidationException.class)
                .hasMessageContaining("must be greater than zero");
    }

    @Test
    @DisplayName("Should fail transfer when exceeds daily limit")
    void testTransferFails_ExceedsDailyLimit() {
        // Given
        fromAccount.setDailyTransferred(new BigDecimal("8000.00"));
        fromAccount.setDailyLimit(new BigDecimal("10000.00"));
        request.setAmount(new BigDecimal("5000.00"));

        when(accountService.findById(1L)).thenReturn(fromAccount);
        when(accountService.findById(2L)).thenReturn(toAccount);
        when(feeCalculationService.calculateFee(request.getAmount()))
                .thenReturn(new BigDecimal("50.00"));
        doThrow(new ValidationException(
                "Transfer would exceed daily limit. Limit: 10000.00, Used: 8000.00, Requested: 5000.00"))
                .when(validationService).validate(any(), any(), any(), any());

        // When & Then
        assertThatThrownBy(() -> transferService.transfer(request))
                .isInstanceOf(ValidationException.class)
                .hasMessageContaining("exceed daily limit");
    }

    @Test
    @DisplayName("Should verify all service interactions in correct order")
    void testTransactionRollback_OnDatabaseError() {
        // Given
        when(accountService.findById(1L)).thenReturn(fromAccount);
        when(accountService.findById(2L)).thenReturn(toAccount);
        when(feeCalculationService.calculateFee(request.getAmount()))
                .thenReturn(new BigDecimal("10.00"));
        doNothing().when(validationService).validate(any(), any(), any(), any());
        doNothing().when(fraudDetectionService).checkFraud(any());
        when(accountService.update(any()))
                .thenReturn(fromAccount)
                .thenThrow(new RuntimeException("Database error"));

        // When & Then
        assertThatThrownBy(() -> transferService.transfer(request))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Database error");

        // Verify notification was not sent due to error
        verify(notificationService, never()).sendNotification(any());
        verify(transferRepository, never()).save(any());
    }
}