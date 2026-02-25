package com.bayraktolga.BayrakBackend.repository;

import com.bayraktolga.BayrakBackend.entity.Account;
import com.bayraktolga.BayrakBackend.enums.AccountStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface AccountRepository extends JpaRepository<Account, UUID> {

    List<Account> findByUserId(UUID userId);

    List<Account> findByUserIdAndStatus(UUID userId, AccountStatus status);

    Optional<Account> findByIban(String iban);

    Optional<Account> findByAccountNo(String accountNo);

    boolean existsByIban(String iban);

    boolean existsByAccountNo(String accountNo);
}
