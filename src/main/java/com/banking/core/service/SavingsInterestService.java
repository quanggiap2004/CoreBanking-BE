package com.banking.core.service;

import com.banking.core.domain.*;
import com.banking.core.repository.AccountRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.List;

/**
 * Service for calculating and posting interest to savings accounts.
 * Runs on a schedule defined in application.yml
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class SavingsInterestService {

        private final AccountRepository accountRepository;
        private final AuditService auditService;

        @Value("${app.interest.savings-rate}")
        private BigDecimal annualInterestRate;

        /**
         * Scheduled job to calculate and post interest.
         * Runs based on cron expression in application.yml
         */
        @Scheduled(cron = "${app.interest.cron}")
        @Transactional
        public void calculateAndPostInterest() {

                log.info("Starting interest calculation job...");

                // Find all active savings accounts
                List<Account> savingsAccounts = accountRepository
                                .findByAccountTypeAndStatus(AccountType.SAVINGS, AccountStatus.ACTIVE);

                int accountsProcessed = 0;
                BigDecimal totalInterestPosted = BigDecimal.ZERO;

                for (Account account : savingsAccounts) {

                        // Skip if already posted today
                        if (account.getLastInterestDate() != null &&
                                        account.getLastInterestDate().equals(LocalDate.now())) {
                                continue;
                        }

                        // Calculate daily interest: (balance * annual_rate) / 365
                        BigDecimal dailyInterest = account.getBalance()
                                        .multiply(annualInterestRate)
                                        .divide(new BigDecimal("365"), 4, RoundingMode.HALF_UP);

                        if (dailyInterest.compareTo(BigDecimal.ZERO) > 0) {

                                BigDecimal oldBalance = account.getBalance();
                                account.setBalance(oldBalance.add(dailyInterest));
                                account.setLastInterestDate(LocalDate.now());

                                // Create audit log
                                auditService.logBalanceChange(
                                                account,
                                                oldBalance,
                                                account.getBalance(),
                                                null,
                                                ActionType.INTEREST,
                                                "SYSTEM_SCHEDULER");

                                accountsProcessed++;
                                totalInterestPosted = totalInterestPosted.add(dailyInterest);

                                log.debug("Interest posted: Account={}, Amount={}",
                                                account.getAccountNumber(), dailyInterest);
                        }
                }

                log.info("Interest calculation completed: {} accounts processed, total interest: {}",
                                accountsProcessed, totalInterestPosted);
        }
}
