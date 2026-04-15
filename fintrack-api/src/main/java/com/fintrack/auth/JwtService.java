package com.fintrack.auth;

import com.fintrack.user.UserEntity;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * Service for JWT token generation and validation.
 * Uses HS256 algorithm with configurable secret and TTL.
 */
@Service
public class JwtService {

    private static final Logger log = LoggerFactory.getLogger(JwtService.class);

    private final SecretKey secretKey;
    private final long accessTokenTtlMillis;
    private final long refreshTokenTtlMillis;

    public JwtService(
            @Value("${jwt.secret}") String secret,
            @Value("${jwt.access-ttl-minutes:15}") long accessTtlMinutes,
            @Value("${jwt.refresh-ttl-days:7}") long refreshTtlDays) {
        this.secretKey = Keys.hmacShaKeyFor(Decoders.BASE64.decode(secret));
        this.accessTokenTtlMillis = accessTtlMinutes * 60 * 1000;
        this.refreshTokenTtlMillis = refreshTtlDays * 24 * 60 * 60 * 1000;
    }

    /**
     * Generates an access token for the given user.
     */
    public String generateAccessToken(UserEntity user) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("userId", user.getId());
        claims.put("role", user.getRole().name());

        return Jwts.builder()
                .claims(claims)
                .subject(user.getEmail())
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + accessTokenTtlMillis))
                .signWith(secretKey)
                .compact();
    }

    /**
     * Generates a refresh token for the given user.
     */
    public String generateRefreshToken(UserEntity user) {
        return Jwts.builder()
                .subject(user.getEmail())
                .claim("userId", user.getId())
                .claim("type", "refresh")
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + refreshTokenTtlMillis))
                .signWith(secretKey)
                .compact();
    }

    /**
     * Extracts the email (subject) from a token.
     */
    public String extractEmail(String token) {
        return extractAllClaims(token).getSubject();
    }

    /**
     * Extracts the user ID from a token.
     */
    public Long extractUserId(String token) {
        Object userId = extractAllClaims(token).get("userId");
        if (userId instanceof Integer) {
            return ((Integer) userId).longValue();
        }
        return (Long) userId;
    }

    /**
     * Validates a token for the given user.
     */
    public boolean isTokenValid(String token, UserEntity user) {
        try {
            String email = extractEmail(token);
            return email.equals(user.getEmail()) && !isTokenExpired(token);
        } catch (JwtException | IllegalArgumentException e) {
            log.debug("Token validation failed: {}", e.getMessage());
            return false;
        }
    }

    /**
     * Checks if a token is expired.
     */
    public boolean isTokenExpired(String token) {
        try {
            return extractAllClaims(token).getExpiration().before(new Date());
        } catch (JwtException | IllegalArgumentException e) {
            return true;
        }
    }

    /**
     * Gets the access token TTL in seconds.
     */
    public long getAccessTokenTtlSeconds() {
        return accessTokenTtlMillis / 1000;
    }

    /**
     * Gets the refresh token expiration date for database storage.
     */
    public Date getRefreshTokenExpiration() {
        return new Date(System.currentTimeMillis() + refreshTokenTtlMillis);
    }

    private Claims extractAllClaims(String token) {
        return Jwts.parser()
                .verifyWith(secretKey)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }
}
