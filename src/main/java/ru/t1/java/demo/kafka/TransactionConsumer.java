package ru.t1.java.demo.kafka;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;
import ru.t1.java.demo.dto.TransactionDTO;
import ru.t1.java.demo.model.Transaction;
import ru.t1.java.demo.service.TransactionService;
import ru.t1.java.demo.util.TransactionMapper;

import java.util.List;

@Slf4j
@RequiredArgsConstructor
@Component
public class TransactionConsumer {

    private final TransactionService transactionService;

    @KafkaListener(topics = "t1_demo_transactions", containerFactory = "transactionKafkaListenerContainerFactory", groupId = "transaction")
    public void listen(@Payload List<TransactionDTO> transactionsDTO, Acknowledgment ack) {
        log.info("Received transactions list");

        try {
            List<Transaction> transactions = transactionsDTO.stream()
                    .map(TransactionMapper::toEntity)
                    .toList();
            transactionService.createTransactions(transactions);
        }finally {
            ack.acknowledge();
        }
        log.info("Finished listening transactions");
    }
}
