package com.fintrack.config;

import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Rate limiting configuration using Bucket4j.
 * Provides per-IP rate limiting for auth endpoints.
 */
@Component
public class RateLimitConfig {

    private final Map<String, Bucket> loginBuckets = new ConcurrentHashMap<>();
    private final Map<String, Bucket> registerBuckets = new ConcurrentHashMap<>();
    private final Map<String, Bucket> generalBuckets = new ConcurrentHashMap<>();

    /**
     * Resolves a rate limit bucket for login attempts (10 per minute per IP).
     */
    public Bucket resolveLoginBucket(String ipAddress) {
        return loginBuckets.computeIfAbsent(ipAddress, this::createLoginBucket);
    }

    /**
     * Resolves a rate limit bucket for registration attempts (5 per minute per IP).
     */
    public Bucket resolveRegisterBucket(String ipAddress) {
        return registerBuckets.computeIfAbsent(ipAddress, this::createRegisterBucket);
    }

    /**
     * Resolves a rate limit bucket for general API requests (100 per minute per user/IP).
     */
    public Bucket resolveGeneralBucket(String identifier) {
        return generalBuckets.computeIfAbsent(identifier, this::createGeneralBucket);
    }

    private Bucket createLoginBucket(String key) {
        Bandwidth limit = Bandwidth.builder()
                .capacity(10)
                .refillGreedy(10, Duration.ofMinutes(1))
                .build();
        return Bucket.builder().addLimit(limit).build();
    }

    private Bucket createRegisterBucket(String key) {
        Bandwidth limit = Bandwidth.builder()
                .capacity(5)
                .refillGreedy(5, Duration.ofMinutes(1))
                .build();
        return Bucket.builder().addLimit(limit).build();
    }

    private Bucket createGeneralBucket(String key) {
        Bandwidth limit = Bandwidth.builder()
                .capacity(100)
                .refillGreedy(100, Duration.ofMinutes(1))
                .build();
        return Bucket.builder().addLimit(limit).build();
    }
}
