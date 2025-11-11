package com.quickpay.transfer.util;

import com.quickpay.transfer.entity.Account;
import com.quickpay.transfer.entity.AccountStatus;

import java.math.BigDecimal;

public class AccountTestBuilder {

    private Long id = 1L;
    private String accountNumber = "ACC001";
    private String accountHolderName = "John Doe";
    private BigDecimal balance = new BigDecimal("10000.00");
    private String currency = "USD";
    private AccountStatus status = AccountStatus.ACTIVE;
    private String accountType = "SAVINGS";
    private BigDecimal dailyLimit = new BigDecimal("10000.00");
    private BigDecimal dailyTransferred = BigDecimal.ZERO;
    private BigDecimal minimumBalance = new BigDecimal("100.00");
    private Integer version = 0;

    public static AccountTestBuilder builder() {
        return new AccountTestBuilder();
    }

    public AccountTestBuilder id(Long id) {
        this.id = id;
        return this;
    }

    public AccountTestBuilder accountNumber(String accountNumber) {
        this.accountNumber = accountNumber;
        return this;
    }

    public AccountTestBuilder accountHolderName(String accountHolderName) {
        this.accountHolderName = accountHolderName;
        return this;
    }

    public AccountTestBuilder balance(String balance) {
        this.balance = new BigDecimal(balance);
        return this;
    }

    public AccountTestBuilder balance(BigDecimal balance) {
        this.balance = balance;
        return this;
    }

    public AccountTestBuilder status(AccountStatus status) {
        this.status = status;
        return this;
    }

    public AccountTestBuilder dailyLimit(String dailyLimit) {
        this.dailyLimit = new BigDecimal(dailyLimit);
        return this;
    }

    public AccountTestBuilder dailyTransferred(String dailyTransferred) {
        this.dailyTransferred = new BigDecimal(dailyTransferred);
        return this;
    }

    public AccountTestBuilder minimumBalance(String minimumBalance) {
        this.minimumBalance = new BigDecimal(minimumBalance);
        return this;
    }

    public Account build() {
        Account account = new Account();
        account.setId(id);
        account.setAccountNumber(accountNumber);
        account.setAccountHolderName(accountHolderName);
        account.setBalance(balance);
        account.setCurrency(currency);
        account.setStatus(status);
        account.setAccountType(accountType);
        account.setDailyLimit(dailyLimit);
        account.setDailyTransferred(dailyTransferred);
        account.setMinimumBalance(minimumBalance);
        account.setVersion(version);
        return account;
    }
}