package com.bayraktolga.BayrakBackend.dto.response;

import com.bayraktolga.BayrakBackend.enums.NotificationType;

import java.time.LocalDateTime;
import java.util.UUID;

public record NotificationResponse(
        UUID id,
        UUID userId,
        String title,
        String message,
        NotificationType type,
        Boolean isRead,
        LocalDateTime createdAt
) {}
