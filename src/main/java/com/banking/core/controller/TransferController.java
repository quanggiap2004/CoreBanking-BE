package com.banking.core.controller;

import com.banking.core.dto.TransferRequest;
import com.banking.core.dto.TransferResponse;
import com.banking.core.service.TransferService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * REST controller for fund transfer operations.
 */
@RestController
@RequestMapping("/api/transfers")
@RequiredArgsConstructor
@Slf4j
public class TransferController {

    private final TransferService transferService;

    /**
     * POST /api/transfers
     * Executes a fund transfer between accounts.
     * 
     * Request body:
     * {
     * "sourceAccountNumber": "123456789012",
     * "destinationAccountNumber": "987654321098",
     * "amount": 100.00,
     * "transferType": "INTERNAL",
     * "description": "Payment for services"
     * }
     */
    @PostMapping
    public ResponseEntity<TransferResponse> transferFunds(
            @Valid @RequestBody TransferRequest request) {

        log.info("Transfer request received: {} -> {}",
                request.getSourceAccountNumber(),
                request.getDestinationAccountNumber());

        TransferResponse response = transferService.transferFunds(request);

        return ResponseEntity.ok(response);
    }
}
