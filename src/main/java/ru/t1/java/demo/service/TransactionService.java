package ru.t1.java.demo.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import ru.t1.java.demo.aop.LogDataSourceError;
import ru.t1.java.demo.dto.TransactionRequestedDTO;
import ru.t1.java.demo.dto.TransactionResultMessageDTO;
import ru.t1.java.demo.model.Account;
import ru.t1.java.demo.model.Client;
import ru.t1.java.demo.model.Transaction;
import ru.t1.java.demo.model.enums.AccountStatus;
import ru.t1.java.demo.model.enums.TransactionStatus;
import ru.t1.java.demo.repository.AccountRepository;
import ru.t1.java.demo.repository.ClientRepository;
import ru.t1.java.demo.repository.TransactionRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@Slf4j
public class TransactionService {

    private final TransactionRepository transactionRepository;
    private final AccountRepository accountRepository;
    private final KafkaTemplate<String, TransactionRequestedDTO> kafkaTemplateTransaction;
    private final ClientRepository clientRepository;

    @Autowired
    public TransactionService(TransactionRepository transactionRepository, AccountRepository accountRepository, KafkaTemplate<String, TransactionRequestedDTO> kafkaTemplate, ClientRepository clientRepository) {
        this.transactionRepository = transactionRepository;
        this.accountRepository = accountRepository;
        this.kafkaTemplateTransaction = kafkaTemplate;
        this.clientRepository = clientRepository;
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

    @Transactional
    public void createTransactions(List<Transaction> transactions) {
        transactions.forEach(transaction -> {
            log.info("Processing transaction {}", transaction);
//            Hibernate.initialize(transaction.getAccount());
            Account account = accountRepository.findById(transaction.getAccount().getId()).get();
//            Hibernate.initialize(account.getClient());
            Client client = clientRepository.findById(account.getClient().getId()).get();
            System.out.println(account + " this account has been processed");
            log.error("Account {}", account);
            log.error("status {}", account.getStatus());
            if (account.getStatus() == AccountStatus.OPEN) {
                log.info("Account {} is open", account);
                transaction.setStatus(TransactionStatus.REQUESTED);
                transaction.setTransactionId(UUID.randomUUID().toString());
                transaction.setTimestamp(LocalDateTime.now());
                transactionRepository.save(transaction);
                account.setBalance(account.getBalance() + transaction.getTransactionAmount());
                accountRepository.save(account);

                TransactionRequestedDTO requestedDTO = TransactionRequestedDTO.builder()
                        .clientId(client.getClientId())
                        .accountId(account.getAccountId())
                        .transactionId(transaction.getTransactionId())
                        .timestamp(transaction.getTimestamp())
                        .transactionAmount(transaction.getTransactionAmount())
                        .accountBalance(account.getBalance())
                        .build();
                kafkaTemplateTransaction.send("t1_demo_transaction_accept", requestedDTO);
            }
        });
    }

    public void processingResult(TransactionResultMessageDTO resultMessageDTO) {
        log.info("Processing result {}", resultMessageDTO);
        Transaction transaction = transactionRepository.findByTransactionId(resultMessageDTO.getTransactionId());
        switch (resultMessageDTO.getStatus()) {
            case "ACCEPTED" -> {
                log.info("Transaction {} accepted", transaction);
                transaction.setStatus(TransactionStatus.ACCEPTED);
                transactionRepository.save(transaction);
            }
            case "BLOCKED" -> {
                log.info("Transaction {} blocked", transaction);
                transaction.setStatus(TransactionStatus.BLOCKED);
                transactionRepository.save(transaction);
                Account account = accountRepository.findByAccountId(resultMessageDTO.getAccountId());
                account.setBalance(account.getBalance() - transaction.getTransactionAmount());
                account.setFrozenAmount(account.getFrozenAmount() + transaction.getTransactionAmount());
                accountRepository.save(account);
            }
            case "REJECTED" -> {
                log.info("Transaction {} rejected", transaction);
                transaction.setStatus(TransactionStatus.REJECTED);
                transactionRepository.save(transaction);
                Account account = accountRepository.findByAccountId(resultMessageDTO.getAccountId());
                account.setBalance(account.getBalance() - transaction.getTransactionAmount());
                accountRepository.save(account);
            }
        }
    }

//    protected void processingTransaction(Transaction transaction) {
//        log.info("Processing transaction {}", transaction);
//        Hibernate.initialize(transaction.getAccount());
//        Account account = transaction.getAccount();
//        Hibernate.initialize(account.getClient());
//        System.out.println(account + " this account has been processed");
//        log.error("Account {}", account);
//        log.error("status {}", account.getStatus());
//        if (account.getStatus() == AccountStatus.OPEN) {
//            log.info("Account {} is open", account);
//            transaction.setStatus(TransactionStatus.REQUESTED);
//            transaction.setTransactionId(UUID.randomUUID().toString());
//            transactionRepository.save(transaction);
//            account.setBalance(account.getBalance() + transaction.getTransactionAmount());
//            accountRepository.save(account);
//            TransactionRequestedDTO requestedDTO = TransactionRequestedDTO.builder()
//                    .clientId(account.getClient().getClientId())
//                    .accountId(account.getAccountId())
//                    .transactionId(transaction.getTransactionId())
//                    .timestamp(transaction.getTimestamp())
//                    .transactionAmount(transaction.getTransactionAmount())
//                    .accountBalance(account.getBalance())
//                    .build();
//            kafkaTemplateTransaction.send("t1_demo_transaction_accept", requestedDTO);
//        }
//    }
}
