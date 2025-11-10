package com.quickpay.integration;

import com.quickpay.dto.TransferRequest;
import com.quickpay.dto.TransferResponse;
import com.quickpay.model.Account;
import com.quickpay.model.AccountStatus;
import com.quickpay.model.User;
import com.quickpay.repository.AccountRepository;
import com.quickpay.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.*;
import org.springframework.test.context.ActiveProfiles;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class TransferIntegrationTest {

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private AccountRepository accountRepository;

    @BeforeEach
    void setUp() {
        // Clean up
        accountRepository.deleteAll();
        userRepository.deleteAll();

        // Create test users
        User user1 = new User();
        user1.setEmail("test1@example.com");
        user1.setPassword("password");
        user1.setFullName("Test User 1");
        user1.setMobileNumber("1234567890");
        user1 = userRepository.save(user1);

        User user2 = new User();
        user2.setEmail("test2@example.com");
        user2.setPassword("password");
        user2.setFullName("Test User 2");
        user2.setMobileNumber("0987654321");
        user2 = userRepository.save(user2);

        // Create test accounts
        Account account1 = new Account();
        account1.setAccountNumber("ACC111111");
        account1.setUser(user1);
        account1.setBalance(new BigDecimal("10000.00"));
        account1.setStatus(AccountStatus.ACTIVE);
        accountRepository.save(account1);

        Account account2 = new Account();
        account2.setAccountNumber("ACC222222");
        account2.setUser(user2);
        account2.setBalance(new BigDecimal("5000.00"));
        account2.setStatus(AccountStatus.ACTIVE);
        accountRepository.save(account2);
    }

    @Test
    void testTransferMoneySuccess() {
        // Arrange
        TransferRequest request = new TransferRequest();
        request.setFromAccountNumber("ACC111111");
        request.setToAccountNumber("ACC222222");
        request.setAmount(new BigDecimal("1000.00"));
        request.setDescription("Test transfer");

        // Create headers to disable CSRF for test
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        HttpEntity<TransferRequest> entity = new HttpEntity<>(request, headers);

        // Act - Use .withBasicAuth() for test authentication
        ResponseEntity<TransferResponse> response = restTemplate
                .withBasicAuth("test", "test") // Add basic auth for test
                .postForEntity("/api/transfers", entity, TransferResponse.class);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue(response.getBody().isSuccess());
        assertNotNull(response.getBody().getTransactionId());

        // Verify balances
        Account fromAccount = accountRepository.findByAccountNumber("ACC111111").orElseThrow();
        Account toAccount = accountRepository.findByAccountNumber("ACC222222").orElseThrow();

        assertEquals(new BigDecimal("9000.00"), fromAccount.getBalance());
        assertEquals(new BigDecimal("6000.00"), toAccount.getBalance());
    }
}