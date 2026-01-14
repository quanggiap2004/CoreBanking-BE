package com.banking.core.service;

import com.banking.core.domain.*;
import com.banking.core.dto.RegisterRequest;
import com.banking.core.exception.UserNotFoundException;
import com.banking.core.repository.AccountRepository;
import com.banking.core.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.Random;

/**
 * Service for user registration and account creation.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class UserService {

    private final UserRepository userRepository;
    private final AccountRepository accountRepository;
    private final PasswordEncoder passwordEncoder;

    /**
     * Registers a new user and creates a default savings account.
     */
    @Transactional
    public User registerUser(RegisterRequest request) {

        log.info("Registering new user: {}", request.getUsername());

        // Create user
        User user = User.builder()
                .username(request.getUsername())
                .passwordHash(passwordEncoder.encode(request.getPassword()))
                .fullName(request.getFullName())
                .email(request.getEmail())
                .phone(request.getPhone())
                .address(request.getAddress())
                .idDocumentNumber(request.getIdDocumentNumber())
                .kycVerified(false)
                .status(UserStatus.ACTIVE)
                .build();

        user = userRepository.save(user);

        // Auto-create a savings account
        Account account = Account.builder()
                .accountNumber(generateAccountNumber())
                .user(user)
                .balance(BigDecimal.ZERO)
                .accountType(AccountType.SAVINGS)
                .status(AccountStatus.ACTIVE)
                .interestRate(new BigDecimal("0.0350")) // 3.5% annual
                .build();

        accountRepository.save(account);

        log.info("User registered successfully: {} with account: {}",
                user.getUsername(), account.getAccountNumber());

        return user;
    }

    /**
     * Finds user by username.
     */
    public User findByUsername(String username) {
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new UserNotFoundException("User not found: " + username));
    }

    /**
     * Generates a unique 12-digit account number.
     */
    private String generateAccountNumber() {
        Random random = new Random();
        long number = 100000000000L + random.nextLong(900000000000L);
        return String.valueOf(number);
    }
}
