package com.bayraktolga.BayrakBackend.dto.response;

import com.bayraktolga.BayrakBackend.enums.AccountStatus;
import com.bayraktolga.BayrakBackend.enums.AccountType;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

public record AccountResponse(
        UUID id,
        UUID userId,
        String iban,
        String accountNo,
        BigDecimal balance,
        String currency,
        AccountStatus status,
        AccountType accountType,
        LocalDateTime createdAt
) {}
