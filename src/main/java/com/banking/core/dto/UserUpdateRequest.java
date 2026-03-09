package com.banking.core.dto;

import com.banking.core.domain.UserStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class UserUpdateRequest {
    private UserStatus status;
    private Boolean kycVerified;
    private BigDecimal transactionLimit;
}
