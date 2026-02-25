package com.bayraktolga.BayrakBackend.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.FORBIDDEN)
public class AccountBlockedException extends RuntimeException {

    public AccountBlockedException() {
        super("Hesap bloke edilmiş.");
    }

    public AccountBlockedException(String iban) {
        super("Hesap bloke edilmiş: " + iban);
    }
}
