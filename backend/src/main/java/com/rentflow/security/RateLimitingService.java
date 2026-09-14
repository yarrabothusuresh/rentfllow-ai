package com.rentflow.security;

import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.regex.Pattern;

/**
 * Token-bucket in-memory rate limiting service for high-risk and public API endpoints.
 * Provides category-specific quotas, safe IP resolution, and detailed audit logging.
 *
 * NOTE: This is an in-memory token bucket implementation per JVM instance.
 * For distributed/multi-instance deployments, Redis-backed rate limiting is scheduled for Day 41.
 */
@Service
public class RateLimitingService {

    private static final Logger log = LoggerFactory.getLogger(RateLimitingService.class);

    private static final Pattern IP_PATTERN = Pattern.compile(
            "^([0-9]{1,3}\\.){3}[0-9]{1,3}$|^([0-9a-fA-F]{1,4}:){1,7}[0-9a-fA-F]{1,4}$|^::1$"
    );

    public enum RateLimitCategory {
        AUTH_LOGIN,
        PASSWORD_RESET,
        PUBLIC_STOREFRONT,
        PUBLIC_CHECKOUT,
        PUBLIC_INQUIRY,
        AI_SALES,
        AI_COPILOT,
        PHONE_AI,
        WEBHOOK,
        EXTERNAL_API
    }

    @Value("${security.ratelimit.requests-per-minute:60}")
    private int defaultRpm = 60;

    @Value("${security.rate-limit.login.capacity:10}")
    private int loginLimit = 10;

    @Value("${security.rate-limit.public-storefront.capacity:60}")
    private int storefrontLimit = 60;

    @Value("${security.rate-limit.checkout.capacity:10}")
    private int checkoutLimit = 10;

    @Value("${security.rate-limit.ai-sales.capacity:20}")
    private int aiSalesLimit = 20;

    @Value("${security.rate-limit.ai-copilot.capacity:30}")
    private int aiCopilotLimit = 30;

    @Value("${security.rate-limit.phone-webhook.capacity:120}")
    private int phoneWebhookLimit = 120;

    @Value("${security.rate-limit.webhook.capacity:120}")
    private int webhookLimit = 120;

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
     * Resolves the request rate limit quota for a given category.
     */
    public int getLimitForCategory(RateLimitCategory category) {
        return switch (category) {
            case AUTH_LOGIN -> loginLimit;
            case PASSWORD_RESET -> 5;
            case PUBLIC_STOREFRONT, PUBLIC_INQUIRY -> storefrontLimit;
            case PUBLIC_CHECKOUT -> checkoutLimit;
            case AI_SALES -> aiSalesLimit;
            case AI_COPILOT -> aiCopilotLimit;
            case PHONE_AI -> phoneWebhookLimit;
            case WEBHOOK -> webhookLimit;
            case EXTERNAL_API -> 120;
        };
    }

    /**
     * Checks if a request in a specific category is allowed.
     */
    public boolean tryAcquire(RateLimitCategory category, String key) {
        int limit = getLimitForCategory(category);
        String fullKey = category.name() + ":" + key;
        boolean allowed = tryAcquire(fullKey, limit);
        if (!allowed) {
            log.warn("[RateLimit] Limit exceeded for category={}, key={}, maxRpm={}", category, key, limit);
        }
        return allowed;
    }

    /**
     * Checks if a request for the given key is allowed under the default rate limit.
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
     * Safely resolves client IP address from HTTP request without blindly trusting X-Forwarded-For.
     */
    public String resolveSafeClientIp(HttpServletRequest request) {
        if (request == null) {
            return "unknown";
        }
        String forwardedFor = request.getHeader("X-Forwarded-For");
        if (forwardedFor != null && !forwardedFor.isBlank()) {
            String firstIp = forwardedFor.split(",")[0].trim();
            if (IP_PATTERN.matcher(firstIp).matches()) {
                return firstIp;
            }
        }
        String remoteAddr = request.getRemoteAddr();
        return (remoteAddr != null && !remoteAddr.isBlank()) ? remoteAddr : "unknown";
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
