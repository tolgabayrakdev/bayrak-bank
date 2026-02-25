package com.bayraktolga.BayrakBackend.repository;

import com.bayraktolga.BayrakBackend.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface UserRepository extends JpaRepository<User, UUID> {

    Optional<User> findByEmail(String email);

    Optional<User> findByTcNo(String tcNo);

    boolean existsByEmail(String email);

    boolean existsByTcNo(String tcNo);
}
