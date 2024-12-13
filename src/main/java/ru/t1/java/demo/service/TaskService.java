package ru.t1.java.demo.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpMethod;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import ru.t1.java.demo.model.Account;
import ru.t1.java.demo.model.Client;
import ru.t1.java.demo.model.enums.AccountStatus;
import ru.t1.java.demo.model.enums.ClientStatus;
import ru.t1.java.demo.repository.AccountRepository;
import ru.t1.java.demo.repository.ClientRepository;
import org.springframework.http.ResponseEntity;

import java.util.List;

@Service
@Slf4j
public class TaskService {

    private final ClientRepository clientRepository;
    private final RestTemplate restTemplate;
    private final AccountRepository accountRepository;
    @Value("${unblocking.number-of-clients.N}")
    private int numberOfUnblockingClients;
    @Value("${unblocking.number-of-accounts.M}")
    private int numberOfUnblockingAccounts;
    @Value("${unblocking.service.url}")
    private String unblockingUrl;

    @Autowired
    public TaskService(ClientRepository clientRepository, RestTemplate restTemplate, AccountRepository accountRepository) {
        this.clientRepository = clientRepository;
        this.restTemplate = restTemplate;
        this.accountRepository = accountRepository;
    }

    @Scheduled(fixedRateString = "${unblocking.period.T}")
    public void clientUnblockRequest(){
        log.info("time to unblock clients");
        List<Client> blockedClients = clientRepository.findBlockedClientsWithLimit(ClientStatus.BLOCKED.toString(), numberOfUnblockingClients);

        for(Client blockedClient : blockedClients){
            log.info("Client {}  try to unlock", blockedClient);
            ResponseEntity<String> response = restTemplate.exchange(
                    unblockingUrl + "/unlock-client" + "?clientId=" + blockedClient.getId(),
                    HttpMethod.GET,
                    null,
                    String.class
            );
        }
    }

    @Scheduled(fixedRateString = "${unblocking.period.T}")
    public void accountUnblockRequest(){
        log.info("time to liberate accounts");
        List<Account> blockedAccounts = accountRepository.findArrestedAccountsWithLimit(AccountStatus.ARRESTED.toString(), numberOfUnblockingAccounts);

        for(Account blockedAccount : blockedAccounts){
            log.info("Account {}  try to liberate", blockedAccount);
            ResponseEntity<String> response = restTemplate.exchange(
                    unblockingUrl + "/unlock-account" + "?accountId=" + blockedAccount.getId(),
                    HttpMethod.GET,
                    null,
                    String.class
            );
        }
    }
}
