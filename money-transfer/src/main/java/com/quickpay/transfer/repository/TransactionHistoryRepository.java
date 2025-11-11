package com.quickpay.transfer.repository;

import com.quickpay.transfer.entity.TransactionHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TransactionHistoryRepository extends JpaRepository<TransactionHistory, Long> {

    List<TransactionHistory> findByTransferId(Long transferId);

    List<TransactionHistory> findByAccountId(Long accountId);
}