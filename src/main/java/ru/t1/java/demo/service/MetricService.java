package ru.t1.java.demo.service;

import io.micrometer.core.instrument.Gauge;
import io.micrometer.core.instrument.MeterRegistry;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.t1.java.demo.repository.AccountRepository;
import ru.t1.java.demo.repository.ClientRepository;

@Service
public class MetricService {

    private final ClientRepository clientRepository;
    private final AccountRepository accountRepository;
    private final MeterRegistry meterRegistry;

    @Autowired
    public MetricService(ClientRepository clientRepository, AccountRepository accountRepository, MeterRegistry meterRegistry) {
        this.clientRepository = clientRepository;
        this.accountRepository = accountRepository;
        this.meterRegistry = meterRegistry;

        Gauge.builder("blocked.clients.count", clientRepository, ClientRepository::countBlockedClients)
                .description("Количество заблокированных клиентов")
                .register(meterRegistry);

        Gauge.builder("blocked.accounts.count", accountRepository, AccountRepository::countArrestedAccounts)
                .description("Количество арестованных счетов")
                .register(meterRegistry);
    }


}
