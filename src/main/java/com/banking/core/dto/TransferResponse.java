package com.banking.core.dto;

import lombok.*;

import java.math.BigDecimal;

/**
 * Response DTO for completed transfers.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TransferResponse {

    private String transactionRef;
    private String status;
    private String message;
    private BigDecimal newSourceBalance;
    private BigDecimal newDestinationBalance;
}
