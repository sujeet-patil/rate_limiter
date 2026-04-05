package com.sujeet.projects.rate_limiter.filter;

import com.sujeet.projects.rate_limiter.algorithms.TokenBucketRateLimiter;
import com.sujeet.projects.rate_limiter.core.RateLimiter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Mono;

@Component
public class RateLimitFilter implements WebFilter {

    @Autowired
    private RateLimiter tokenBucketRateLimiter;

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, WebFilterChain chain) {
        String key = exchange.getRequest().getRemoteAddress().getAddress().getHostAddress();
        if (!tokenBucketRateLimiter.allowRequest(key)) {
            exchange.getResponse().setStatusCode(HttpStatus.TOO_MANY_REQUESTS);
            return exchange.getResponse().setComplete();
        }
        return chain.filter(exchange);
    }
}
