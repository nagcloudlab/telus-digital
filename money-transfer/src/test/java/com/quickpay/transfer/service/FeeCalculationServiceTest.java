package com.quickpay.transfer.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("Fee Calculation Service Tests")
class FeeCalculationServiceTest {

    private FeeCalculationService feeCalculationService;

    @BeforeEach
    void setUp() {
        feeCalculationService = new FeeCalculationService();
    }

    @Test
    @DisplayName("Should return zero fee for amount below 1000")
    void testNoFee_AmountBelow1000() {
        // Given
        BigDecimal amount = new BigDecimal("500.00");

        // When
        BigDecimal fee = feeCalculationService.calculateFee(amount);

        // Then
        assertThat(fee).isEqualByComparingTo("0.00");
    }

    @Test
    @DisplayName("Should return zero fee for amount exactly 1000")
    void testNoFee_AmountExactly1000() {
        // Given
        BigDecimal amount = new BigDecimal("1000.00");

        // When
        BigDecimal fee = feeCalculationService.calculateFee(amount);

        // Then
        assertThat(fee).isEqualByComparingTo("0.00");
    }

    @Test
    @DisplayName("Should return 1% fee for amount 1001")
    void testBasicFee_Amount1001() {
        // Given
        BigDecimal amount = new BigDecimal("1001.00");

        // When
        BigDecimal fee = feeCalculationService.calculateFee(amount);

        // Then
        assertThat(fee).isEqualByComparingTo("10.01");
    }

    @Test
    @DisplayName("Should return 1% fee for amount 5000")
    void testBasicFee_Amount5000() {
        // Given
        BigDecimal amount = new BigDecimal("5000.00");

        // When
        BigDecimal fee = feeCalculationService.calculateFee(amount);

        // Then
        assertThat(fee).isEqualByComparingTo("50.00");
    }

    @Test
    @DisplayName("Should return 1% fee for amount exactly 10000")
    void testBasicFee_Amount10000() {
        // Given
        BigDecimal amount = new BigDecimal("10000.00");

        // When
        BigDecimal fee = feeCalculationService.calculateFee(amount);

        // Then
        assertThat(fee).isEqualByComparingTo("100.00");
    }

    @Test
    @DisplayName("Should return 0.5% fee for amount 10001")
    void testPremiumFee_Amount10001() {
        // Given
        BigDecimal amount = new BigDecimal("10001.00");

        // When
        BigDecimal fee = feeCalculationService.calculateFee(amount);

        // Then
        assertThat(fee).isEqualByComparingTo("50.01");
    }

    @Test
    @DisplayName("Should return 0.5% fee for amount 50000")
    void testPremiumFee_Amount50000() {
        // Given
        BigDecimal amount = new BigDecimal("50000.00");

        // When
        BigDecimal fee = feeCalculationService.calculateFee(amount);

        // Then
        assertThat(fee).isEqualByComparingTo("250.00");
    }

    @Test
    @DisplayName("Should return 0.25% fee for amount 100000")
    void testHighValueFee_Amount100000() {
        // Given
        BigDecimal amount = new BigDecimal("100000.00");

        // When
        BigDecimal fee = feeCalculationService.calculateFee(amount);

        // Then
        assertThat(fee).isEqualByComparingTo("250.00");
    }

    @Test
    @DisplayName("Should round fee to 2 decimal places")
    void testFeeRounding_TwoDecimals() {
        // Given
        BigDecimal amount = new BigDecimal("1234.56");

        // When
        BigDecimal fee = feeCalculationService.calculateFee(amount);

        // Then
        assertThat(fee).isEqualByComparingTo("12.35");
        assertThat(fee.scale()).isEqualTo(2);
    }

    @Test
    @DisplayName("Should apply maximum fee cap of 500")
    void testMaximumFeeCap_500() {
        // Given - amount that would result in fee > 500
        BigDecimal amount = new BigDecimal("1000000.00");

        // When
        BigDecimal fee = feeCalculationService.calculateFee(amount);

        // Then
        assertThat(fee).isEqualByComparingTo("500.00");
    }
}