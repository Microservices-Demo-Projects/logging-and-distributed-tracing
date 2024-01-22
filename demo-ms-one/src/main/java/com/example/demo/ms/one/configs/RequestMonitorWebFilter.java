package com.example.demo.ms.one.configs;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.micrometer.context.ContextSnapshot;
import io.micrometer.observation.contextpropagation.ObservationThreadLocalAccessor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Mono;

import java.util.List;

import static java.util.Optional.ofNullable;

@Slf4j
@Component
public class RequestMonitorWebFilter implements WebFilter {
    @Override
    public Mono<Void> filter(ServerWebExchange exchange, WebFilterChain chain) {
        long startTime = System.currentTimeMillis();
        return chain.filter(exchange)
                /** !! IMPORTANT STEP !!
                 * Preparing context for the Tracer Span used in TracerConfiguration
                 */
                .contextWrite(context -> {
                    ContextSnapshot.setThreadLocalsFrom(context, ObservationThreadLocalAccessor.KEY);
                    return context;
                })
                /**
                 * Logging the metrics for the API call, not really required to have this section for tracing setup
                 */
                .doFinally(signalType -> {
                    long endTime = System.currentTimeMillis();
                    long executionTime = endTime - startTime;
                    /**
                     * Extracting traceId added in TracerConfiguration Webfilter bean
                     */
                    List<String> traceIds = ofNullable(exchange.getResponse().getHeaders().get("traceId")).orElse(List.of());
                    MetricsLogTemplate metricsLogTemplate = new MetricsLogTemplate(
                            String.join(",", traceIds),
                            exchange.getLogPrefix().trim(),
                            executionTime
                    );
                    try {
                        log.info(new ObjectMapper().writeValueAsString(metricsLogTemplate));
                    } catch (JsonProcessingException e) {
                        throw new RuntimeException(e);
                    }
                });
    }

    record MetricsLogTemplate(String traceId, String logPrefix, long executionTime){}
}