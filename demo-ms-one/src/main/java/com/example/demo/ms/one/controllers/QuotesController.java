package com.example.demo.ms.one.controllers;

import com.example.demo.ms.one.models.Quote;
import com.example.demo.ms.one.models.QuoteResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Random;


@Slf4j
@RestController
public class QuotesController {

    private final String applicationName;
    private final String applicationBaseApiUrl;
    private final List<Quote> quoteList;
    private final Random randomIndexGenerator = new Random();


    public QuotesController(@Value("${spring.application.name}") String applicationName,
                            @Value("${spring.webflux.base-path}") String applicationBaseApiUrl) {
        this.applicationName = applicationName;
        this.applicationBaseApiUrl = applicationBaseApiUrl;

        this.quoteList = new ArrayList<>();
        quoteList.add(new Quote("It’s hardware that makes a machine fast.  It’s software that makes a fast machine slow.", "Craig Bruce"));
        quoteList.add(new Quote("The more you know, the more you realize you know nothing.", "Socrates"));
        quoteList.add(new Quote("The best way to predict the future is to implement it.", "David Heinemeier Hansson"));
        quoteList.add(new Quote("If you think your users are idiots, only idiots will use it.", "Linus Torvalds"));
        quoteList.add(new Quote("A program is never less than 90% complete, and never more than 95% complete.", "Terry Baker"));
        quoteList.add(new Quote("Today, most software exists, not to solve a problem, but to interface with other software.", "IO Angell"));
        quoteList.add(new Quote("Programs must be written for people to read, and only incidentally for machines to execute.", "Abelson and Sussman"));
        quoteList.add(new Quote("We have to stop optimizing for programmers and start optimizing for users.", "Jeff Atwood"));
        quoteList.add(new Quote("Before software should be reusable, it should be usable.", "Ralph Johnson"));
        quoteList.add(new Quote("If you automate a mess, you get an automated mess.", "Rod Michael"));
        quoteList.add(new Quote("UNIX is simple.  It just takes a genius to understand its simplicity.", "Dennis Ritchie"));
    }

    @CrossOrigin
    @GetMapping("/quote")
    public Mono<QuoteResponse> getQuote2(@RequestHeader Map<String, String> headers) {
        log.trace("=======================================================");
        log.debug("[RECEIVED] :: API call request to {}/quote", applicationBaseApiUrl);
        log.trace("=======================================================");
        headers.forEach((key, value) -> log.debug("[REQUEST_HEADER] :: {} = {}", key, value));

        int index = randomIndexGenerator.nextInt(quoteList.size());
        Mono<QuoteResponse> quoteResponseMono = Mono.just(new QuoteResponse(applicationName, quoteList.get(index)));

        log.trace("=======================================================");
        log.debug("[COMPLETED] :: API call request to {}/quote", applicationBaseApiUrl);
        log.trace("=======================================================");
        return quoteResponseMono;
    }


}
