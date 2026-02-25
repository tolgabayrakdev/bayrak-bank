package com.bayraktolga.BayrakBackend.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Past;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;

public record UpdateProfileRequest(

        @Size(max = 50, message = "Ad en fazla 50 karakter olabilir")
        String firstName,

        @Size(max = 50, message = "Soyad en fazla 50 karakter olabilir")
        String lastName,

        @Email(message = "Geçerli bir email adresi giriniz")
        @Size(max = 100, message = "Email en fazla 100 karakter olabilir")
        String email,

        @Size(max = 15, message = "Telefon numarası en fazla 15 karakter olabilir")
        String phone,

        @Past(message = "Doğum tarihi geçmiş bir tarih olmalıdır")
        LocalDate birthDate
) {}
