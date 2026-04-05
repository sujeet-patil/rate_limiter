package com.sujeet.projects.rate_limiter.algorithms;

import com.sujeet.projects.rate_limiter.core.RateLimiter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class TokenBucketRateLimiter implements RateLimiter {
    private final int capacity;
    private final double refillRate; // tokens per second

    @Autowired
    private StringRedisTemplate redis;

    private final DefaultRedisScript<Long> rateLimitScript;


    private static final String LUA_SCRIPT = """
            local tokens_key     = KEYS[1]
            local last_refill_key = KEYS[2]
            
            local capacity    = tonumber(ARGV[1])
            local refill_rate = tonumber(ARGV[2])
            local now         = tonumber(ARGV[3])
            
            local tokens_val     = redis.call('GET', tokens_key)
            local last_refill_val = redis.call('GET', last_refill_key)
            
            local tokens
            local last_refill
            
            if tokens_val == false or last_refill_val == false then
                -- First request: initialise full bucket
                tokens      = capacity
                last_refill = now
            else
                tokens      = tonumber(tokens_val)
                last_refill = tonumber(last_refill_val)
            end
            
            -- Refill based on elapsed time
            local elapsed = (now - last_refill) / 1000.0
            tokens = math.min(capacity, tokens + elapsed * refill_rate)
            
            local allowed = 0
            
            if tokens >= 1 then
                tokens  = tokens - 1
                allowed = 1
            end
            
            -- Always persist updated state back to Redis
            redis.call('SET', tokens_key,      tostring(tokens))
            redis.call('SET', last_refill_key, tostring(now))
            
            return allowed
            """;


    public TokenBucketRateLimiter() {
        this.capacity = 10;
        this.refillRate = 0.1;

        this.rateLimitScript = new DefaultRedisScript<>();
        this.rateLimitScript.setScriptText(LUA_SCRIPT);
        this.rateLimitScript.setResultType(Long.class);
    }

    @Override
    public synchronized boolean allowRequest(String key) {
        String tokensKey     = "rate_limit:" + key + ":tokens";
        String lastRefillKey = "rate_limit:" + key + ":last_refill";

        Long result = redis.execute(
                rateLimitScript,
                List.of(tokensKey, lastRefillKey),  // KEYS
                String.valueOf(capacity),            // ARGV[1]
                String.valueOf(refillRate),          // ARGV[2]
                String.valueOf(System.currentTimeMillis()) // ARGV[3]
        );

        return Long.valueOf(1).equals(result);
    }
}
