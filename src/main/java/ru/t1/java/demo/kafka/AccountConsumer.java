package ru.t1.java.demo.kafka;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;
import ru.t1.java.demo.dto.AccountDTO;
import ru.t1.java.demo.model.Account;
import ru.t1.java.demo.service.AccountService;
import ru.t1.java.demo.util.AccountMapper;

import java.util.List;

@Slf4j
@RequiredArgsConstructor
@Component
public class AccountConsumer {

    private final AccountService accountService;

    @KafkaListener(topics = "t1_demo_accounts", containerFactory = "accountKafkaListenerContainerFactory", groupId = "account")
    public void listen(@Payload List<AccountDTO> messageAccountList, Acknowledgment ack) {
        log.info("Received account list");
        try {
            List<Account> accounts = messageAccountList.stream()
                    .map(AccountMapper::toEntity)
                    .toList();
            accountService.createAccounts(accounts);
        }finally {
            ack.acknowledge();
        }

        log.info("Finished listening accounts");
    }
}
