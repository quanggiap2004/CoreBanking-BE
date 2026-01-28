package com.banking.core.dto;

import lombok.*;

/**
 * Response DTO containing JWT token and user information.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AuthResponse {

    private String token;

    @Builder.Default
    private String type = "Bearer";

    private Long userId;
    private String username;
    private java.math.BigDecimal transactionLimit;
    private String message;
}
