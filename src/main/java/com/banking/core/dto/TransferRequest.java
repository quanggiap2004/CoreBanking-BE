package com.banking.core.dto;

import jakarta.validation.constraints.*;
import lombok.*;

import java.math.BigDecimal;

/**
 * Request DTO for fund transfers.
 * Uses account numbers instead of internal IDs for better user experience.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TransferRequest {

    @NotBlank(message = "Source account number is required")
    private String sourceAccountNumber;

    @NotBlank(message = "Destination account number is required")
    private String destinationAccountNumber;

    @NotNull(message = "Amount is required")
    @DecimalMin(value = "0.01", message = "Amount must be greater than 0")
    @DecimalMax(value = "100000.00", message = "Amount cannot exceed 100,000")
    private BigDecimal amount;

    @NotBlank(message = "Transfer type is required")
    private String transferType; // INTERNAL or INTERBANK_MOCK

    private String description;
}
