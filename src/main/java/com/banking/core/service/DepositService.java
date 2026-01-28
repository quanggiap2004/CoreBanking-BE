package com.banking.core.service;

import com.banking.core.domain.*;
import com.banking.core.dto.DepositRequest;
import com.banking.core.dto.DepositResponse;
import com.banking.core.exception.AccountNotFoundException;
import com.banking.core.repository.AccountRepository;
import com.banking.core.repository.TransactionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.UUID;

/**
 * Service for handling external deposit operations.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class DepositService {

    private final AccountRepository accountRepository;
    private final TransactionRepository transactionRepository;
    private final AuditService auditService;

    /**
     * Process an external deposit to an account.
     * FR-005: Users can initiate deposits
     * FR-006: Validates amount > 0 (handled by @Valid in controller)
     * FR-007: Increases account balance
     * FR-008: Records DEPOSIT transaction
     */
    @Transactional
    public DepositResponse processDeposit(DepositRequest request) {

        log.info("Processing deposit: account={}, amount={}, source={}",
                request.getAccountNumber(), request.getAmount(), request.getSource());

        // Find the account
        Account account = accountRepository.findByAccountNumber(request.getAccountNumber())
                .orElseThrow(() -> new AccountNotFoundException(
                        "Account not found: " + request.getAccountNumber()));

        // Validate amount (additional check beyond @Valid)
        if (request.getAmount().compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Deposit amount must be greater than zero");
        }

        BigDecimal oldBalance = account.getBalance();
        BigDecimal newBalance = oldBalance.add(request.getAmount());

        log.debug("Updating balance: {} -> {}", oldBalance, newBalance);

        // Update account balance (FR-007)
        account.setBalance(newBalance);
        accountRepository.save(account);

        // Create transaction record (FR-008)
        Transaction transaction = Transaction.builder()
                .transactionRef(UUID.randomUUID().toString())
                .destinationAccountId(account.getId())
                .amount(request.getAmount())
                .transactionType(TransactionType.DEPOSIT)
                .description(request.getDescription() != null
                        ? request.getDescription()
                        : "External deposit from " + request.getSource())
                .status(TransactionStatus.COMPLETED)
                .build();

        Transaction savedTransaction = transactionRepository.save(transaction);

        log.info("Deposit transaction created: ref={}", savedTransaction.getTransactionRef());

        // Audit log
        auditService.logBalanceChange(
                account,
                oldBalance,
                newBalance,
                savedTransaction,
                ActionType.DEPOSIT,
                "EXTERNAL:" + request.getSource());

        log.info("Deposit completed successfully: ref={}, newBalance={}",
                savedTransaction.getTransactionRef(), newBalance);

        return DepositResponse.builder()
                .transactionRef(savedTransaction.getTransactionRef())
                .status("SUCCESS")
                .message("Deposit completed successfully")
                .newBalance(newBalance)
                .build();
    }
}
