package ru.t1.java.demo.service;

import jakarta.persistence.EntityNotFoundException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import ru.t1.java.demo.aop.LogDataSourceError;
import ru.t1.java.demo.dto.TransactionDTO;
import ru.t1.java.demo.dto.TransactionRequestedDTO;
import ru.t1.java.demo.dto.TransactionResultMessageDTO;
import ru.t1.java.demo.model.Account;
import ru.t1.java.demo.model.Client;
import ru.t1.java.demo.model.Transaction;
import ru.t1.java.demo.model.enums.AccountStatus;
import ru.t1.java.demo.model.enums.ClientStatus;
import ru.t1.java.demo.model.enums.TransactionStatus;
import ru.t1.java.demo.repository.AccountRepository;
import ru.t1.java.demo.repository.ClientRepository;
import ru.t1.java.demo.repository.TransactionRepository;
import ru.t1.java.demo.util.TransactionMapper;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
@Slf4j
public class TransactionService {

    private final TransactionRepository transactionRepository;
    private final AccountRepository accountRepository;
    private final KafkaTemplate<String, TransactionRequestedDTO> kafkaTemplateTransaction;
    private final ClientRepository clientRepository;
    private final ClientStatusService clientStatusService;
    private final AccountService accountService;

    @Value("${transaction.max-count}")
    private int maxCount;

    @Autowired
    public TransactionService(TransactionRepository transactionRepository, AccountRepository accountRepository, KafkaTemplate<String, TransactionRequestedDTO> kafkaTemplate, ClientRepository clientRepository, ClientStatusService clientStatusService, AccountService accountService) {
        this.transactionRepository = transactionRepository;
        this.accountRepository = accountRepository;
        this.kafkaTemplateTransaction = kafkaTemplate;
        this.clientRepository = clientRepository;
        this.clientStatusService = clientStatusService;
        this.accountService = accountService;
    }

    @LogDataSourceError
    public Transaction createTransaction(TransactionDTO transactionDTO) {
        Transaction transaction = TransactionMapper.toEntity(transactionDTO, accountService);
        transaction.setTransactionId(UUID.randomUUID().toString());
        Account account = accountRepository.findById(transaction.getAccount().getId())
                .orElseThrow(() -> new EntityNotFoundException("Account not found"));
        Client client = clientRepository.findById(account.getClient().getId())
                .orElseThrow(() -> new EntityNotFoundException("Client not found"));

        if (client.getStatus() == null || client.getStatus() == ClientStatus.UNKNOWN){
            handleUnknownClientStatus(client, account, transaction);
        }
        if (client.getStatus() == ClientStatus.BLOCKED){
            log.info("Client is BLOCKED, rejecting transaction");
            transaction.setStatus(TransactionStatus.REJECTED);
        }
        checkRejectedTransactionsThreshold(account, client, transaction);
        return transactionRepository.save(transaction);
    }

    private void checkRejectedTransactionsThreshold(Account account, Client client, Transaction transaction) {
        List<Transaction> rejectedTransactions = transactionRepository.findByStatusAndAccountId(TransactionStatus.REJECTED, account.getId());
        log.info("Found {} rejected transactions for account {}", rejectedTransactions.size(), account.getId());

        if (rejectedTransactions.size() > maxCount && client.getStatus() != ClientStatus.UNKNOWN){
            log.info("max count is reached and account will be arrested");
            account.setStatus(AccountStatus.ARRESTED);
            accountRepository.save(account);
            transaction.setStatus(TransactionStatus.REJECTED);
        }
    }

    private void handleUnknownClientStatus(Client client, Account account, Transaction transaction) {
        log.info("Client {} status is unknown", client);
        Map<String, String> response = clientStatusService.sendRequestToService2(client.getId(), account.getId());
        log.info("Client {} request is {}", client, response);
        String statusValue = response.get("status");
        ClientStatus status = ClientStatus.valueOf(statusValue);
        System.out.println(status + " status is");
        if (status == ClientStatus.BLOCKED){
            blockClientAndAccount(client, account);
            transaction.setStatus(TransactionStatus.REJECTED);
        }
    }

    private void blockClientAndAccount(Client client, Account account) {
        log.info("Blocking client {} and account {}", client.getId(), account.getId());
        client.setStatus(ClientStatus.BLOCKED);
        account.setStatus(AccountStatus.BLOCKED);
        clientRepository.save(client);
        accountRepository.save(account);
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
            Account account = accountRepository.findById(transaction.getAccount().getId()).get();
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
}
