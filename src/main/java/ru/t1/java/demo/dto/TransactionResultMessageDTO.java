package ru.t1.java.demo.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class TransactionResultMessageDTO {

    @JsonProperty("account_id")
    private String accountId;
    @JsonProperty("client_id")
    private String clientId;
    @JsonProperty("transaction_id")
    private String transactionId;
    @JsonProperty("status")
    private String status;
}
