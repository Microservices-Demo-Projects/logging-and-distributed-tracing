package com.example.demo.ms.three.controllers;


import com.example.demo.ms.three.models.QuoteResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.reactive.function.client.ClientResponse;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.Map;


@Slf4j
@RestController
public class QuotesController {

    private final String applicationName;
    private final String applicationBaseApiUrl;
    private final WebClient webClient;

    public QuotesController(@Value("${spring.application.name}") String applicationName,
                            @Value("${spring.webflux.base-path}") String applicationBaseApiUrl,
                            @Value("${client.base-url.demo-ms-two}") String clientBaseUrl,
                            WebClient.Builder webClientBuilder) {

        this.applicationName = applicationName;
        this.applicationBaseApiUrl = applicationBaseApiUrl;

        // NOTE: Creating the WebClient instance using the WebClient.builder() injected/autowired by springBoot
        // sets the necessary defaults for traceId/SpanId propagation across microservice API calls.
        webClient = webClientBuilder.baseUrl(clientBaseUrl).build();

    }

    @CrossOrigin
    @GetMapping("/quote")
    public Mono<QuoteResponse> getQuote2(@RequestHeader Map<String, String> headers) {
        log.trace("=======================================================");
        log.debug("[RECEIVED] :: API call request to {}/quote", applicationBaseApiUrl);
        log.trace("=======================================================");
        headers.forEach((key, value) -> log.debug("[REQUEST_HEADER] :: {} = {}", key, value));

        Mono<QuoteResponse> quoteResponseMono = webClient.get()
                .uri("/quote")
                .exchangeToMono(this::handleResponse);

        log.trace("=======================================================");
        log.debug("[COMPLETED] :: API call request to {}/quote", applicationBaseApiUrl);
        log.trace("=======================================================");
        return quoteResponseMono;
    }

    private Mono<QuoteResponse> handleResponse(ClientResponse clientResponse) {
        ClientResponse.Headers responseHeaders = clientResponse.headers();
        responseHeaders.asHttpHeaders().forEach((key, value) ->
                log.debug("[RESPONSE_HEADER] :: {} = {}", key, value));

        HttpStatusCode statusCode = clientResponse.statusCode();  // HTTP Status
        if (statusCode.is2xxSuccessful()) {
            return clientResponse.bodyToMono(QuoteResponse.class)
                    .map(response -> {
                        response.setApplicationName(applicationName);
                        return response;
                    });

        } else {
            return Mono.error(new RuntimeException("Unexpected Server error"));
        }
    }
}
