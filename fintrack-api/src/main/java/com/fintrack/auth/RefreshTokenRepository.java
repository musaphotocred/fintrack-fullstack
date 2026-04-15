package com.fintrack.auth;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Repository for refresh token persistence operations.
 */
@Repository
public interface RefreshTokenRepository extends JpaRepository<RefreshTokenEntity, Long> {

    /**
     * Find a refresh token by its token string.
     */
    Optional<RefreshTokenEntity> findByToken(String token);

    /**
     * Revoke all refresh tokens for a user.
     */
    @Modifying
    @Query("UPDATE RefreshTokenEntity rt SET rt.revoked = true WHERE rt.user.id = :userId AND rt.revoked = false")
    void revokeAllByUserId(Long userId);

    /**
     * Delete expired tokens (cleanup job).
     */
    @Modifying
    @Query("DELETE FROM RefreshTokenEntity rt WHERE rt.expiresAt < CURRENT_TIMESTAMP")
    void deleteExpiredTokens();
}
