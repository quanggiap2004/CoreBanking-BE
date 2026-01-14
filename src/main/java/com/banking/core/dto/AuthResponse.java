package com.banking.core.dto;

import lombok.*;

/**
 * Response DTO containing JWT token.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AuthResponse {

    private String token;
    private final String type = "Bearer";
    private String username;
    private String message;
}
