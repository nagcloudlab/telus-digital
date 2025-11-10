package com.quickpay.service;

import com.quickpay.dto.AccountBalanceResponse;
import com.quickpay.exception.AccountNotFoundException;
import com.quickpay.model.Account;
import com.quickpay.model.AccountStatus;
import com.quickpay.model.User;
import com.quickpay.repository.AccountRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AccountServiceTest {

    @Mock
    private AccountRepository accountRepository;

    @InjectMocks
    private AccountService accountService;

    private Account account;
    private User user;

    @BeforeEach
    void setUp() {
        user = new User();
        user.setId(1L);
        user.setEmail("ramesh@example.com");

        account = new Account();
        account.setId(1L);
        account.setAccountNumber("ACC123456");
        account.setUser(user);
        account.setBalance(new BigDecimal("50000.00"));
        account.setCurrency("INR");
        account.setStatus(AccountStatus.ACTIVE);
    }

    @Test
    void testGetBalanceSuccess() {
        // Arrange
        when(accountRepository.findByAccountNumber("ACC123456"))
                .thenReturn(Optional.of(account));

        // Act
        AccountBalanceResponse response = accountService.getBalance("ACC123456");

        // Assert
        assertNotNull(response);
        assertEquals("ACC123456", response.getAccountNumber());
        assertEquals(new BigDecimal("50000.00"), response.getBalance());
        assertEquals("INR", response.getCurrency());
        assertEquals("ACTIVE", response.getStatus());

        verify(accountRepository, times(1)).findByAccountNumber("ACC123456");
    }

    @Test
    void testGetBalanceAccountNotFound() {
        // Arrange
        when(accountRepository.findByAccountNumber("ACC999999"))
                .thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(AccountNotFoundException.class, () -> {
            accountService.getBalance("ACC999999");
        });

        verify(accountRepository, times(1)).findByAccountNumber("ACC999999");
    }
}