package com.rentflow.security;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Token-bucket in-memory rate limiting service for high-risk and public API endpoints.
 */
@Service
public class RateLimitingService {

    @Value("${security.ratelimit.requests-per-minute:60}")
    private int defaultRpm;

    private static class Bucket {
        long windowStart;
        AtomicInteger count;

        Bucket(long windowStart) {
            this.windowStart = windowStart;
            this.count = new AtomicInteger(0);
        }
    }

    private final Map<String, Bucket> buckets = new ConcurrentHashMap<>();

    /**
     * Checks if a request for the given key is allowed under the default rate limit.
     * @param key unique identifier (e.g. client IP or tenantId + endpoint)
     * @return true if permitted, false if rate limited
     */
    public boolean tryAcquire(String key) {
        return tryAcquire(key, defaultRpm);
    }

    /**
     * Checks if a request for the given key is allowed under a custom limit.
     */
    public boolean tryAcquire(String key, int maxRequestsPerMinute) {
        long now = System.currentTimeMillis();
        long window = now / 60000; // 1-minute window

        Bucket bucket = buckets.compute(key, (k, existing) -> {
            if (existing == null || existing.windowStart != window) {
                return new Bucket(window);
            }
            return existing;
        });

        return bucket.count.incrementAndGet() <= maxRequestsPerMinute;
    }

    /**
     * Resets rate limit for a specific key (useful for testing).
     */
    public void reset(String key) {
        buckets.remove(key);
    }

    /**
     * Clears all rate limiting buckets.
     */
    public void clearAll() {
        buckets.clear();
    }
}
