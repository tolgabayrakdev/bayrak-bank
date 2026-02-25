package com.bayraktolga.BayrakBackend.dto.request;

import jakarta.validation.constraints.*;

import java.math.BigDecimal;

public record TransferRequest(

        @NotBlank(message = "Gönderen IBAN boş olamaz")
        String senderIban,

        @NotBlank(message = "Alıcı IBAN boş olamaz")
        String receiverIban,

        @NotNull(message = "Tutar boş olamaz")
        @DecimalMin(value = "0.01", message = "Tutar 0'dan büyük olmalıdır")
        @Digits(integer = 13, fraction = 2, message = "Geçersiz tutar formatı")
        BigDecimal amount,

        @Size(max = 255, message = "Açıklama en fazla 255 karakter olabilir")
        String description
) {}
