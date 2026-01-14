package com.banking.core.repository;

import com.banking.core.domain.Account;
import com.banking.core.domain.AccountStatus;
import com.banking.core.domain.AccountType;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository for Account entity.
 * 
 * KEY FEATURE: Pessimistic locking method to prevent concurrent modifications.
 */
@Repository
public interface AccountRepository extends JpaRepository<Account, Long> {

    /**
     * Finds account with PESSIMISTIC_WRITE lock.
     * 
     * This generates SQL: SELECT ... FROM accounts WHERE id = ? FOR UPDATE
     * 
     * The FOR UPDATE clause:
     * - Acquires exclusive row-level lock
     * - Blocks other transactions from reading/writing this row
     * - Lock is released when transaction commits/rolls back
     * 
     * PREVENTS RACE CONDITIONS AND DOUBLE SPENDING!
     * 
     * @param accountId the account ID
     * @return Optional containing locked account
     */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT a FROM Account a WHERE a.id = :accountId")
    Optional<Account> findByIdWithLock(@Param("accountId") Long accountId);

    /**
     * Finds account by account number.
     */
    Optional<Account> findByAccountNumber(String accountNumber);

    /**
     * Finds all accounts belonging to a user.
     */
    List<Account> findByUserId(Long userId);

    /**
     * Finds all SAVINGS accounts with ACTIVE status.
     * Used by scheduler for interest calculation.
     */
    List<Account> findByAccountTypeAndStatus(AccountType accountType, AccountStatus status);
}
