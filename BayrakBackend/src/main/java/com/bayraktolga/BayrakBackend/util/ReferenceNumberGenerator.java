package com.bayraktolga.BayrakBackend.util;

import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Random;

/**
 * Transfer referans numarası üretici.
 * Format: BNK + yyyyMMddHHmmss + 3 rastgele rakam  → toplam 20 karakter
 */
@Component
public class ReferenceNumberGenerator {

    private static final String PREFIX = "BNK";
    private static final DateTimeFormatter FORMATTER =
            DateTimeFormatter.ofPattern("yyyyMMddHHmmss");
    private final Random random = new Random();

    public String generate() {
        String timestamp = LocalDateTime.now().format(FORMATTER);  // 14 karakter
        String suffix    = String.format("%03d", random.nextInt(1000)); // 3 karakter
        return PREFIX + timestamp + suffix; // 3 + 14 + 3 = 20 karakter
    }
}
