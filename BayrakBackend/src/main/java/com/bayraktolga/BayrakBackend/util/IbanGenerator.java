package com.bayraktolga.BayrakBackend.util;

import org.springframework.stereotype.Component;

import java.math.BigInteger;
import java.util.Random;

/**
 * TR standartına uygun IBAN üretici.
 * Format: TR + 2 kontrol hanesi + 5 haneli banka kodu + 1 rezerv + 16 haneli hesap no
 * Toplam: 26 karakter
 */
@Component
public class IbanGenerator {

    private static final String COUNTRY_CODE = "TR";
    private static final String BANK_CODE    = "00064"; // Örnek banka kodu
    private static final int    ACCOUNT_LENGTH = 16;
    private final Random random = new Random();

    public String generate() {
        String reserveDigit = "0";
        String accountNo    = generateNumeric(ACCOUNT_LENGTH);

        // BBAN = banka kodu + rezerv + hesap no
        String bban = BANK_CODE + reserveDigit + accountNo;

        // Kontrol hanesi hesapla (ISO 7064 MOD 97-10)
        String checkDigits = calculateCheckDigits(COUNTRY_CODE, bban);

        return COUNTRY_CODE + checkDigits + bban;
    }

    private String calculateCheckDigits(String countryCode, String bban) {
        // Ülke kodunu sayıya dönüştür (A=10, B=11 ... Z=35)
        String rearranged = bban + countryToDigits(countryCode) + "00";
        BigInteger numeric = new BigInteger(rearranged);
        int remainder = numeric.mod(BigInteger.valueOf(97)).intValue();
        int checkDigit = 98 - remainder;
        return String.format("%02d", checkDigit);
    }

    private String countryToDigits(String countryCode) {
        StringBuilder sb = new StringBuilder();
        for (char c : countryCode.toCharArray()) {
            sb.append(c - 'A' + 10);
        }
        return sb.toString();
    }

    private String generateNumeric(int length) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < length; i++) {
            sb.append(random.nextInt(10));
        }
        return sb.toString();
    }
}
