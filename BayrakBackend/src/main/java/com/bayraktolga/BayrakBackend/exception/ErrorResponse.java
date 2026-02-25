package com.bayraktolga.BayrakBackend.exception;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.time.LocalDateTime;
import java.util.List;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record ErrorResponse(
        int status,
        String error,
        String message,
        String path,
        LocalDateTime timestamp,
        List<FieldError> fieldErrors
) {
    public record FieldError(
            String field,
            String message
    ) {}

    // Genel hata için factory method
    public static ErrorResponse of(int status, String error, String message, String path) {
        return new ErrorResponse(status, error, message, path, LocalDateTime.now(), null);
    }

    // Validasyon hatası için factory method
    public static ErrorResponse ofValidation(int status, String path, List<FieldError> fieldErrors) {
        return new ErrorResponse(status, "Validation Failed", "Girdi doğrulama hatası", path, LocalDateTime.now(), fieldErrors);
    }
}
