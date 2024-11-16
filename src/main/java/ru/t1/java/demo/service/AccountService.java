package ru.t1.java.demo.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import ru.t1.java.demo.aop.LogDataSourceError;
import ru.t1.java.demo.model.Account;
import ru.t1.java.demo.repository.AccountRepository;

import java.util.List;
import java.util.Optional;

@Service
@Slf4j
public class AccountService {

    public final AccountRepository accountRepository;

    @Autowired
    public AccountService(AccountRepository accountRepository) {
        this.accountRepository = accountRepository;
    }

    @LogDataSourceError
    public Account createAccount(Account account) {
        log.debug("Call method createAccount");
        return accountRepository.save(account);
    }

    @LogDataSourceError
    public Account getAccountById(long id) {
        log.debug("Call method getAccountById {}", id);
        return accountRepository.findById(id).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Account with id " + id + " not found"));
    }

    public List<Account> getAllAccounts() {
        log.debug("Call method getAllAccounts");
        return accountRepository.findAll();
    }

    @LogDataSourceError
    public Account updateAccount(Account updatedAccount, long id) {
        log.debug("Call method updateAccount with id {}", id);
        Account oldAccount = accountRepository.findById(id).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Account not found"));
        Optional.ofNullable(updatedAccount.getAccountType()).ifPresent(oldAccount::setAccountType);
        Optional.ofNullable(updatedAccount.getBalance()).ifPresent(oldAccount::setBalance);
        return accountRepository.save(oldAccount);
    }

    @LogDataSourceError
    public void deleteAccountById(long id) {
        log.debug("Call method deleteAccountById {}", id);
        accountRepository.deleteById(id);
    }

    public void createAccounts(List<Account> accounts) {
        accounts.forEach(this::createAccount);
    }
}
