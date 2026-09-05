package com.clothmarket.user.repository;

import com.clothmarket.user.entity.RefreshTokenEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Spring Data JPA repository for refresh token persistence operations.
 * Declares query methods for token lookup, revocation, and cleanup.
 */
@Repository
public interface RefreshTokenRepository extends JpaRepository<RefreshTokenEntity, Long> {

    /**
     * Finds a refresh token entity by its unique token string.
     *
     * @param token the token string
     * @return an Optional containing the RefreshTokenEntity if found, or empty
     */
    Optional<RefreshTokenEntity> findByToken(String token);

    /**
     * Revokes all active refresh tokens for a specific user.
     *
     * @param userId the user identifier
     */
    @Modifying
    @Query("UPDATE RefreshTokenEntity r SET r.revoked = true WHERE r.userId = :userId AND r.revoked = false")
    void revokeAllByUserId(@Param("userId") Long userId);

    /**
     * Revokes a specific token string.
     *
     * @param token the token string to revoke
     */
    @Modifying
    @Query("UPDATE RefreshTokenEntity r SET r.revoked = true WHERE r.token = :token")
    void revokeByToken(@Param("token") String token);
}
