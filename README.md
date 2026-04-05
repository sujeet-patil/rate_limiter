# Rate Limiter

A Spring Boot project demonstrating rate limiting using the **Token Bucket algorithm** backed by **Redis**. Built as a system design learning project.

## What it does

Every incoming request is checked against a token bucket stored in Redis. Each user gets a bucket with a fixed capacity that refills at a steady rate. If the bucket is empty, the request is rejected with a `429 Too Many Requests` response.

The read-modify-write on the bucket is handled by a **Lua script** executed atomically inside Redis, preventing race conditions across multiple server instances.

## Tech Stack

- **Java 17**
- **Spring Boot 4.x**
- **Redis** — stores token buckets per user
- **Lettuce** — Redis client (included with Spring Data Redis)
- **Lombok** — reduces boilerplate

## Project Structure

```
com.sujeet.projects.rate_limiter
├── algorithms/
│   └── TokenBucketRateLimiter.java   # Token bucket logic with Lua script
├── configuration/
│   ├── RedisConfiguration.java       # Redis connection setup
│   └── SecretProperties.java         # Maps redis secrets from yml
├── controller/
│   └── HelloController.java          # Sample protected endpoint
├── core/
│   └── RateLimiter.java              # RateLimiter interface
├── filter/
│   └── RateLimitFilter.java          # Intercepts every request
└── RateLimiterApplication.java
```

## How it works

```
Incoming Request
      ↓
RateLimitFilter
      ↓
TokenBucketRateLimiter.allowRequest(userId)
      ↓
Lua script runs atomically in Redis
      ↓
allowed → chain.doFilter()   /   denied → 429
```

### Token Bucket Algorithm

- Each user gets a bucket with a capacity of **10 tokens**
- Tokens refill at **1 token per second**
- Each request consumes 1 token
- If tokens < 1, the request is blocked

### Why Lua?

Without an atomic script, two servers reading and writing the same bucket simultaneously can cause race conditions — both see 1 token, both decrement, and one request slips through. The Lua script runs as a single Redis command, so no other operation can interleave.

## Getting Started

### Prerequisites

- Java 17+
- Redis server running on `localhost:6379`

### Configuration

Create `src/main/resources/application-secrets.yml` (this file is gitignored):

```yaml
redis:
  username: default
  password: yourpassword
```

The main `application.yml` is already configured for `localhost:6379`.

### Run

```bash
./gradlew bootRun
```

### Test

```bash
# Single request
curl -H "X-User-Id: sujeet" http://localhost:8080/api/hello

# Hammer to trigger rate limit (watch for 429s after 10 requests)
for i in {1..15}; do
  curl -s -o /dev/null -w "%{http_code}\n" \
    -H "X-User-Id: sujeet" \
    http://localhost:8080/api/hello
done
```

Expected output:
```
200
200
200
...
429
429
429
```

## API

| Method | Endpoint | Header | Description |
|--------|----------|--------|-------------|
| GET | `/api/hello` | `X-User-Id: {userId}` | Returns a greeting, rate limited per user |

### Responses

**200 OK**
```
Hello, User!
```

**429 Too Many Requests**
```json
{ "error": "Too Many Requests" }
```

Headers on 429:
```
Retry-After: 1
```

## Redis Key Structure

```
rate_limit:{userId}:tokens       → current token count (e.g. "8.0")
rate_limit:{userId}:last_refill  → last refill timestamp in ms
```

You can inspect these live in **RedisInsight** while sending requests.

## Roadmap

- [ ] JWT authentication with user login/register
- [ ] MongoDB for user storage
- [ ] Per-user rate limits based on plan (free / premium / admin)
- [ ] Sliding window algorithm
- [ ] Rate limit response headers (`X-RateLimit-Limit`, `X-RateLimit-Remaining`, `X-RateLimit-Reset`)
- [ ] Request audit logs in MongoDB