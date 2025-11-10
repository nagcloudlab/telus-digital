package com.quickpay.controller;

import com.quickpay.dto.TransferRequest;
import com.quickpay.dto.TransferResponse;
import com.quickpay.dto.TransactionHistoryResponse;
import com.quickpay.service.TransferService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/transfers")
@RequiredArgsConstructor
@Slf4j
public class TransferController {

    private final TransferService transferService;

    @PostMapping
    public ResponseEntity<TransferResponse> transfer(@Valid @RequestBody TransferRequest request) {
        log.info("Transfer request received: {} -> {}",
                request.getFromAccountNumber(),
                request.getToAccountNumber());

        TransferResponse response = transferService.transferMoney(request);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/history/{accountNumber}")
    public ResponseEntity<List<TransactionHistoryResponse>> getHistory(@PathVariable String accountNumber) {
        log.info("Transaction history request for account: {}", accountNumber);
        List<TransactionHistoryResponse> history = transferService.getTransactionHistory(accountNumber);
        return ResponseEntity.ok(history);
    }
}