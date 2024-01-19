package com.example.demo.ms.four.controllers;


import com.example.demo.ms.four.models.QuoteResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

import java.util.Map;


@Slf4j
@RestController
public class QuotesController {

    private final String applicationName;
    private final String applicationBaseApiUrl;
    private final String clientBaseUrl;
    private final RestTemplate restTemplate;

    public QuotesController(@Value("{spring.application.name}") String applicationName,
                            @Value("{server.servlet.context-path}") String applicationBaseApiUrl,
                            @Value("{client.base-url.demo-ms-three}") String clientBaseUrl,
                            RestTemplateBuilder restTemplateBuilder) {

        this.applicationName = applicationName;
        this.applicationBaseApiUrl = applicationBaseApiUrl;
        this.clientBaseUrl = clientBaseUrl;

        // NOTE: Creating the RestTemplate instance using the RestTemplateBuilder
        // sets the necessary defaults for traceId/SpanId propagation across microservice API calls.
        restTemplate = restTemplateBuilder.build();
    }

    @CrossOrigin
    @GetMapping("/quote")
    public QuoteResponse getQuote(@RequestHeader Map<String, String> headers) {
        log.trace("=======================================================");
        log.debug("[RECEIVED] :: API call request to {}/quote", applicationBaseApiUrl);
        log.trace("=======================================================");
        headers.forEach((key, value) -> log.debug("[REQUEST_HEADER] :: {} = {}", key, value));

        ResponseEntity<QuoteResponse> quoteResponseEntity = restTemplate.exchange(clientBaseUrl+"/quote",
                HttpMethod.GET, null, QuoteResponse.class);
        quoteResponseEntity.getHeaders().forEach((key, value) -> log.debug("[RESPONSE_HEADER] :: {} = {}", key, value));
        QuoteResponse quoteResponse = quoteResponseEntity.getBody();
        if(quoteResponse != null) {
            quoteResponse.setApplicationName(applicationName);
        }
        log.debug("Quote Response: {}", quoteResponse);

        log.trace("=======================================================");
        log.debug("[COMPLETED] :: API call request to {}/quote", applicationBaseApiUrl);
        log.trace("=======================================================");
        return quoteResponse;
    }
}
