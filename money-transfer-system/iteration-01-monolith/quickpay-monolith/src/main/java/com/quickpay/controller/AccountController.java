package com.quickpay.controller;

import com.quickpay.dto.AccountBalanceResponse;
import com.quickpay.service.AccountService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/accounts")
@RequiredArgsConstructor
@Slf4j
public class AccountController {

    private final AccountService accountService;

    @GetMapping("/{accountNumber}/balance")
    public ResponseEntity<AccountBalanceResponse> getBalance(@PathVariable String accountNumber) {
        log.info("Balance inquiry for account: {}", accountNumber);
        AccountBalanceResponse response = accountService.getBalance(accountNumber);
        return ResponseEntity.ok(response);
    }
}