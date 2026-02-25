package com.bayraktolga.BayrakBackend.dto.request;

import com.bayraktolga.BayrakBackend.enums.AccountType;
import jakarta.validation.constraints.NotNull;

public record CreateAccountRequest(

        @NotNull(message = "Hesap tipi boş olamaz")
        AccountType accountType
) {}
