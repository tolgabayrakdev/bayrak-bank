package com.bayraktolga.BayrakBackend.service;

import com.bayraktolga.BayrakBackend.dto.request.CreateAccountRequest;
import com.bayraktolga.BayrakBackend.dto.response.AccountResponse;
import com.bayraktolga.BayrakBackend.entity.Account;
import com.bayraktolga.BayrakBackend.entity.User;
import com.bayraktolga.BayrakBackend.enums.AccountStatus;
import com.bayraktolga.BayrakBackend.enums.NotificationType;
import com.bayraktolga.BayrakBackend.repository.AccountRepository;
import com.bayraktolga.BayrakBackend.util.AccountNumberGenerator;
import com.bayraktolga.BayrakBackend.util.IbanGenerator;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AccountService {

    private final AccountRepository accountRepository;
    private final UserService userService;
    private final NotificationService notificationService;
    private final IbanGenerator ibanGenerator;
    private final AccountNumberGenerator accountNumberGenerator;

    @Transactional
    public AccountResponse createAccount(UUID userId, CreateAccountRequest request) {
        User user = userService.findUserById(userId);

        String iban      = generateUniqueIban();
        String accountNo = generateUniqueAccountNo();

        Account account = Account.builder()
                .user(user)
                .iban(iban)
                .accountNo(accountNo)
                .balance(BigDecimal.ZERO)
                .currency("TRY")
                .status(AccountStatus.ACTIVE)
                .accountType(request.accountType())
                .build();

        Account saved = accountRepository.save(account);

        notificationService.createNotification(
                user,
                "Yeni Hesap Açıldı",
                "IBAN: " + iban + " numaralı hesabınız başarıyla açıldı.",
                NotificationType.SYSTEM
        );

        return toAccountResponse(saved);
    }

    @Transactional(readOnly = true)
    public List<AccountResponse> getAccountsByUser(UUID userId) {
        return accountRepository.findByUserId(userId)
                .stream()
                .map(this::toAccountResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public AccountResponse getAccountById(UUID accountId, UUID userId) {
        Account account = findAccountById(accountId);
        validateOwnership(account, userId);
        return toAccountResponse(account);
    }

    @Transactional(readOnly = true)
    public BigDecimal getBalance(UUID accountId, UUID userId) {
        Account account = findAccountById(accountId);
        validateOwnership(account, userId);
        return account.getBalance();
    }

    public Account findAccountById(UUID accountId) {
        return accountRepository.findById(accountId)
                .orElseThrow(() -> new RuntimeException("Hesap bulunamadı."));
    }

    public Account findAccountByIban(String iban) {
        return accountRepository.findByIban(iban)
                .orElseThrow(() -> new RuntimeException("IBAN'a ait hesap bulunamadı: " + iban));
    }

    private void validateOwnership(Account account, UUID userId) {
        if (!account.getUser().getId().equals(userId)) {
            throw new RuntimeException("Bu hesaba erişim yetkiniz yok.");
        }
    }

    private String generateUniqueIban() {
        String iban;
        do { iban = ibanGenerator.generate(); }
        while (accountRepository.existsByIban(iban));
        return iban;
    }

    private String generateUniqueAccountNo() {
        String no;
        do { no = accountNumberGenerator.generate(); }
        while (accountRepository.existsByAccountNo(no));
        return no;
    }

    public AccountResponse toAccountResponse(Account a) {
        return new AccountResponse(
                a.getId(),
                a.getUser().getId(),
                a.getIban(),
                a.getAccountNo(),
                a.getBalance(),
                a.getCurrency(),
                a.getStatus(),
                a.getAccountType(),
                a.getCreatedAt()
        );
    }
}
