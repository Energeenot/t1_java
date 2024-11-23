package ru.t1.java.demo.model;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.springframework.data.jpa.domain.AbstractPersistable;
import ru.t1.java.demo.model.enums.TransactionStatus;

import java.time.LocalDateTime;

@Entity
@Table(name = "transaction")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString
public class Transaction extends AbstractPersistable<Long> {

    @Column(name = "transaction_amount")
    private double transactionAmount;
    @CreationTimestamp
    @Column(name = "transaction_time", nullable = false, updatable = false)
    private LocalDateTime transactionTime;
    @Enumerated(EnumType.STRING)
    @Column(name = "status")
    private TransactionStatus status;
    @Column(name = "transaction_id")
    private String transactionId;
    // зачем второй timestamp?  в первом дз ещё надо было добавить время транзакции
    @CreationTimestamp
    private LocalDateTime timestamp;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "account_id", nullable = false)
    private Account account;

}
