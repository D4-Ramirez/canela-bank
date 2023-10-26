package com.canela.service.accounts.repository;

import com.canela.service.accounts.model.Account;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;

public interface AccountRepository extends JpaRepository<Account, String> {
    @Query(value = "SELECT * FROM accounts WHERE number = ?", nativeQuery = true)
    Optional<Account> findByNumber(String number);

    @Query(value = "SELECT * FROM accounts WHERE holder_id = ?", nativeQuery = true)
    Optional<Account> findByHolderId(String id);
}
