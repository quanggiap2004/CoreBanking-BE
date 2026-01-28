package com.banking.core.controller;

import com.banking.core.dto.DepositRequest;
import com.banking.core.dto.DepositResponse;
import com.banking.core.service.DepositService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * Controller for external deposit operations.
 */
@RestController
@RequestMapping("/api/deposits")
@RequiredArgsConstructor
@Slf4j
public class DepositController {

    private final DepositService depositService;

    /**
     * POST /api/deposits
     * Process an external deposit to an account.
     * 
     * @param request Deposit details including account number, amount, and source
     * @return Deposit confirmation with transaction reference and new balance
     */
    @PostMapping
    public ResponseEntity<DepositResponse> deposit(@Valid @RequestBody DepositRequest request) {

        log.info("Received deposit request for account: {}", request.getAccountNumber());

        DepositResponse response = depositService.processDeposit(request);

        return ResponseEntity.ok(response);
    }
}
