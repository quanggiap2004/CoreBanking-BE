package com.banking.core.dto;

import lombok.*;

import java.math.BigDecimal;

/**
 * Response DTO for deposit operations.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DepositResponse {

    private String transactionRef;
    private String status;
    private String message;
    private BigDecimal newBalance;
}
