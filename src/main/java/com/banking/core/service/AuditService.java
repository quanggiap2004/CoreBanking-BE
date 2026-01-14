package com.banking.core.service;

import com.banking.core.domain.*;
import com.banking.core.repository.AuditLogRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

/**
 * Service for creating audit logs.
 * Every balance change MUST be logged for compliance.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class AuditService {

    private final AuditLogRepository auditLogRepository;

    /**
     * Logs a balance change to the audit trail.
     * 
     * @param account         the account being modified
     * @param previousBalance balance before change
     * @param newBalance      balance after change
     * @param transaction     related transaction (can be null)
     * @param actionType      type of action performed
     * @param initiatedBy     username who initiated the action
     */
    @Transactional
    public void logBalanceChange(Account account, BigDecimal previousBalance,
            BigDecimal newBalance, Transaction transaction,
            ActionType actionType, String initiatedBy) {

        BigDecimal changeAmount = newBalance.subtract(previousBalance);

        AuditLog auditLog = AuditLog.builder()
                .accountId(account.getId())
                .transactionId(transaction != null ? transaction.getId() : null)
                .previousBalance(previousBalance)
                .newBalance(newBalance)
                .changeAmount(changeAmount)
                .actionType(actionType)
                .initiatedBy(initiatedBy)
                .build();

        auditLogRepository.save(auditLog);

        log.info("Audit log created: Account={}, Change={}, Action={}",
                account.getAccountNumber(), changeAmount, actionType);
    }
}
