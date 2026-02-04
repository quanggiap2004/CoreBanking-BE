package com.banking.core.controller;

import com.banking.core.dto.DepositRequest;
import com.banking.core.dto.DepositResponse;
import com.banking.core.service.DepositService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/deposits")
@RequiredArgsConstructor
@Slf4j
public class DepositController {

    private final DepositService depositService;

    @PostMapping
    public ResponseEntity<DepositResponse> deposit(@Valid @RequestBody DepositRequest request) {

        log.info("Received deposit request for account: {}", request.getAccountNumber());

        DepositResponse response = depositService.processDeposit(request);

        return ResponseEntity.ok(response);
    }
}
