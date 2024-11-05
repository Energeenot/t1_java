package ru.t1.java.demo.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.t1.java.demo.model.Account;
import ru.t1.java.demo.service.AccountService;

import java.util.List;

@RestController
@Slf4j
@RequestMapping("/account")
public class AccountController {

    private final AccountService accountService;

    @Autowired
    public AccountController(AccountService accountService) {
        this.accountService = accountService;
    }

    @GetMapping()
    @ResponseStatus(HttpStatus.OK)
    public List<Account> getAllAccount(){
        log.info("get all account");
        return accountService.getAllAccounts();
    }

    @GetMapping("/{id}")
    public ResponseEntity<Account> getAccountById(@PathVariable long id){
        log.info("get account by id: {}", id);
        Account account = accountService.getAccountById(id);
        return ResponseEntity.ok(account);
    }

    @PostMapping()
    @ResponseStatus(HttpStatus.CREATED)
    public Account createAccount(@RequestBody Account account){
        log.info("create account: {}", account);
        return accountService.createAccount(account);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Account> updateAccount(@PathVariable long id, @RequestBody Account account){
        log.info("update account: {}", account);
        accountService.updateAccount(account, id);
        return ResponseEntity.ok(account);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<HttpStatus> deleteAccount(@PathVariable long id){
        log.info("delete account: {}", id);
        accountService.deleteAccountById(id);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }
}
