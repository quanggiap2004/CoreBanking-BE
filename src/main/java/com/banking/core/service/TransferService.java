package com.banking.core.service;

import com.banking.core.domain.*;
import com.banking.core.dto.TransferRequest;
import com.banking.core.dto.TransferResponse;
import com.banking.core.exception.AccountNotFoundException;
import com.banking.core.exception.InsufficientFundsException;
import com.banking.core.exception.InvalidAccountStatusException;
import com.banking.core.repository.AccountRepository;
import com.banking.core.repository.TransactionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.UUID;

/**
 * CRITICAL SERVICE: Handles fund transfers with ACID guarantees.
 * 
 * KEY FEATURES:
 * 1. Pessimistic locking to prevent concurrent modifications
 * 2. @Transactional ensures atomicity
 * 3. Deadlock prevention via ordered lock acquisition
 * 4. Comprehensive validation
 * 5. Audit logging for compliance
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class TransferService {

        private final AccountRepository accountRepository;
        private final TransactionRepository transactionRepository;
        private final AuditService auditService;

        /**
         * Executes a fund transfer between two accounts.
         * 
         * CONCURRENCY CONTROL:
         * - Uses READ_COMMITTED isolation to prevent dirty reads
         * - Acquires PESSIMISTIC_WRITE locks on both accounts
         * - Locks acquired in ascending ID order to prevent deadlocks
         * 
         * ATOMICITY:
         * - If ANY step fails, entire transaction rolls back
         * - Database guarantees all-or-nothing execution
         * 
         * @param request transfer details
         * @return transfer response with transaction reference
         * @throws AccountNotFoundException      if account doesn't exist
         * @throws InsufficientFundsException    if source balance is insufficient
         * @throws InvalidAccountStatusException if account is not active
         */
        @Transactional(isolation = Isolation.READ_COMMITTED, rollbackFor = Exception.class)
        public TransferResponse transferFunds(TransferRequest request) {

                log.info("Starting transfer: {} -> {}, amount: {}",
                                request.getSourceAccountNumber(),
                                request.getDestinationAccountNumber(),
                                request.getAmount());

                // Acquire locks in consistent order to prevent deadlocks
                String firstAccountNumber;
                String secondAccountNumber;

                int comparison = request.getSourceAccountNumber().compareTo(request.getDestinationAccountNumber());
                if (comparison < 0) {
                        firstAccountNumber = request.getSourceAccountNumber();
                        secondAccountNumber = request.getDestinationAccountNumber();
                } else {
                        firstAccountNumber = request.getDestinationAccountNumber();
                        secondAccountNumber = request.getSourceAccountNumber();
                }

                log.debug("Acquiring locks: first={}, second={}", firstAccountNumber, secondAccountNumber);

                Account firstAccount = accountRepository.findByAccountNumberWithLock(firstAccountNumber)
                                .orElseThrow(() -> new AccountNotFoundException(
                                                "Account not found: " + firstAccountNumber));

                Account secondAccount = accountRepository.findByAccountNumberWithLock(secondAccountNumber)
                                .orElseThrow(() -> new AccountNotFoundException(
                                                "Account not found: " + secondAccountNumber));

                // Identify which is source and which is destination
                Account sourceAccount = firstAccount.getAccountNumber().equals(request.getSourceAccountNumber())
                                ? firstAccount
                                : secondAccount;
                Account destAccount = firstAccount.getAccountNumber().equals(request.getDestinationAccountNumber())
                                ? firstAccount
                                : secondAccount;

                log.debug("Locks acquired successfully");

                validateTransfer(sourceAccount, destAccount, request.getAmount());

                BigDecimal sourceOldBalance = sourceAccount.getBalance();
                BigDecimal destOldBalance = destAccount.getBalance();

                log.debug("Source balance: {}, Destination balance: {}", sourceOldBalance, destOldBalance);

                // Perform the transfer
                // At this point, rows are locked - no other transaction can modify these
                // accounts!
                sourceAccount.setBalance(sourceOldBalance.subtract(request.getAmount()));
                destAccount.setBalance(destOldBalance.add(request.getAmount()));

                log.debug("Balances updated: Source={}, Dest={}",
                                sourceAccount.getBalance(), destAccount.getBalance());

                Transaction transaction = Transaction.builder()
                                .transactionRef(UUID.randomUUID().toString())
                                .sourceAccountId(sourceAccount.getId())
                                .destinationAccountId(destAccount.getId())
                                .amount(request.getAmount())
                                .transactionType(TransactionType.TRANSFER)
                                .transferType(TransferType.valueOf(request.getTransferType()))
                                .description(request.getDescription())
                                .status(TransactionStatus.COMPLETED)
                                .build();

                transaction = transactionRepository.save(transaction);

                log.info("Transaction created: ref={}", transaction.getTransactionRef());

                auditService.logBalanceChange(
                                sourceAccount,
                                sourceOldBalance,
                                sourceAccount.getBalance(),
                                transaction,
                                ActionType.WITHDRAWAL,
                                "SYSTEM" // TODO: Replace with actual username
                );

                auditService.logBalanceChange(
                                destAccount,
                                destOldBalance,
                                destAccount.getBalance(),
                                transaction,
                                ActionType.DEPOSIT,
                                "SYSTEM");

                // JPA will automatically persist account changes on transaction commit
                // Locks are released when method returns

                log.info("Transfer completed successfully: ref={}", transaction.getTransactionRef());

                return TransferResponse.builder()
                                .transactionRef(transaction.getTransactionRef())
                                .status("SUCCESS")
                                .message("Transfer completed successfully")
                                .newSourceBalance(sourceAccount.getBalance())
                                .newDestinationBalance(destAccount.getBalance())
                                .build();
        }

        /**
         * Validates that transfer can proceed.
         * 
         * Checks:
         * - Both accounts are ACTIVE
         * - Source has sufficient balance
         * - Amount is positive
         */
        private void validateTransfer(Account source, Account dest, BigDecimal amount) {

                if (source.getStatus() != AccountStatus.ACTIVE) {
                        throw new InvalidAccountStatusException(
                                        "Source account is not active: " + source.getAccountNumber());
                }

                if (dest.getStatus() != AccountStatus.ACTIVE) {
                        throw new InvalidAccountStatusException(
                                        "Destination account is not active: " + dest.getAccountNumber());
                }

                if (source.getBalance().compareTo(amount) < 0) {
                        throw new InsufficientFundsException(
                                        String.format("Insufficient funds. Available: %s, Required: %s",
                                                        source.getBalance(), amount));
                }

                if (amount.compareTo(BigDecimal.ZERO) <= 0) {
                        throw new IllegalArgumentException("Transfer amount must be greater than zero");
                }

                log.debug("Validation passed");
        }
}
