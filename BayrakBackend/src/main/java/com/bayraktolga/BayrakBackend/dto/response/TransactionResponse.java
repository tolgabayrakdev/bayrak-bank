package com.bayraktolga.BayrakBackend.dto.response;

import com.bayraktolga.BayrakBackend.enums.TransactionStatus;
import com.bayraktolga.BayrakBackend.enums.TransactionType;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

public record TransactionResponse(
        UUID id,
        String referenceNo,
        String senderIban,
        String receiverIban,
        BigDecimal amount,
        String currency,
        TransactionType type,
        TransactionStatus status,
        String description,
        LocalDateTime createdAt
) {}
