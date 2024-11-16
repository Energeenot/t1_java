package ru.t1.java.demo.util;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import ru.t1.java.demo.dto.TransactionDTO;
import ru.t1.java.demo.model.Account;
import ru.t1.java.demo.model.Transaction;
import ru.t1.java.demo.service.AccountService;

@Component
@Slf4j
public class TransactionMapper {

    public static Transaction toEntity(TransactionDTO dto, AccountService accountService) {
        Account account = accountService.getAccountById(dto.getAccountId());
        return Transaction.builder()
                .transactionAmount(dto.getTransactionAmount())
                .account(account)
                .build();
    }
}
