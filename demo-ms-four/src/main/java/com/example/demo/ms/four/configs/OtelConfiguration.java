package com.example.demo.ms.four.configs;

import io.opentelemetry.context.propagation.TextMapPropagator;
import io.opentelemetry.exporter.otlp.trace.OtlpGrpcSpanExporter;
import io.opentelemetry.extension.trace.propagation.JaegerPropagator;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;

@Configuration
public class OtelConfiguration {

    private final String otelCollectorUrl;

    public OtelConfiguration(@Value("${otel.collector.url") String otelCollectorUrl) {
        this.otelCollectorUrl = otelCollectorUrl;
    }

    @Bean
    public TextMapPropagator jaegerPropagator() {
        return JaegerPropagator.getInstance();
    }

    /**
     * This custom bean definition method is required if we want to use gRPC (typical port 4317)
     * instead of the default HTTP protocol (typically on port 4318) for exporting the span.
     * By default, the Spring's OtlpAutoConfiguration class will setup the OtlpHttpSpanExporter bean.
     *
     * @return OtlpGrpcSpanExporter
     */
    @Bean
    public OtlpGrpcSpanExporter grpcSpanExporter() {
        return OtlpGrpcSpanExporter.builder()
                .addHeader(HttpHeaders.CONTENT_TYPE, "application/x-protobuf")
                .setEndpoint(otelCollectorUrl)
                .build();
    }
}
