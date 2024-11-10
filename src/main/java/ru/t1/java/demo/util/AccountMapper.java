package ru.t1.java.demo.util;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import ru.t1.java.demo.dto.AccountDTO;
import ru.t1.java.demo.model.Account;

@Component
@Slf4j
public class AccountMapper {

    public static Account toEntity(AccountDTO dto) {
        return Account.builder()
                .accountType(dto.getAccountType())
                .balance(dto.getBalance())
                .build();
    }

}
