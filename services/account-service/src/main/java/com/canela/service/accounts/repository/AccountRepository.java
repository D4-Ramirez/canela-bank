package com.canela.service.accounts.repository;

import com.canela.service.accounts.model.Account;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AccountRepository extends JpaRepository<Account, String> {
}
