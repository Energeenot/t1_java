package ru.t1.java.demo.util;

import ru.t1.java.demo.dto.TransactionDTO;
import ru.t1.java.demo.model.Transaction;

public class TransactionMapper {

    public static Transaction toEntity(TransactionDTO dto) {
        return Transaction.builder()
                .transactionAmount(dto.getTransactionAmount())
                .transactionTime(dto.getTransactionTime())
                .build();
    }
}
