package com.quickpay.transfer.service;

import com.quickpay.transfer.dto.TransferRequest;
import com.quickpay.transfer.entity.Account;
import com.quickpay.transfer.entity.AccountStatus;
import com.quickpay.transfer.exception.InsufficientBalanceException;
import com.quickpay.transfer.exception.ValidationException;
import com.quickpay.transfer.util.AccountTestBuilder;
import com.quickpay.transfer.util.TransferRequestTestBuilder;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.Assertions.assertThatCode;

@DisplayName("Validation Service Tests")
class ValidationServiceTest {

    private ValidationService validationService;

    @BeforeEach
    void setUp() {
        validationService = new ValidationService();
    }

    @Test
    @DisplayName("Should pass validation for valid transfer request")
    void testValidTransferRequest() {
        // Given
        Account fromAccount = AccountTestBuilder.builder()
                .id(1L)
                .balance("10000.00")
                .status(AccountStatus.ACTIVE)
                .build();

        Account toAccount = AccountTestBuilder.builder()
                .id(2L)
                .balance("5000.00")
                .status(AccountStatus.ACTIVE)
                .build();

        TransferRequest request = TransferRequestTestBuilder.builder()
                .amount("1000.00")
                .build();

        BigDecimal totalAmount = new BigDecimal("1010.00");

        // When & Then - should not throw exception
        assertThatCode(() -> validationService.validate(request, fromAccount, toAccount, totalAmount))
                .doesNotThrowAnyException();
    }

    @Test
    @DisplayName("Should throw exception when from account is null")
    void testValidation_NullFromAccount() {
        // Given
        Account toAccount = AccountTestBuilder.builder().build();
        TransferRequest request = TransferRequestTestBuilder.builder().build();
        BigDecimal totalAmount = new BigDecimal("1010.00");

        // When & Then
        assertThatThrownBy(() -> validationService.validate(request, null, toAccount, totalAmount))
                .isInstanceOf(ValidationException.class)
                .hasMessageContaining("Source account not found");
    }

    @Test
    @DisplayName("Should throw exception when to account is null")
    void testValidation_NullToAccount() {
        // Given
        Account fromAccount = AccountTestBuilder.builder().build();
        TransferRequest request = TransferRequestTestBuilder.builder().build();
        BigDecimal totalAmount = new BigDecimal("1010.00");

        // When & Then
        assertThatThrownBy(() -> validationService.validate(request, fromAccount, null, totalAmount))
                .isInstanceOf(ValidationException.class)
                .hasMessageContaining("Destination account not found");
    }

    @Test
    @DisplayName("Should throw exception when transferring to same account")
    void testValidation_SameAccount() {
        // Given
        Account account = AccountTestBuilder.builder()
                .id(1L)
                .balance("10000.00")
                .build();

        TransferRequest request = TransferRequestTestBuilder.builder()
                .fromAccountId(1L)
                .toAccountId(1L)
                .build();

        BigDecimal totalAmount = new BigDecimal("1010.00");

        // When & Then
        assertThatThrownBy(() -> validationService.validate(request, account, account, totalAmount))
                .isInstanceOf(ValidationException.class)
                .hasMessageContaining("Cannot transfer to same account");
    }

    @Test
    @DisplayName("Should throw exception when from account is inactive")
    void testValidation_InactiveFromAccount() {
        // Given
        Account fromAccount = AccountTestBuilder.builder()
                .id(1L)
                .balance("10000.00")
                .status(AccountStatus.INACTIVE)
                .build();

        Account toAccount = AccountTestBuilder.builder()
                .id(2L)
                .build();

        TransferRequest request = TransferRequestTestBuilder.builder().build();
        BigDecimal totalAmount = new BigDecimal("1010.00");

        // When & Then
        assertThatThrownBy(() -> validationService.validate(request, fromAccount, toAccount, totalAmount))
                .isInstanceOf(ValidationException.class)
                .hasMessageContaining("Source account is not active");
    }

    @Test
    @DisplayName("Should throw exception when to account is inactive")
    void testValidation_InactiveToAccount() {
        // Given
        Account fromAccount = AccountTestBuilder.builder()
                .id(1L)
                .balance("10000.00")
                .build();

        Account toAccount = AccountTestBuilder.builder()
                .id(2L)
                .status(AccountStatus.INACTIVE)
                .build();

        TransferRequest request = TransferRequestTestBuilder.builder().build();
        BigDecimal totalAmount = new BigDecimal("1010.00");

        // When & Then
        assertThatThrownBy(() -> validationService.validate(request, fromAccount, toAccount, totalAmount))
                .isInstanceOf(ValidationException.class)
                .hasMessageContaining("Destination account is not active");
    }

