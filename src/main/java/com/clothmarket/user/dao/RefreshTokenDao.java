package com.clothmarket.user.dao;

import com.clothmarket.user.entity.RefreshTokenEntity;

import java.util.Optional;

/**
 * Data Access Object interface for RefreshToken entity operations.
 * Manages storage and revocation of refresh tokens for authenticated sessions.
 */
public interface RefreshTokenDao {

    /**
     * Persists a new refresh token entity.
     *
     * @param refreshToken the RefreshTokenEntity to save
     * @return the saved RefreshTokenEntity instance
     */
    RefreshTokenEntity save(RefreshTokenEntity refreshToken);

    /**
     * Finds a refresh token entity by its raw token string.
     *
     * @param token the token string to search for
     * @return an Optional containing the RefreshTokenEntity if found
     */
    Optional<RefreshTokenEntity> findByToken(String token);

    /**
     * Revokes a specific refresh token by its token string.
     *
     * @param token the token string to revoke
     */
    void revokeByToken(String token);

    /**
     * Revokes all active refresh tokens associated with a user ID.
     *
     * @param userId the user identifier
     */
    void revokeAllByUserId(Long userId);
}
