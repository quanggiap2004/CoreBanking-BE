package com.banking.core.repository;

import com.banking.core.domain.AuditLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AuditLogRepository extends JpaRepository<AuditLog, Long> {

    List<AuditLog> findByAccountId(Long accountId);

    List<AuditLog> findByTransactionId(Long transactionId);

    List<AuditLog> findByAccountIdOrderByCreatedAtDesc(Long accountId);
}
