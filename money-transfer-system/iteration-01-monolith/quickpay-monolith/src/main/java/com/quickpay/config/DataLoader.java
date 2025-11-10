package com.quickpay.config;

import com.quickpay.model.Account;
import com.quickpay.model.AccountStatus;
import com.quickpay.model.User;
import com.quickpay.repository.AccountRepository;
import com.quickpay.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import java.math.BigDecimal;

@Component
@RequiredArgsConstructor
@Slf4j
public class DataLoader implements CommandLineRunner {

    private final UserRepository userRepository;
    private final AccountRepository accountRepository;

    @Override
    public void run(String... args) {
        log.info("Checking if demo data exists...");

        // Check if data already exists
        if (userRepository.count() > 0) {
            log.info("Demo data already exists. Skipping initialization.");
            return;
        }

        log.info("Loading demo data...");

        // Create demo users
        User ramesh = createUser("ramesh@example.com", "Ramesh Kumar", "9876543210");
        User priya = createUser("priya@example.com", "Priya Sharma", "9876543211");
        User amit = createUser("amit@example.com", "Amit Patel", "9876543212");

        // Create demo accounts
        createAccount("ACC123456", ramesh, new BigDecimal("50000.00"));
        createAccount("ACC987654", priya, new BigDecimal("30000.00"));
        createAccount("ACC555777", amit, new BigDecimal("75000.00"));

        log.info("Demo data loaded successfully!");
        log.info("Demo Accounts:");
        log.info("  - ramesh@example.com -> ACC123456 (₹50,000)");
        log.info("  - priya@example.com -> ACC987654 (₹30,000)");
        log.info("  - amit@example.com -> ACC555777 (₹75,000)");
    }

    private User createUser(String email, String fullName, String mobile) {
        User user = new User();
        user.setEmail(email);
        user.setPassword("password"); // Simple password for demo
        user.setFullName(fullName);
        user.setMobileNumber(mobile);
        user.setEnabled(true);
        return userRepository.save(user);
    }

    private Account createAccount(String accountNumber, User user, BigDecimal balance) {
        Account account = new Account();
        account.setAccountNumber(accountNumber);
        account.setUser(user);
        account.setBalance(balance);
        account.setCurrency("INR");
        account.setStatus(AccountStatus.ACTIVE);
        return accountRepository.save(account);
    }
}