package com.quickpay.transfer.controller;

import com.quickpay.transfer.dto.TransferRequest;
import com.quickpay.transfer.dto.TransferResponse;
import com.quickpay.transfer.entity.TransferStatus;
import com.quickpay.transfer.exception.ErrorResponse;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Transactional
@DisplayName("Transfer Controller API Integration Tests")
class TransferControllerIntegrationTest {

    @Autowired
    private TestRestTemplate restTemplate;

    @Test
    @DisplayName("Should return 200 OK for health check")
    void testHealthCheck() {
        // When
        ResponseEntity<String> response = restTemplate.getForEntity(
                "/api/transfers/health",
                String.class);

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isEqualTo("Money Transfer Service is running!");
    }

    @Test
    @DisplayName("Should successfully transfer money via API")
    @DirtiesContext
    void testSuccessfulTransfer_ViaAPI() {
        // Given
        TransferRequest request = new TransferRequest();
        request.setFromAccountId(1L);
        request.setToAccountId(2L);
        request.setAmount(new BigDecimal("500.00")); // Amount < 1000 for 0 fee
        request.setDescription("API test transfer");

        // When
        ResponseEntity<TransferResponse> response = restTemplate.postForEntity(
                "/api/transfers",
                request,
                TransferResponse.class);

        // Then - HTTP Status
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);

        // Then - Response body
        TransferResponse body = response.getBody();
        assertThat(body).isNotNull();
        assertThat(body.getStatus()).isEqualTo(TransferStatus.COMPLETED);
        assertThat(body.getTransferId()).isNotNull();
        assertThat(body.getAmount()).isEqualByComparingTo("500.00");
        assertThat(body.getFee()).isEqualByComparingTo("0.00");
        assertThat(body.getTotalAmount()).isEqualByComparingTo("500.00");
        assertThat(body.getFromAccountBalanceBefore()).isEqualByComparingTo("10000.00");
        assertThat(body.getFromAccountBalanceAfter()).isEqualByComparingTo("9500.00");
        assertThat(body.getToAccountBalanceBefore()).isEqualByComparingTo("5000.00");
        assertThat(body.getToAccountBalanceAfter()).isEqualByComparingTo("5500.00");
        assertThat(body.getTimestamp()).isNotNull();
        assertThat(body.getMessage()).contains("successfully");
    }

    @Test
    @DisplayName("Should return 409 CONFLICT for insufficient balance")
    @DirtiesContext
    void testInsufficientBalance_Returns409() {
        // Given
        TransferRequest request = new TransferRequest();
        request.setFromAccountId(5L); // Account with $100
        request.setToAccountId(2L);
        request.setAmount(new BigDecimal("5000.00"));

        // When
        ResponseEntity<ErrorResponse> response = restTemplate.postForEntity(
                "/api/transfers",
                request,
                ErrorResponse.class);

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getStatus()).isEqualTo(409);
        assertThat(response.getBody().getMessage()).contains("Insufficient balance");
    }

    @Test
    @DisplayName("Should return 404 NOT FOUND for invalid account")
    @DirtiesContext
    void testInvalidAccount_Returns404() {
        // Given
        TransferRequest request = new TransferRequest();
        request.setFromAccountId(999L); // Non-existent account
        request.setToAccountId(2L);
        request.setAmount(new BigDecimal("100.00"));

        // When
        ResponseEntity<ErrorResponse> response = restTemplate.postForEntity(
                "/api/transfers",
                request,
                ErrorResponse.class);

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getMessage()).contains("Account not found");
    }

    @Test
    @DisplayName("Should return 400 BAD REQUEST for null amount")
    void testNullAmount_Returns400() {
        // Given
        TransferRequest request = new TransferRequest();
        request.setFromAccountId(1L);
        request.setToAccountId(2L);
        request.setAmount(null); // Invalid: null amount

        // When
        ResponseEntity<String> response = restTemplate.postForEntity(
                "/api/transfers",
                request,
                String.class);

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    }

    @Test
    @DisplayName("Should return 400 BAD REQUEST for negative amount")
    @DirtiesContext
    void testNegativeAmount_Returns400() {
        // Given
        TransferRequest request = new TransferRequest();
        request.setFromAccountId(1L);
        request.setToAccountId(2L);
        request.setAmount(new BigDecimal("-100.00")); // Invalid: negative

        // When
        ResponseEntity<ErrorResponse> response = restTemplate.postForEntity(
                "/api/transfers",
                request,
                ErrorResponse.class);

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    }

    @Test
    @DisplayName("Should return 400 BAD REQUEST for same account transfer")
    @DirtiesContext
    void testSameAccountTransfer_Returns400() {
        // Given
        TransferRequest request = new TransferRequest();
        request.setFromAccountId(1L);
        request.setToAccountId(1L); // Same account
        request.setAmount(new BigDecimal("100.00"));

        // When
        ResponseEntity<ErrorResponse> response = restTemplate.postForEntity(
                "/api/transfers",
                request,
                ErrorResponse.class);

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getMessage()).contains("same account");
    }

    @Test
    @DisplayName("Should handle large transfer amount")
    @DirtiesContext
    void testLargeTransfer() {
        // Given
        TransferRequest request = new TransferRequest();
        request.setFromAccountId(3L); // Account with $50,000
        request.setToAccountId(1L);
        request.setAmount(new BigDecimal("20000.00"));

        // When
        ResponseEntity<TransferResponse> response = restTemplate.postForEntity(
                "/api/transfers",
                request,
                TransferResponse.class);

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getAmount()).isEqualByComparingTo("20000.00");
        assertThat(response.getBody().getFee()).isEqualByComparingTo("100.00"); // 0.5%
    }

    @Test
    @DisplayName("Should handle small transfer with zero fee")
    @DirtiesContext
    void testSmallTransfer_ZeroFee() {
        // Given
        TransferRequest request = new TransferRequest();
        request.setFromAccountId(1L);
        request.setToAccountId(2L);
        request.setAmount(new BigDecimal("500.00")); // Below $1000

        // When
        ResponseEntity<TransferResponse> response = restTemplate.postForEntity(
                "/api/transfers",
                request,
                TransferResponse.class);

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getFee()).isEqualByComparingTo("0.00");
    }

    @Test
    @DisplayName("Should validate request body structure")
    void testInvalidRequestBody() {
        // Given - Send request with missing required fields
        TransferRequest invalidRequest = new TransferRequest();
        // All fields are null - should fail validation

        // When
        ResponseEntity<String> response = restTemplate.postForEntity(
                "/api/transfers",
                invalidRequest,
                String.class);

        // Then - Should return 400 for validation error
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    }

    @Test
    @DisplayName("Should return proper content type")
    @DirtiesContext
    void testContentType() {
        // Given
        TransferRequest request = new TransferRequest();
        request.setFromAccountId(1L);
        request.setToAccountId(2L);
        request.setAmount(new BigDecimal("100.00"));

        // When
        ResponseEntity<TransferResponse> response = restTemplate.postForEntity(
                "/api/transfers",
                request,
                TransferResponse.class);

        // Then
        assertThat(response.getHeaders().getContentType().toString())
                .contains("application/json");
    }
}