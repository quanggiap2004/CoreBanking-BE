package com.banking.core.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * Admin-facing DTO for user details. Exposes fields that standard users should
 * not see.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserAdminResponse {

    private Long id;
    private String username;
    private String email;
    private BigDecimal balance;
    private BigDecimal transactionLimit;
    private String status;
    private String role;
}
