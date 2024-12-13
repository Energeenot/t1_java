package ru.t1.java.demo.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import ru.t1.java.demo.model.Client;
import ru.t1.java.demo.model.enums.ClientStatus;

import java.util.List;
import java.util.Optional;

public interface ClientRepository extends JpaRepository<Client, Long> {
    @Override
    Optional<Client> findById(Long aLong);

    @Query(value = "SELECT * FROM client WHERE status = :status LIMIT :limit", nativeQuery = true)
    List<Client> findBlockedClientsWithLimit(@Param("status") String status, @Param("limit") int limit);

    @Query("SELECT COUNT(c) FROM Client c WHERE c.status = 'BLOCKED'")
    long countBlockedClients();
}