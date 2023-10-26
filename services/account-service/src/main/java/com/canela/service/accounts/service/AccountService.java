package com.canela.service.accounts.service;

import com.canela.service.accounts.dto.AccountRequestDTO;
import com.canela.service.accounts.exception.AlreadyExistException;
import com.canela.service.accounts.exception.NotFoundException;
import com.canela.service.accounts.model.Account;
import com.canela.service.accounts.repository.AccountRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

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

    public Account findAccountById(String id) {
        Optional<Account> account = repository.findById(id);
        if(account.isEmpty()) {
            throw new NotFoundException("User not found");
        }
        return account.get();
    }

    public Boolean saveAccount(AccountRequestDTO body) {
        repository.save(Account.builder()
                .balance(body.balance())
                .holderId(body.holderId())
                .build());
        return true;
    }
}
