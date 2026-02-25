package com.bayraktolga.BayrakBackend.dto.request;

import jakarta.validation.constraints.*;

import java.time.LocalDate;

public record RegisterRequest(

        @NotBlank(message = "TC Kimlik No boş olamaz")
        @Size(min = 11, max = 11, message = "TC Kimlik No 11 karakter olmalıdır")
        @Pattern(regexp = "\\d{11}", message = "TC Kimlik No yalnızca rakam içermelidir")
        String tcNo,

        @NotBlank(message = "Ad boş olamaz")
        @Size(max = 50, message = "Ad en fazla 50 karakter olabilir")
        String firstName,

        @NotBlank(message = "Soyad boş olamaz")
        @Size(max = 50, message = "Soyad en fazla 50 karakter olabilir")
        String lastName,

        @NotBlank(message = "Email boş olamaz")
        @Email(message = "Geçerli bir email adresi giriniz")
        @Size(max = 100, message = "Email en fazla 100 karakter olabilir")
        String email,

        @NotBlank(message = "Şifre boş olamaz")
        @Size(min = 8, message = "Şifre en az 8 karakter olmalıdır")
        String password,

        @Size(max = 15, message = "Telefon numarası en fazla 15 karakter olabilir")
        String phone,

        @NotNull(message = "Doğum tarihi boş olamaz")
        @Past(message = "Doğum tarihi geçmiş bir tarih olmalıdır")
        LocalDate birthDate
) {}
