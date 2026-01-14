package com.banking.core.repository;

import com.banking.core.domain.Transaction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface TransactionRepository extends JpaRepository<Transaction, Long> {

    Optional<Transaction> findByTransactionRef(String transactionRef);

    List<Transaction> findBySourceAccountId(Long accountId);

    List<Transaction> findByDestinationAccountId(Long accountId);

    List<Transaction> findBySourceAccountIdOrDestinationAccountId(Long sourceId, Long destinationId);

    List<Transaction> findByCreatedAtBetween(LocalDateTime start, LocalDateTime end);
}
