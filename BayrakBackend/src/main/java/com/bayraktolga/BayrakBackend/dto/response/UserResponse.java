package com.bayraktolga.BayrakBackend.dto.response;

import com.bayraktolga.BayrakBackend.enums.Role;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

public record UserResponse(
        UUID id,
        String tcNo,
        String firstName,
        String lastName,
        String email,
        String phone,
        LocalDate birthDate,
        Role role,
        Boolean isActive,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {}
