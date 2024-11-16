package ru.t1.java.demo.kafka;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;
import ru.t1.java.demo.dto.TransactionDTO;
import ru.t1.java.demo.dto.TransactionResultMessageDTO;
import ru.t1.java.demo.model.Transaction;
import ru.t1.java.demo.service.AccountService;
import ru.t1.java.demo.service.TransactionService;
import ru.t1.java.demo.util.TransactionMapper;

import java.util.List;

@Slf4j
@RequiredArgsConstructor
@Component
public class TransactionConsumer {

    private final TransactionService transactionService;
    private final AccountService accountService;

    @KafkaListener(topics = "t1_demo_transactions", containerFactory = "transactionKafkaListenerContainerFactory", groupId = "transaction")
    public void listen(@Payload List<TransactionDTO> transactionsDTO, Acknowledgment ack) {
        log.info("Received transactions list");

        try {
            log.info("go in try");
            List<Transaction> transactions = transactionsDTO.stream()
                    .map(dto -> TransactionMapper.toEntity(dto, accountService))
                    .toList();
            log.info("Received {} transactions", transactions.size());
            transactionService.createTransactions(transactions);
        }finally {
            ack.acknowledge();
        }
        log.info("Finished listening transactions");
    }

    @KafkaListener(topics = "t1_demo_transaction_result", containerFactory = "transactionResultKafkaListenerContainerFactory", groupId = "transaction-result")
    public void listenResult(@Payload TransactionResultMessageDTO resultMessageDTO, Acknowledgment ack){
        log.info("Received transaction result");
        try {
            transactionService.processingResult(resultMessageDTO);
        }finally {
            ack.acknowledge();
        }
    }
}
