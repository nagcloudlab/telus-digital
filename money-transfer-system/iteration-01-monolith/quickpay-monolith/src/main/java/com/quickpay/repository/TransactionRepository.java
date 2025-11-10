package com.quickpay.repository;

import com.quickpay.model.Account;
import com.quickpay.model.Transaction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface TransactionRepository extends JpaRepository<Transaction, Long> {
    List<Transaction> findByFromAccountOrToAccountOrderByCreatedAtDesc(Account fromAccount, Account toAccount);

    @Query("SELECT COUNT(t) FROM Transaction t WHERE t.fromAccount = :account AND t.createdAt >= :since")
    long countByFromAccountAndCreatedAtAfter(Account account, LocalDateTime since);
}