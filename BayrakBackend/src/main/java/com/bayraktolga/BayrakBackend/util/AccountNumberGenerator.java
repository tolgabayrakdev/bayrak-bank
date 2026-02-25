package com.bayraktolga.BayrakBackend.util;

import org.springframework.stereotype.Component;

import java.util.Random;

/**
 * 16 haneli dahili hesap numarası üretici.
 * Format: 1 + 15 rastgele rakam (ilk hane 0 olmasın)
 */
@Component
public class AccountNumberGenerator {

    private final Random random = new Random();

    public String generate() {
        StringBuilder sb = new StringBuilder();
        // İlk hane 1-9 arası
        sb.append(random.nextInt(9) + 1);
        // Kalan 15 hane
        for (int i = 0; i < 15; i++) {
            sb.append(random.nextInt(10));
        }
        return sb.toString();
    }
}