    @Test
    @DisplayName("Should throw exception when amount is negative")
    void testValidation_NegativeAmount() {
        // Given
        Account fromAccount = AccountTestBuilder.builder().build();
        Account toAccount = AccountTestBuilder.builder().id(2L).build();

        TransferRequest request = TransferRequestTestBuilder.builder()
                .amount("-100.00")
                .build();

        BigDecimal totalAmount = new BigDecimal("-100.00");

        // When & Then
        assertThatThrownBy(() -> validationService.validate(request, fromAccount, toAccount, totalAmount))
                .isInstanceOf(ValidationException.class)
                .hasMessageContaining("must be greater than zero");
    }

    @Test
    @DisplayName("Should throw exception when amount is zero")
    void testValidation_ZeroAmount() {
        // Given
        Account fromAccount = AccountTestBuilder.builder().build();
        Account toAccount = AccountTestBuilder.builder().id(2L).build();

        TransferRequest request = TransferRequestTestBuilder.builder()
                .amount("0.00")
                .build();

        BigDecimal totalAmount = BigDecimal.ZERO;

        // When & Then
        assertThatThrownBy(() -> validationService.validate(request, fromAccount, toAccount, totalAmount))
                .isInstanceOf(ValidationException.class)
                .hasMessageContaining("must be greater than zero");
    }

    @Test
    @DisplayName("Should throw exception when insufficient balance")
    void testValidation_InsufficientBalance() {
        // Given
        Account fromAccount = AccountTestBuilder.builder()
                .balance("500.00")
                .build();

        Account toAccount = AccountTestBuilder.builder()
                .id(2L)
                .build();

        TransferRequest request = TransferRequestTestBuilder.builder()
                .amount("1000.00")
                .build();

        BigDecimal totalAmount = new BigDecimal("1010.00");

        // When & Then
        assertThatThrownBy(() -> validationService.validate(request, fromAccount, toAccount, totalAmount))
                .isInstanceOf(InsufficientBalanceException.class)
                .hasMessageContaining("Insufficient balance");
    }

    @Test
    @DisplayName("Should throw exception when exceeds daily limit")
    void testValidation_ExceedsDailyLimit() {
        // Given
        Account fromAccount = AccountTestBuilder.builder()
                .balance("20000.00")
                .dailyLimit("10000.00")
                .dailyTransferred("8000.00")
                .build();

        Account toAccount = AccountTestBuilder.builder()
                .id(2L)
                .build();

        TransferRequest request = TransferRequestTestBuilder.builder()
                .amount("5000.00")
                .build();

        BigDecimal totalAmount = new BigDecimal("5050.00");

        // When & Then
        assertThatThrownBy(() -> validationService.validate(request, fromAccount, toAccount, totalAmount))
                .isInstanceOf(ValidationException.class)
                .hasMessageContaining("exceed daily limit");
    }

    @Test
    @DisplayName("Should throw exception when violates minimum balance")
    void testValidation_MinimumBalanceViolation() {
        // Given
        Account fromAccount = AccountTestBuilder.builder()
                .balance("1200.00")
                .minimumBalance("100.00")
                .build();

        Account toAccount = AccountTestBuilder.builder()
                .id(2L)
                .build();

        TransferRequest request = TransferRequestTestBuilder.builder()
                .amount("1150.00")
                .build();

        BigDecimal totalAmount = new BigDecimal("1161.50"); // 1150 + 11.50 fee

        // When & Then
        assertThatThrownBy(() -> validationService.validate(request, fromAccount, toAccount, totalAmount))
                .isInstanceOf(ValidationException.class)
                .hasMessageContaining("violate minimum balance");
    }

    @Test
    @DisplayName("Should throw exception when amount exceeds maximum transfer limit")
    void testValidation_ExceedsMaxTransferLimit() {
        // Given
        Account fromAccount = AccountTestBuilder.builder()
                .balance("200000.00")
                .build();

        Account toAccount = AccountTestBuilder.builder()
                .id(2L)
                .build();

        TransferRequest request = TransferRequestTestBuilder.builder()
                .amount("150000.00")
                .build();

        BigDecimal totalAmount = new BigDecimal("150375.00");

        // When & Then
        assertThatThrownBy(() -> validationService.validate(request, fromAccount, toAccount, totalAmount))
                .isInstanceOf(ValidationException.class)
                .hasMessageContaining("exceeds maximum limit");
    }
}