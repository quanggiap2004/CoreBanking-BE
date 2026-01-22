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

@Repository
public interface AccountRepository extends JpaRepository<Account, Long> {

    /**
     * Finds account with pessimistic lock to prevent concurrent updates.
     */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT a FROM Account a WHERE a.id = :accountId")
    Optional<Account> findByIdWithLock(@Param("accountId") Long accountId);

    Optional<Account> findByAccountNumber(String accountNumber);

    /**
     * Finds account by number with pessimistic lock (used for transfers).
     */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT a FROM Account a WHERE a.accountNumber = :accountNumber")
    Optional<Account> findByAccountNumberWithLock(@Param("accountNumber") String accountNumber);

    List<Account> findByUserId(Long userId);

    // For daily interest calculation job
    List<Account> findByAccountTypeAndStatus(AccountType accountType, AccountStatus status);

    Account findByAccountNumberOrderByBalanceDesc(String accountNumber);
}
