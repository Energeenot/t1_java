package ru.t1.java.demo.service;

import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.t1.java.demo.dto.TransactionDTO;
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

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TransactionServiceTest {

    @InjectMocks
    private TransactionService transactionService;

    @Mock
    private TransactionRepository transactionRepository;

    @Mock
    private AccountRepository accountRepository;

    @Mock
    private ClientRepository clientRepository;

    @Mock
    private AccountService accountService;

    @Test
    void createTransactionShouldThrowAccountNotFoundException() {
        TransactionDTO transactionDTO = new TransactionDTO();
        Account account = new Account();
        account.setId(1L);

        when(accountService.getAccountById(anyLong())).thenReturn(account);
        when(accountRepository.findById(anyLong())).thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class, () -> transactionService.createTransaction(transactionDTO));
        verify(accountRepository, times(1)).findById(anyLong());
    }

    @Test
    void createTransactionShouldRejectTransactionBecauseClientBlocked() {
        TransactionDTO transactionDTO = new TransactionDTO();
        transactionDTO.setAccountId(1L);
        Account account = new Account();
        account.setId(1L);
        Client client = new Client();
        client.setId(1L);
        client.setStatus(ClientStatus.BLOCKED);
        account.setClient(client);

        when(accountRepository.findById(anyLong())).thenReturn(Optional.of(account));
        when(clientRepository.findById(anyLong())).thenReturn(Optional.of(client));
        when(accountService.getAccountById(anyLong())).thenReturn(account);
        when(transactionRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        Transaction result = transactionService.createTransaction(transactionDTO);
        assertEquals(TransactionStatus.REJECTED, result.getStatus());
    }

    @Test
    void createTransactionShouldArrestAccount(){
        TransactionDTO transactionDTO = new TransactionDTO();
        Account account = new Account();
        account.setId(1L);
        account.setStatus(AccountStatus.OPEN);
        Client client = new Client();
        client.setId(1L);
        client.setStatus(ClientStatus.ACTIVE);
        account.setClient(client);
        Transaction transaction = new Transaction();
        transaction.setAccount(account);

        when(accountRepository.findById(anyLong())).thenReturn(Optional.of(account));
        when(clientRepository.findById(anyLong())).thenReturn(Optional.of(client));
        when(accountService.getAccountById(anyLong())).thenReturn(account);
        when(transactionRepository.findByStatusAndAccountId(TransactionStatus.REJECTED, account.getId()))
                .thenReturn(List.of(new Transaction(), new Transaction(), new Transaction()));
        when(transactionRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        Transaction result = transactionService.createTransaction(transactionDTO);
        assertEquals(TransactionStatus.REJECTED, result.getStatus());
        assertEquals(AccountStatus.ARRESTED, account.getStatus());
        verify(accountRepository, times(1)).save(account);
    }

    @Test
    void processingResultShouldSetAcceptedStatus(){
        TransactionResultMessageDTO resultMessageDTO = TransactionResultMessageDTO.builder()
                .transactionId("test")
                .status("ACCEPTED")
//                .accountId("1")
                .build();
        Transaction transaction = Transaction.builder()
                .transactionId("test")
//                .status(TransactionStatus.ACCEPTED)
                .build();
        Account account = new Account();
        account.setAccountId("1");

        when(transactionRepository.findByTransactionId("test")).thenReturn(transaction);
        when(transactionRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        transactionService.processingResult(resultMessageDTO);

        assertEquals(TransactionStatus.ACCEPTED, transaction.getStatus());
        verify(transactionRepository, times(1)).save(transaction);
        verify(accountRepository, never()).save(any(Account.class));
    }

    @Test
    void processingResultShouldUpdateTransactionAndAccountForBlockedStatus(){
        TransactionResultMessageDTO resultMessageDTO = TransactionResultMessageDTO.builder()
                .transactionId("test")
                .status("BLOCKED")
                .accountId("1")
                .build();
        Transaction transaction = Transaction.builder()
                .transactionId("test")
                .transactionAmount(100.0)
                .build();

        Account account = Account.builder()
                .accountId("1")
                .balance(1000.0)
                .frozenAmount(0.0)
                .build();

        when(transactionRepository.findByTransactionId("test")).thenReturn(transaction);
        when(transactionRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));
        when(accountRepository.findByAccountId("1")).thenReturn(account);

        transactionService.processingResult(resultMessageDTO);

        assertEquals(TransactionStatus.BLOCKED, transaction.getStatus());
        verify(transactionRepository, times(1)).save(transaction);
        assertEquals(900, account.getBalance());
        assertEquals(100, account.getFrozenAmount());
        verify(accountRepository, times(1)).save(account);
    }

    @Test
    void processingResultShouldUpdateTransactionAndClientForRejectedStatus(){
        TransactionResultMessageDTO resultMessageDTO = TransactionResultMessageDTO.builder()
                .transactionId("test")
                .status("REJECTED")
                .accountId("1")
                .build();
        Transaction transaction = Transaction.builder()
                .transactionId("test")
                .transactionAmount(100.0)
                .build();

        Account account = Account.builder()
                .accountId("1")
                .balance(1000.0)
                .frozenAmount(0.0)
                .build();

        when(transactionRepository.findByTransactionId("test")).thenReturn(transaction);
        when(transactionRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));
        when(accountRepository.findByAccountId("1")).thenReturn(account);

        transactionService.processingResult(resultMessageDTO);

        assertEquals(TransactionStatus.REJECTED, transaction.getStatus());
        verify(transactionRepository, times(1)).save(transaction);
        assertEquals(900, account.getBalance());
        verify(accountRepository, times(1)).save(account);
    }

}