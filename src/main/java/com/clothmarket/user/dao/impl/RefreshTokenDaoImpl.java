package com.clothmarket.user.dao.impl;

import com.clothmarket.user.dao.RefreshTokenDao;
import com.clothmarket.user.entity.RefreshTokenEntity;
import com.clothmarket.user.repository.RefreshTokenRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Optional;

/**
 * Data Access Object implementation for RefreshToken persistence operations.
 * Delegates database interactions to {@link RefreshTokenRepository}.
 */
@Component
@RequiredArgsConstructor
public class RefreshTokenDaoImpl implements RefreshTokenDao {

    private final RefreshTokenRepository refreshTokenRepository;

    /**
     * Persists a new refresh token entity.
     *
     * @param refreshToken the RefreshTokenEntity to save
     * @return the saved RefreshTokenEntity instance
     */
    @Override
    public RefreshTokenEntity save(RefreshTokenEntity refreshToken) {
        return refreshTokenRepository.save(refreshToken);
    }

    /**
     * Finds a refresh token entity by its raw token string.
     *
     * @param token the token string to search for
     * @return an Optional containing the RefreshTokenEntity if found
     */
    @Override
    public Optional<RefreshTokenEntity> findByToken(String token) {
        return refreshTokenRepository.findByToken(token);
    }

    /**
     * Revokes a specific refresh token by its token string.
     *
     * @param token the token string to revoke
     */
    @Override
    public void revokeByToken(String token) {
        refreshTokenRepository.revokeByToken(token);
    }

    /**
     * Revokes all active refresh tokens associated with a user ID.
     *
     * @param userId the user identifier
     */
    @Override
    public void revokeAllByUserId(Long userId) {
        refreshTokenRepository.revokeAllByUserId(userId);
    }
}
