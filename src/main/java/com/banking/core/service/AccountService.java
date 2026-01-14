package com.banking.core.service;

import com.banking.core.domain.Account;
import com.banking.core.dto.AccountDto;
import com.banking.core.exception.AccountNotFoundException;
import com.banking.core.repository.AccountRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Service for account management operations.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class AccountService {

    private final AccountRepository accountRepository;

    /**
     * Retrieves account details by ID.
     */
    @Transactional(readOnly = true)
    public AccountDto getAccountById(Long accountId) {
        Account account = accountRepository.findById(accountId)
                .orElseThrow(() -> new AccountNotFoundException(accountId));

        return mapToDto(account);
    }

    /**
     * Retrieves all accounts for a user.
     */
    @Transactional(readOnly = true)
    public List<AccountDto> getUserAccounts(Long userId) {
        return accountRepository.findByUserId(userId)
                .stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    /**
     * Maps Account entity to DTO.
     */
    private AccountDto mapToDto(Account account) {
        return AccountDto.builder()
                .id(account.getId())
                .accountNumber(account.getAccountNumber())
                .balance(account.getBalance())
                .accountType(account.getAccountType().name())
                .status(account.getStatus().name())
                .interestRate(account.getInterestRate())
                .createdAt(account.getCreatedAt())
                .build();
    }
}
