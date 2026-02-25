package com.bayraktolga.BayrakBackend.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

import java.math.BigDecimal;

@ResponseStatus(HttpStatus.BAD_REQUEST)
public class InsufficientBalanceException extends RuntimeException {

    public InsufficientBalanceException() {
        super("Yetersiz bakiye.");
    }

    public InsufficientBalanceException(BigDecimal available, BigDecimal requested) {
        super("Yetersiz bakiye. Mevcut: " + available + " TL, İstenen: " + requested + " TL.");
    }
}
