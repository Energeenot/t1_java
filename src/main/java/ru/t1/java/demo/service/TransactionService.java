package ru.t1.java.demo.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import ru.t1.java.demo.aop.LogDataSourceError;
import ru.t1.java.demo.model.Transaction;
import ru.t1.java.demo.repository.TransactionRepository;

import java.util.List;

@Service
@Slf4j
public class TransactionService {

    private final TransactionRepository transactionRepository;

    @Autowired
    public TransactionService(TransactionRepository transactionRepository) {
        this.transactionRepository = transactionRepository;
    }

    @LogDataSourceError
    public Transaction createTransaction(Transaction transaction) {
        log.info("Create transaction");
        return transactionRepository.save(transaction);
    }

    @LogDataSourceError
    public Transaction getTransaction(long id) {
        log.info("Get transaction with id {}", id);
        return transactionRepository.findById(id).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Transaction not found"));
    }

    @LogDataSourceError
    public List<Transaction> getAllTransactions() {
        log.info("Get all transactions");
        return transactionRepository.findAll();
    }

//    обновление транзакции?
//    @LogDataSourceError
//    public Transaction updateTransaction(Transaction updatedTransaction, long id) {
//        log.info("Update transaction with id {}", id);
//        Transaction transaction = transactionRepository.findById(id).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Transaction not found"));
//        Optional.ofNullable(updatedTransaction.getTransactionAmount()).ifPresent(transaction::setTransactionAmount);
//        return transactionRepository.save(transaction);
//    }

    @LogDataSourceError
    public void deleteTransaction(long id) {
        log.info("Delete transaction with id {}", id);
        transactionRepository.deleteById(id);
    }

    public void createTransactions(List<Transaction> transactions) {
        transactions.forEach(this::createTransaction);
    }
}
