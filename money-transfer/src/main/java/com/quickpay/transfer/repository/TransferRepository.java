package com.quickpay.transfer.repository;

import com.quickpay.transfer.entity.Transfer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface TransferRepository extends JpaRepository<Transfer, Long> {

    Optional<Transfer> findByTransferReference(String transferReference);

    List<Transfer> findByFromAccountId(Long fromAccountId);

    List<Transfer> findByToAccountId(Long toAccountId);
}