package com.canela.service.accounts.dto;

import java.util.Optional;

public record AccountRequestDTO(
        Double balance,
        String holderId
) {
    public AccountRequestDTO(Double balance, String holderId) {
        this.balance = balance == null ? 0.0 : balance;
        this.holderId = holderId;
    }
}