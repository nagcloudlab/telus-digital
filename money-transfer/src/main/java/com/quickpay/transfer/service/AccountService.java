package com.quickpay.transfer.service;

import com.quickpay.transfer.entity.Account;
import com.quickpay.transfer.exception.InvalidAccountException;
import com.quickpay.transfer.repository.AccountRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class AccountService {

    private final AccountRepository accountRepository;

    @Transactional(readOnly = true)
    public Account findById(Long id) {
        log.debug("Finding account by ID: {}", id);
        return accountRepository.findById(id)
                .orElseThrow(() -> new InvalidAccountException("Account not found with ID: " + id));
    }

    @Transactional
    public Account update(Account account) {
        log.debug("Updating account: {}", account.getAccountNumber());
        return accountRepository.save(account);
    }
}