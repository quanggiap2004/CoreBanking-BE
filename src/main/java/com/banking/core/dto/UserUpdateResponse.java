package com.banking.core.dto;

import com.banking.core.domain.UserStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class UserUpdateResponse {
    private Long id;
    private String username;
    private UserStatus status;
    private Boolean kycVerified;
    private BigDecimal transactionLimit;
    private LocalDateTime updatedAt;
}
