package ru.t1.java.demo.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.t1.java.demo.aop.Metric;
import ru.t1.java.demo.dto.TransactionDTO;
import ru.t1.java.demo.model.Transaction;
import ru.t1.java.demo.service.TransactionService;

import java.util.List;

@RestController
@Slf4j
@RequestMapping("/transaction")
public class TransactionController {

    private final TransactionService transactionService;

    @Autowired
    public TransactionController(TransactionService transactionService) {
        this.transactionService = transactionService;
    }

    @GetMapping
    @ResponseStatus(HttpStatus.OK)
    @Metric(limit = 100)
    public List<Transaction> getAllTransactions() {
        log.info("get all transactions");
        return transactionService.getAllTransactions();
    }

    @GetMapping("/{id}")
    public ResponseEntity<Transaction> getTransactionById(@PathVariable long id) {
        log.info("get transaction by id: {}", id);
        Transaction transaction = transactionService.getTransaction(id);
        return ResponseEntity.ok(transaction);
    }

    @PostMapping("")
    @ResponseStatus(HttpStatus.CREATED)
    public Transaction createTransaction(@RequestBody TransactionDTO transactionDTO) {
        log.info("create transaction: {}", transactionDTO);
        return transactionService.createTransaction(transactionDTO);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<HttpStatus> deleteTransaction(@PathVariable long id) {
        log.info("delete transaction by id: {}", id);
        transactionService.deleteTransaction(id);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }


}
