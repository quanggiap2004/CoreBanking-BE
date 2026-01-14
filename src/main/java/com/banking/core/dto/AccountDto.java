package com.banking.core.dto;

import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Response DTO for account details.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AccountDto {

    private Long id;
    private String accountNumber;
    private BigDecimal balance;
    private String accountType;
    private String status;
    private BigDecimal interestRate;
    private LocalDateTime createdAt;
}
