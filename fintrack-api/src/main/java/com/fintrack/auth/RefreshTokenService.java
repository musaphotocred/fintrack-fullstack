package com.fintrack.auth;

import com.fintrack.user.UserEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;
import java.util.Optional;

/**
 * Service for refresh token management.
 */
@Service
public class RefreshTokenService {

    private final RefreshTokenRepository refreshTokenRepository;
    private final JwtService jwtService;

    public RefreshTokenService(RefreshTokenRepository refreshTokenRepository, JwtService jwtService) {
        this.refreshTokenRepository = refreshTokenRepository;
        this.jwtService = jwtService;
    }

    /**
     * Creates and persists a new refresh token for the user.
     */
    @Transactional
    public String createRefreshToken(UserEntity user) {
        String tokenValue = jwtService.generateRefreshToken(user);
        Date expiration = jwtService.getRefreshTokenExpiration();
        LocalDateTime expiresAt = expiration.toInstant()
                .atZone(ZoneId.systemDefault())
                .toLocalDateTime();

        RefreshTokenEntity refreshToken = new RefreshTokenEntity(user, tokenValue, expiresAt);
        refreshTokenRepository.save(refreshToken);

        return tokenValue;
    }

    /**
     * Finds a refresh token by its value.
     */
    public Optional<RefreshTokenEntity> findByToken(String token) {
        return refreshTokenRepository.findByToken(token);
    }

    /**
     * Validates that a refresh token is valid (not revoked, not expired).
     */
    public boolean isValid(RefreshTokenEntity refreshToken) {
        return refreshToken.isValid();
    }

    /**
     * Revokes a specific refresh token.
     */
    @Transactional
    public void revoke(RefreshTokenEntity refreshToken) {
        refreshToken.setRevoked(true);
        refreshTokenRepository.save(refreshToken);
    }

    /**
     * Revokes all refresh tokens for a user.
     */
    @Transactional
    public void revokeAllForUser(Long userId) {
        refreshTokenRepository.revokeAllByUserId(userId);
    }

    /**
     * Cleanup expired tokens.
     */
    @Transactional
    public void cleanupExpiredTokens() {
        refreshTokenRepository.deleteExpiredTokens();
    }
}
