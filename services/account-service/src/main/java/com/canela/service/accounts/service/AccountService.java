package com.canela.service.accounts.service;

import com.canela.service.accounts.model.Account;
import com.canela.service.accounts.repository.AccountRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class AccountService {
    private final AccountRepository repository;

    @Autowired
    public AccountService(AccountRepository repository) {
        this.repository = repository;
    }

    public List<Account> findAllAccounts() {
        return repository.findAll();
    }
}
