package com.consignment.gateway.filter;

import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.util.UUID;

@Component
public class CorrelationIdGlobalFilter implements GlobalFilter, Ordered {

    private static final String CORRELATION_ID_HEADER = "X-Correlation-Id";

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        if (exchange.getRequest().getHeaders().containsKey(CORRELATION_ID_HEADER)) {
            return chain.filter(exchange);
        }

        String correlationId = UUID.randomUUID().toString();
        try {
            ServerWebExchange mutated = exchange.mutate()
                    .request(r -> r.header(CORRELATION_ID_HEADER, correlationId))
                    .build();
            return chain.filter(mutated);
        } catch (UnsupportedOperationException e) {
            return chain.filter(exchange);
        }
    }

    @Override
    public int getOrder() {
        return Ordered.HIGHEST_PRECEDENCE;
    }
}
