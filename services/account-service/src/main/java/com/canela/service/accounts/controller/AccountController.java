package com.canela.service.accounts.controller;

import com.canela.service.accounts.dto.AccountRequestDTO;
import com.canela.service.accounts.exception.AccountAdvisor;
import com.canela.service.accounts.model.Account;
import com.canela.service.accounts.service.AccountService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/accounts")
public class AccountController {
    private final AccountService service;

    @Autowired
    public AccountController(AccountService service) {
        this.service = service;
    }

    @Operation(
            summary = "Find all accounts", description = "Retrieves all the bank accounts information",
            tags = {"Account"}
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Accounts retrieved")
    })
    @GetMapping
    public ResponseEntity<List<Account>> findAllAccounts() {
        return ResponseEntity.status(HttpStatus.OK).body(service.findAllAccounts());
    }

    @Operation(
            summary = "Find account by id", description = "Retrieves the bank account identified by id",
            tags = {"Account"}
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Account retrieved"),
            @ApiResponse(responseCode = "404", description = "Account doesn't exists", content = @Content(
                    schema = @Schema(implementation = AccountAdvisor.class)
            ))
    })
    @GetMapping("/{id}")
    public ResponseEntity<Account> findAccountById(@PathVariable("id") String id) {
        return ResponseEntity.status(HttpStatus.OK).body(service.findAccountById(id));
    }

    @Operation(
            summary = "Create account", description = "Creates a new account",
            tags = {"Account"}
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Account created"),
            @ApiResponse(responseCode = "409", description = "Account already exists", content = @Content(
                    schema = @Schema(implementation = AccountAdvisor.class)
            ))
    })
    @PostMapping
    public ResponseEntity<Boolean> saveAccount(@RequestBody AccountRequestDTO body) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.saveAccount(body));
    }
}
