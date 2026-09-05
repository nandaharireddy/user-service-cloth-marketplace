package com.clothmarket.user.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.util.Base64;
import java.util.Date;
import java.util.UUID;

/**
 * Implementation of {@link JwtService} for issuing, parsing, and verifying HS256 JWT tokens.
 * Complies with auth-and-security.md specifications for claims and expirations.
 */
@Service
public class JwtServiceImpl implements JwtService {

    private static final Logger log = LoggerFactory.getLogger(JwtServiceImpl.class);

    private final SecretKey secretKey;
    private final long accessTokenExpirationMs;
    private final SecureRandom secureRandom = new SecureRandom();

    /**
     * Constructs JwtServiceImpl injecting secret and expiration configurations.
     *
     * @param secret                  HS256 secret key string (min 256 bits)
     * @param accessTokenExpirationMs validity duration in milliseconds (default 15 mins)
     */
    public JwtServiceImpl(
            @Value("${jwt.secret}") String secret,
            @Value("${jwt.access-token-expiration-ms:900000}") long accessTokenExpirationMs) {
        // Ensure secret key is at least 32 bytes (256 bits)
        byte[] keyBytes = secret.getBytes(StandardCharsets.UTF_8);
        if (keyBytes.length < 32) {
            byte[] padded = new byte[32];
            System.arraycopy(keyBytes, 0, padded, 0, keyBytes.length);
            this.secretKey = Keys.hmacShaKeyFor(padded);
        } else {
            this.secretKey = Keys.hmacShaKeyFor(keyBytes);
        }
        this.accessTokenExpirationMs = accessTokenExpirationMs;
    }

    /**
     * Generates a signed HS256 access token containing sub, role, vendor_id, iat, and exp claims.
     *
     * @param userId   the user's unique identifier (subject)
     * @param role     user role (ROLE_CUSTOMER or ROLE_VENDOR)
     * @param vendorId vendor identifier if user is a vendor, or null
     * @return signed JWT string
     */
    @Override
    public String generateAccessToken(Long userId, String role, Long vendorId) {
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + accessTokenExpirationMs);

        String formattedRole = role != null && role.startsWith("ROLE_") ? role : "ROLE_" + (role != null ? role : "CUSTOMER");

        return Jwts.builder()
                .subject(String.valueOf(userId))
                .claim("role", formattedRole)
                .claim("vendor_id", vendorId)
                .issuedAt(now)
                .expiration(expiryDate)
                .signWith(secretKey, Jwts.SIG.HS256)
                .compact();
    }

    /**
     * Generates a secure cryptographically random refresh token string.
     *
     * @return unique refresh token string
     */
    @Override
    public String generateRefreshToken() {
        byte[] randomBytes = new byte[32];
        secureRandom.nextBytes(randomBytes);
        String randomHex = Base64.getUrlEncoder().withoutPadding().encodeToString(randomBytes);
        return UUID.randomUUID().toString().replace("-", "") + randomHex;
    }

    /**
     * Parses and extracts all claims from a JWT token string.
     *
     * @param token the JWT token string
     * @return Claims payload
     */
    @Override
    public Claims extractAllClaims(String token) {
        return Jwts.parser()
                .verifyWith(secretKey)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    /**
     * Extracts the user identifier (subject) from a JWT token.
     *
     * @param token the JWT token string
     * @return user ID as Long
     */
    @Override
    public Long extractUserId(String token) {
        Claims claims = extractAllClaims(token);
        String subject = claims.getSubject();
        return subject != null ? Long.parseLong(subject) : null;
    }

    /**
     * Extracts the user role from a JWT token.
     *
     * @param token the JWT token string
     * @return user role string
     */
    @Override
    public String extractRole(String token) {
        Claims claims = extractAllClaims(token);
        return claims.get("role", String.class);
    }

    /**
     * Extracts the optional vendor ID from a JWT token.
     *
     * @param token the JWT token string
     * @return vendor ID as Long, or null
     */
    @Override
    public Long extractVendorId(String token) {
        Claims claims = extractAllClaims(token);
        Object vendorId = claims.get("vendor_id");
        if (vendorId instanceof Number number) {
            return number.longValue();
        }
        return null;
    }

    /**
     * Validates signature, structure, and expiration of a JWT token.
     *
     * @param token the JWT token string
     * @return true if token is valid and not expired, false otherwise
     */
    @Override
    public boolean isTokenValid(String token) {
        try {
            Claims claims = extractAllClaims(token);
            return claims.getExpiration().after(new Date());
        } catch (ExpiredJwtException ex) {
            log.debug("JWT token is expired: {}", ex.getMessage());
            return false;
        } catch (JwtException | IllegalArgumentException ex) {
            log.debug("Invalid JWT signature or format: {}", ex.getMessage());
            return false;
        }
    }

    /**
     * Returns the access token validity duration in seconds.
     *
     * @return expiration duration in seconds
     */
    @Override
    public long getAccessTokenExpirationSeconds() {
        return accessTokenExpirationMs / 1000;
    }
}
