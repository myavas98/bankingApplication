package com.simplebanking.controller;

import com.simplebanking.model.*;
import com.simplebanking.services.AccountService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;


@RestController
@RequestMapping("/account/v1")
public class AccountController {

    @Autowired
    private AccountService accountService;

    @GetMapping("/{accountNumber}")
    public ResponseEntity<Account> getAccount(@PathVariable String accountNumber) {
        Account account = accountService.findAccount(accountNumber);
        if (account == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(account);
    }

    @PostMapping("/credit/{accountNumber}")
    public ResponseEntity<TransactionStatus> credit(@PathVariable String accountNumber, @RequestBody DepositTransaction deposit) {
        Account account = accountService.findAccount(accountNumber);
        if (account == null) {
            return ResponseEntity.notFound().build();
        }
        
        account.deposit(deposit.getAmount());
        
        try {
            TransactionStatus status = accountService.credit(accountNumber, deposit.getAmount());
            return ResponseEntity.ok(status);
        } catch (InsufficientBalanceException e) { 
            return ResponseEntity.badRequest().body(new TransactionStatus("ERROR", e.getMessage()));
        }
    }

    @PostMapping("/debit/{accountNumber}")
    public ResponseEntity<TransactionStatus> debit(@PathVariable String accountNumber, @RequestBody WithdrawalTransaction withdrawal) {
        
        try {
            TransactionStatus status = accountService.debit(accountNumber, withdrawal.getAmount());
            return ResponseEntity.ok(status);
        } catch (InsufficientBalanceException e) {
            return ResponseEntity.badRequest().body(new TransactionStatus("ERROR", e.getMessage()));
        }
    }

    @PostMapping("/create")
    public ResponseEntity<Account> createAccount(@RequestBody AccountRequest request) {
        Account account = new Account(request.getOwner(), request.getAccountNumber());
        Account savedAccount = accountService.save(account); 
        return ResponseEntity.ok(savedAccount);
    }

}
