package com.sujeet.projects.rate_limiter.core;

public interface RateLimiter {
    boolean allowRequest(String key);
}
