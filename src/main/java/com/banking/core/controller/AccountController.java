package com.banking.core.controller;

import com.banking.core.domain.AuditLog;
import com.banking.core.dto.AccountDto;
import com.banking.core.repository.AuditLogRepository;
import com.banking.core.service.AccountService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Controller for account operations.
 */
@RestController
@RequestMapping("/api/accounts")
@RequiredArgsConstructor
public class AccountController {

    private final AccountService accountService;
    private final AuditLogRepository auditLogRepository;

    /**
     * GET /api/accounts/{id}
     * Retrieves account details.
     */
    @GetMapping("/{id}")
    public ResponseEntity<AccountDto> getAccount(@PathVariable Long id) {
        AccountDto account = accountService.getAccountById(id);
        return ResponseEntity.ok(account);
    }

    /**
     * GET /api/accounts/{id}/audit
     * Retrieves audit trail for an account.
     */
    @GetMapping("/{id}/audit")
    public ResponseEntity<List<AuditLog>> getAuditTrail(@PathVariable Long id) {
        List<AuditLog> auditLogs = auditLogRepository.findByAccountIdOrderByCreatedAtDesc(id);
        return ResponseEntity.ok(auditLogs);
    }

    /**
     * GET /api/accounts/user/{userId}
     * Retrieves all accounts for a user.
     */
    @GetMapping("/user/{userId}")
    public ResponseEntity<List<AccountDto>> getUserAccounts(@PathVariable Long userId) {
        List<AccountDto> accounts = accountService.getUserAccounts(userId);
        return ResponseEntity.ok(accounts);
    }
}
