package com.sujeet.projects.rate_limiter.algorithms;

import com.sujeet.projects.rate_limiter.core.RateLimiter;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class TokenBucketRateLimiter implements RateLimiter {
    private final int capacity;
    private final double refillRate; // tokens per second
    private final Map<String, double[]> buckets = new ConcurrentHashMap<>();
    // double[0] = tokens, double[1] = lastRefillTime

    public TokenBucketRateLimiter(int capacity, double refillRate) {
        this.capacity = capacity;
        this.refillRate = refillRate;
    }

    @Override
    public synchronized boolean allowRequest(String key) {
        double[] bucket = buckets.computeIfAbsent(key,
                k -> new double[]{capacity, System.currentTimeMillis()});

        long now = System.currentTimeMillis();
        double elapsed = (now - bucket[1]) / 1000.0;
        bucket[0] = Math.min(capacity, bucket[0] + elapsed * refillRate);
        bucket[1] = now;

        if (bucket[0] >= 1) {
            bucket[0]--;
            return true;
        }
        return false;
    }
}
