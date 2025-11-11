package com.quickpay.transfer.controller;

import com.quickpay.transfer.dto.TransferRequest;
import com.quickpay.transfer.dto.TransferResponse;
import com.quickpay.transfer.service.TransferService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/transfers")
@RequiredArgsConstructor
@Slf4j
public class TransferController {

    private final TransferService transferService;

    @PostMapping
    public ResponseEntity<TransferResponse> createTransfer(@Valid @RequestBody TransferRequest request) {
        log.info("Received transfer request: {} -> {}, Amount: {}",
                request.getFromAccountId(), request.getToAccountId(), request.getAmount());

        TransferResponse response = transferService.transfer(request);

        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @GetMapping("/health")
    public ResponseEntity<String> health() {
        return ResponseEntity.ok("Money Transfer Service is running!");
    }
}