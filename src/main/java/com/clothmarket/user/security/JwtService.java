package com.clothmarket.user.security;

import io.jsonwebtoken.Claims;

/**
 * Service interface for JSON Web Token (JWT) generation, extraction, and validation.
 */
public interface JwtService {

    /**
     * Generates a signed HS256 access token containing sub, role, vendor_id, iat, and exp claims.
     *
     * @param userId   the user's unique identifier (subject)
     * @param role     user role (ROLE_CUSTOMER or ROLE_VENDOR)
     * @param vendorId vendor identifier if user is a vendor, or null
     * @return signed JWT string
     */
    String generateAccessToken(Long userId, String role, Long vendorId);

    /**
     * Generates a secure cryptographically random refresh token string.
     *
     * @return unique refresh token string
     */
    String generateRefreshToken();

    /**
     * Parses and extracts all claims from a JWT token string.
     *
     * @param token the JWT token string
     * @return Claims payload
     */
    Claims extractAllClaims(String token);

    /**
     * Extracts the user identifier (subject) from a JWT token.
     *
     * @param token the JWT token string
     * @return user ID as Long
     */
    Long extractUserId(String token);

    /**
     * Extracts the user role from a JWT token.
     *
     * @param token the JWT token string
     * @return user role string
     */
    String extractRole(String token);

    /**
     * Extracts the optional vendor ID from a JWT token.
     *
     * @param token the JWT token string
     * @return vendor ID as Long, or null
     */
    Long extractVendorId(String token);

    /**
     * Validates signature, structure, and expiration of a JWT token.
     *
     * @param token the JWT token string
     * @return true if token is valid and not expired, false otherwise
     */
    boolean isTokenValid(String token);

    /**
     * Returns the access token validity duration in seconds.
     *
     * @return expiration duration in seconds
     */
    long getAccessTokenExpirationSeconds();
}
