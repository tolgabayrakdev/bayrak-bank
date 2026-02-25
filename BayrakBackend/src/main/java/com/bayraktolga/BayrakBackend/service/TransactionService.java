package com.bayraktolga.BayrakBackend.service;

import com.bayraktolga.BayrakBackend.dto.request.TransferRequest;
import com.bayraktolga.BayrakBackend.dto.response.PageResponse;
import com.bayraktolga.BayrakBackend.dto.response.TransactionResponse;
import com.bayraktolga.BayrakBackend.entity.Account;
import com.bayraktolga.BayrakBackend.entity.Transaction;
import com.bayraktolga.BayrakBackend.enums.AccountStatus;
import com.bayraktolga.BayrakBackend.enums.NotificationType;
import com.bayraktolga.BayrakBackend.enums.TransactionStatus;
import com.bayraktolga.BayrakBackend.enums.TransactionType;
import com.bayraktolga.BayrakBackend.repository.AccountRepository;
import com.bayraktolga.BayrakBackend.repository.TransactionRepository;
import com.bayraktolga.BayrakBackend.util.ReferenceNumberGenerator;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class TransactionService {

    private final TransactionRepository transactionRepository;
    private final AccountRepository accountRepository;
    private final AccountService accountService;
    private final NotificationService notificationService;
    private final EmailService emailService;
    private final ReferenceNumberGenerator referenceNumberGenerator;

    @Transactional
    public TransactionResponse transfer(UUID userId, TransferRequest request) {
        if (request.senderIban().equals(request.receiverIban())) {
            throw new RuntimeException("Gönderen ve alıcı hesap aynı olamaz.");
        }

        Account sender   = accountService.findAccountByIban(request.senderIban());
        Account receiver = accountService.findAccountByIban(request.receiverIban());

        if (!sender.getUser().getId().equals(userId)) {
            throw new RuntimeException("Bu hesaptan transfer yapma yetkiniz yok.");
        }
        if (sender.getStatus()   == AccountStatus.BLOCKED)  throw new RuntimeException("Gönderen hesap bloke edilmiş.");
        if (sender.getStatus()   == AccountStatus.PASSIVE)  throw new RuntimeException("Gönderen hesap pasif durumda.");
        if (receiver.getStatus() == AccountStatus.BLOCKED)  throw new RuntimeException("Alıcı hesap bloke edilmiş.");
        if (receiver.getStatus() == AccountStatus.PASSIVE)  throw new RuntimeException("Alıcı hesap pasif durumda.");

        if (sender.getBalance().compareTo(request.amount()) < 0) {
            throw new RuntimeException("Yetersiz bakiye.");
        }

        boolean isInternal = sender.getUser().getId().equals(receiver.getUser().getId());
        TransactionType type = isInternal ? TransactionType.TRANSFER : TransactionType.EFT;

        sender.setBalance(sender.getBalance().subtract(request.amount()));
        receiver.setBalance(receiver.getBalance().add(request.amount()));
        accountRepository.save(sender);
        accountRepository.save(receiver);

        String referenceNo = generateUniqueReferenceNo();
        Transaction transaction = Transaction.builder()
                .referenceNo(referenceNo)
                .senderAccount(sender)
                .receiverAccount(receiver)
                .amount(request.amount())
                .currency(sender.getCurrency())
                .type(type)
                .status(TransactionStatus.COMPLETED)
                .description(request.description())
                .build();

        Transaction saved = transactionRepository.save(transaction);

        notificationService.createNotification(
                sender.getUser(),
                "Transfer Gönderildi",
                request.amount() + " TL transfer edildi. Alıcı: " + receiver.getIban() + " | Ref: " + referenceNo,
                NotificationType.TRANSFER
        );
        if (!isInternal) {
            notificationService.createNotification(
                    receiver.getUser(),
                    "Para Transferi Alındı",
                    request.amount() + " TL alındı. Gönderen: " + sender.getIban() + " | Ref: " + referenceNo,
                    NotificationType.TRANSFER
            );
        }

        emailService.sendTransferEmail(
                sender.getUser().getEmail(),
                sender.getUser().getFirstName(),
                referenceNo,
                request.amount().toPlainString()
        );

        return toTransactionResponse(saved);
    }

    @Transactional(readOnly = true)
    public PageResponse<TransactionResponse> getTransactionsByUser(UUID userId, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<Transaction> txPage = transactionRepository.findByUserId(userId, pageable);
        return toPageResponse(txPage);
    }

    @Transactional(readOnly = true)
    public PageResponse<TransactionResponse> getTransactionsByAccount(UUID accountId, UUID userId, int page, int size) {
        Account account = accountService.findAccountById(accountId);
        if (!account.getUser().getId().equals(userId)) {
            throw new RuntimeException("Bu hesabın işlem geçmişine erişim yetkiniz yok.");
        }
        Pageable pageable = PageRequest.of(page, size);
        Page<Transaction> txPage = transactionRepository.findByAccountId(accountId, pageable);
        return toPageResponse(txPage);
    }

    @Transactional(readOnly = true)
    public TransactionResponse getTransactionById(UUID transactionId, UUID userId) {
        Transaction tx = transactionRepository.findById(transactionId)
                .orElseThrow(() -> new RuntimeException("İşlem bulunamadı."));

        boolean isSender   = tx.getSenderAccount().getUser().getId().equals(userId);
        boolean isReceiver = tx.getReceiverAccount().getUser().getId().equals(userId);
        if (!isSender && !isReceiver) {
            throw new RuntimeException("Bu işleme erişim yetkiniz yok.");
        }
        return toTransactionResponse(tx);
    }

    private String generateUniqueReferenceNo() {
        String refNo;
        do { refNo = referenceNumberGenerator.generate(); }
        while (transactionRepository.existsByReferenceNo(refNo));
        return refNo;
    }

    private PageResponse<TransactionResponse> toPageResponse(Page<Transaction> page) {
        return new PageResponse<>(
                page.getContent().stream().map(this::toTransactionResponse).toList(),
                page.getNumber(),
                page.getSize(),
                page.getTotalElements(),
                page.getTotalPages(),
                page.isLast()
        );
    }

    private TransactionResponse toTransactionResponse(Transaction t) {
        return new TransactionResponse(
                t.getId(),
                t.getReferenceNo(),
                t.getSenderAccount().getIban(),
                t.getReceiverAccount().getIban(),
                t.getAmount(),
                t.getCurrency(),
                t.getType(),
                t.getStatus(),
                t.getDescription(),
                t.getCreatedAt()
        );
    }
}
