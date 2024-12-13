package ru.t1.java.demo.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import ru.t1.java.demo.model.Account;

import java.util.List;

public interface AccountRepository extends JpaRepository<Account, Long> {
    Account findByAccountId(String accountId);

    @Query(value = "SELECT * FROM account WHERE status = :status LIMIT :limit", nativeQuery = true)
    List<Account> findArrestedAccountsWithLimit(@Param("status") String status,@Param("limit") int numberOfUnblockingAccounts);

    @Query("SELECT COUNT(a) FROM Account a WHERE a.status = 'ARRESTED'")
    long countArrestedAccounts();
}
