package ru.t1.java.demo.service;

import com.github.tomakehurst.wiremock.client.WireMock;
import com.github.tomakehurst.wiremock.junit5.WireMockExtension;
import com.github.tomakehurst.wiremock.junit5.WireMockTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpHeaders;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import ru.t1.java.demo.T1JavaDemoApplication;

import java.util.Map;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.wireMockConfig;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.when;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT, classes = {T1JavaDemoApplication.class})
@WireMockTest
public class ClientStatusServiceTest {

    @Autowired
    private ClientStatusService clientStatusService;

    @MockBean
    private JwtProvider jwtProvider;

    @BeforeEach
    void setUp() {
        when(jwtProvider.generateTokenForService()).thenReturn("mocked-jwt-token");
    }

    @RegisterExtension
    static WireMockExtension wireMockExtension = WireMockExtension.newInstance()
            .options(wireMockConfig().dynamicPort()).build();

    @DynamicPropertySource
    static void setUpMockBaseUrl(DynamicPropertyRegistry registry) {
        registry.add("client.service.url", wireMockExtension::baseUrl);
    }

    @Test
    void sendToService2ShouldReturnActiveStatus(){
        long clientId = 1;
        long accountId = 2;
        String token = "mocked-jwt-token";
        wireMockExtension.stubFor(
                WireMock.get("/clients?clientId=" + clientId +"&accountId=" + accountId)
                        .willReturn(aResponse()
                                .withHeader(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                                .withHeader(HttpHeaders.CONTENT_TYPE, "application/json")
                                .withStatus(200)
                                        .withBody("{\"status\":\"active\"}")));

        System.out.println("WireMock Base URL: " + wireMockExtension.baseUrl());
        Map<String, String> response = clientStatusService.sendRequestToService2(clientId, accountId);
        assertNotNull(response);
        assertEquals("active", response.get("status"));
    }
}
