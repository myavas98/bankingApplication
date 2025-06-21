package com.simplebanking.services;

import com.simplebanking.config.KafkaTopicConfig;
import com.simplebanking.events.AccountCreatedEvent;
import com.simplebanking.events.AccountCreditedEvent;
import com.simplebanking.events.AccountDebitedEvent;
import com.simplebanking.model.*;
import com.simplebanking.repository.AccountRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;
import com.simplebanking.model.TransactionStatus;


@Service
public class AccountService {

    @Autowired
    private AccountRepository accountRepository;

    @Autowired
    private KafkaTemplate<String, Object> kafkaTemplate;

    public Account findAccount(String accountNumber) {
        return accountRepository.findById(accountNumber)
                .orElse(null);
    }

    @Transactional
    public Account save(Account account) {
        boolean isNewAccount = !accountRepository.existsById(account.getAccountNumber());
        Account savedAccount = accountRepository.save(account);
        if (isNewAccount) {
            kafkaTemplate.send(KafkaTopicConfig.ACCOUNT_EVENTS_TOPIC, 
                               new AccountCreatedEvent(savedAccount.getAccountNumber(), savedAccount.getOwner()));
        }
        return savedAccount;
    }

    @Transactional
    public TransactionStatus credit(String accountNumber, double amount) throws InsufficientBalanceException {
        Account account = findAccount(accountNumber);
        if (account == null) {
            return new TransactionStatus("ERROR", "Account not found");
        }
        account.deposit(amount);
        accountRepository.save(account);
        kafkaTemplate.send(KafkaTopicConfig.ACCOUNT_EVENTS_TOPIC, 
                           new AccountCreditedEvent(account.getAccountNumber(), amount));
        return new TransactionStatus("OK", UUID.randomUUID().toString());
    }

    @Transactional
    public TransactionStatus debit(String accountNumber, double amount) throws InsufficientBalanceException {
        Account account = findAccount(accountNumber);
        if (account == null) {
            return new TransactionStatus("ERROR", "Account not found");
        }
        account.withdraw(amount);
        accountRepository.save(account);
        kafkaTemplate.send(KafkaTopicConfig.ACCOUNT_EVENTS_TOPIC, 
                           new AccountDebitedEvent(account.getAccountNumber(), amount));
        return new TransactionStatus("OK", UUID.randomUUID().toString());
    }
}
