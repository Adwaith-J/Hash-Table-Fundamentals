import java.util.concurrent.*;
import java.util.concurrent.atomic.*;

class TokenBucket {
    AtomicInteger tokens;
    long lastRefillTime;
    final int maxTokens;
    final double refillRate;

    TokenBucket(int maxTokens, double refillRate) {
        this.tokens = new AtomicInteger(maxTokens);
        this.lastRefillTime = System.currentTimeMillis();
        this.maxTokens = maxTokens;
        this.refillRate = refillRate;
    }

    synchronized boolean allowRequest() {
        refill();
        if (tokens.get() > 0) {
            tokens.decrementAndGet();
            return true;
        }
        return false;
    }

    synchronized void refill() {
        long now = System.currentTimeMillis();
        double tokensToAdd = (now - lastRefillTime) / 1000.0 * refillRate;
        if (tokensToAdd > 0) {
            int newTokens = (int) Math.min(maxTokens, tokens.get() + tokensToAdd);
            tokens.set(newTokens);
            lastRefillTime = now;
        }
    }

    int remaining() {
        refill();
        return tokens.get();
    }

    long retryAfter() {
        if (tokens.get() > 0) return 0;
        double seconds = 1.0 / refillRate;
        return (long) seconds;
    }
}

class RateLimitResult {
    boolean allowed;
    int remaining;
    long retryAfter;

    RateLimitResult(boolean allowed, int remaining, long retryAfter) {
        this.allowed = allowed;
        this.remaining = remaining;
        this.retryAfter = retryAfter;
    }

    public String toString() {
        if (allowed)
            return "Allowed (" + remaining + " requests remaining)";
        else
            return "Denied (0 requests remaining, retry after " + retryAfter + "s)";
    }
}

class RateLimitStatus {
    int used;
    int limit;
    long reset;

    RateLimitStatus(int used, int limit, long reset) {
        this.used = used;
        this.limit = limit;
        this.reset = reset;
    }

    public String toString() {
        return "{used:" + used + ", limit:" + limit + ", reset:" + reset + "}";
    }
}

public class DistributedRateLimiter {
    private final ConcurrentHashMap<String, TokenBucket> buckets = new ConcurrentHashMap<>();
    private final int maxTokens = 1000;
    private final double refillRate = 1000.0 / 3600.0;

    private TokenBucket getBucket(String clientId) {
        return buckets.computeIfAbsent(clientId, k -> new TokenBucket(maxTokens, refillRate));
    }

    public RateLimitResult checkRateLimit(String clientId) {
        TokenBucket bucket = getBucket(clientId);
        boolean allowed = bucket.allowRequest();
        int remaining = bucket.remaining();
        long retry = allowed ? 0 : bucket.retryAfter();
        return new RateLimitResult(allowed, remaining, retry);
    }

    public RateLimitStatus getRateLimitStatus(String clientId) {
        TokenBucket bucket = getBucket(clientId);
        int remaining = bucket.remaining();
        int used = maxTokens - remaining;
        long reset = System.currentTimeMillis() / 1000 + (long)((maxTokens - remaining) / refillRate);
        return new RateLimitStatus(used, maxTokens, reset);
    }

    public static void main(String[] args) {
        DistributedRateLimiter limiter = new DistributedRateLimiter();
        System.out.println(limiter.checkRateLimit("abc123"));
        System.out.println(limiter.checkRateLimit("abc123"));
        System.out.println(limiter.getRateLimitStatus("abc123"));
    }
}