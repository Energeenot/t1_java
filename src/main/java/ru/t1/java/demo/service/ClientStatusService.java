package ru.t1.java.demo.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.Map;

@Service
@Slf4j
public class ClientStatusService {

    private final RestTemplate restTemplate;
    private final JwtProvider jwtProvider;

    @Autowired
    public ClientStatusService(RestTemplate restTemplate, JwtProvider jwtProvider) {
        this.restTemplate = restTemplate;
        this.jwtProvider = jwtProvider;
    }

    public Map<String, String> sendRequestToService2(Long clientId, Long accountId) {
        log.info("sendRequestToService2 called");
        String token = jwtProvider.generateTokenForService();

        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer " + token);

        HttpEntity<Void> request = new HttpEntity<>(headers);

        ResponseEntity<String> response = restTemplate.exchange(
                "http://localhost:8081/clients?clientId=" + clientId + "&accountId=" + accountId,
                HttpMethod.GET,
                request,
                String.class
        );

        ObjectMapper objectMapper = new ObjectMapper();
        Map<String, String> responseMap;
        try {
            responseMap = objectMapper.readValue(response.getBody(), new TypeReference<Map<String, String>>() {});
            log.info("trying to get client status in map {}", responseMap);
        } catch (Exception e) {
            log.error("get client status failed with exception {}", e.getMessage());
            throw new RuntimeException("Failed to parse response", e);
        }
        return responseMap;
    }
}
