package com.banking.core.service;

import com.banking.core.domain.*;
import com.banking.core.dto.RegisterRequest;
import com.banking.core.dto.UserAdminResponse;
import com.banking.core.exception.UserNotFoundException;
import com.banking.core.repository.AccountRepository;
import com.banking.core.repository.UserRepository;
import com.banking.core.repository.UserAuditLogRepository;
import com.banking.core.dto.UserUpdateResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.security.core.context.SecurityContextHolder;

import java.math.BigDecimal;
import java.util.List;
import java.util.Random;

/**
 * Service for user registration, account creation, and admin user queries.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class UserService {

        private final UserRepository userRepository;
        private final AccountRepository accountRepository;
        private final UserAuditLogRepository userAuditLogRepository;
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
                                .idDocumentNumber(request.getIdDocumentNumber().isEmpty() ? null
                                                : request.getIdDocumentNumber())
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
         * Returns all users mapped to admin response DTOs.
         */
        @Transactional(readOnly = true)
        public List<UserAdminResponse> getAllUsers() {
                return userRepository.findAllWithAccounts().stream()
                                .map(this::mapToAdminResponse)
                                .toList();
        }

        /**
         * Returns a single user by ID mapped to admin response DTO.
         * Throws UserNotFoundException if user does not exist.
         */
        @Transactional(readOnly = true)
        public UserAdminResponse getUserById(Long id) {
                User user = userRepository.findByIdWithAccounts(id)
                                .orElseThrow(() -> new UserNotFoundException("User not found with id: " + id));
                return mapToAdminResponse(user);
        }

        /**
         * Updates user details (status, kycVerified, transactionLimit) and creates
         * audit logs.
         */
        @Transactional
        public UserUpdateResponse updateUser(Long id, com.banking.core.dto.UserUpdateRequest request) {
                User user = userRepository.findById(id)
                                .orElseThrow(() -> new UserNotFoundException("User not found with id: " + id));

                String adminUsername = "SYSTEM";
                if (SecurityContextHolder.getContext().getAuthentication() != null) {
                        adminUsername = SecurityContextHolder.getContext().getAuthentication().getName();
                }

                if (request.getStatus() != null && !request.getStatus().equals(user.getStatus())) {
                        logAudit(user.getId(), "STATUS", user.getStatus().name(), request.getStatus().name(),
                                        adminUsername);
                        user.setStatus(request.getStatus());
                }

                if (request.getKycVerified() != null && !request.getKycVerified().equals(user.getKycVerified())) {
                        logAudit(user.getId(), "KYC_VERIFIED", String.valueOf(user.getKycVerified()),
                                        String.valueOf(request.getKycVerified()), adminUsername);
                        user.setKycVerified(request.getKycVerified());
                }

                if (request.getTransactionLimit() != null
                                && request.getTransactionLimit().compareTo(user.getTransactionLimit()) != 0) {
                        if (request.getTransactionLimit().compareTo(BigDecimal.ZERO) <= 0) {
                                throw new IllegalArgumentException("Transaction limit must be strictly > 0");
                        }
                        logAudit(user.getId(), "TRANSACTION_LIMIT", String.valueOf(user.getTransactionLimit()),
                                        String.valueOf(request.getTransactionLimit()), adminUsername);
                        user.setTransactionLimit(request.getTransactionLimit());
                }

                userRepository.save(user);

                return com.banking.core.dto.UserUpdateResponse.builder()
                                .id(user.getId())
                                .username(user.getUsername())
                                .status(user.getStatus())
                                .kycVerified(user.getKycVerified())
                                .transactionLimit(user.getTransactionLimit())
                                .updatedAt(java.time.LocalDateTime.now())
                                .build();
        }

        private void logAudit(Long userId, String field, String oldValue, String newValue, String adminUsername) {
                UserAuditLog auditLog = UserAuditLog.builder()
                                .userId(userId)
                                .changedField(field)
                                .oldValue(oldValue)
                                .newValue(newValue)
                                .changedBy(adminUsername)
                                .build();
                userAuditLogRepository.save(auditLog);
        }

        /**
         * Maps a User entity to UserAdminResponse DTO.
         * Aggregates balance from all associated accounts.
         */
        private UserAdminResponse mapToAdminResponse(User user) {
                BigDecimal totalBalance = user.getAccounts().stream()
                                .map(Account::getBalance)
                                .reduce(BigDecimal.ZERO, BigDecimal::add);

                return UserAdminResponse.builder()
                                .id(user.getId())
                                .username(user.getUsername())
                                .email(user.getEmail())
                                .balance(totalBalance)
                                .transactionLimit(user.getTransactionLimit())
                                .status(user.getStatus().name())
                                .role(user.getRole().name())
                                .build();
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
