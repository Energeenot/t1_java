package ru.t1.java.demo.util;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.t1.java.demo.dto.TransactionDTO;
import ru.t1.java.demo.model.Account;
import ru.t1.java.demo.model.Transaction;
import ru.t1.java.demo.service.AccountService;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class TransactionMapperTest {

    @Mock
    private AccountService accountService;

    @Test
    void toEntityShouldMapDtoToEntityCorrectly() {
        TransactionDTO dto = TransactionDTO.builder()
                .accountId(1L)
                .transactionAmount(100.0)
                .build();

        Account account = new Account();
        account.setId(1L);

        when(accountService.getAccountById(1L)).thenReturn(account);

        Transaction transaction = TransactionMapper.toEntity(dto, accountService);

        verify(accountService, times(1)).getAccountById(1L);
        assertEquals(100.0, transaction.getTransactionAmount());
        assertEquals(account, transaction.getAccount());
    }
}
