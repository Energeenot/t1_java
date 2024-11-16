package ru.t1.java.demo.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class TransactionRequestedDTO {

    @JsonProperty("client_id")
    private String clientId;
    @JsonProperty("account_id")
    private String accountId;
    @JsonProperty("transaction_id")
    private String transactionId;
    @JsonProperty("timestamp")
    private LocalDateTime timestamp;
    @JsonProperty("transaction_amount")
    private double transactionAmount;
    @JsonProperty("account_balance")
    private double accountBalance;

}
