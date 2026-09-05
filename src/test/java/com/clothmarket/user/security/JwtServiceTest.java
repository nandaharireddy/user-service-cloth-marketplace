package com.clothmarket.user.security;

import io.jsonwebtoken.Claims;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Unit tests for {@link JwtServiceImpl} validating token issuance, claim structure, and verification.
 */
class JwtServiceTest {

    private JwtServiceImpl jwtService;
    private final String secret = "clothmarketplace-super-secret-jwt-key-must-be-at-least-256-bits-long-for-hs256-algorithm";
    private final long expirationMs = 900000; // 15 mins

    @BeforeEach
    void setUp() {
        jwtService = new JwtServiceImpl(secret, expirationMs);
    }

    @Test
    @DisplayName("generateAccessToken: creates valid token containing sub, role, vendor_id, iat, exp")
    void generateAccessToken_ClaimsStructureValid() {
        Long userId = 12345L;
        String role = "ROLE_CUSTOMER";
        Long vendorId = null;

        String token = jwtService.generateAccessToken(userId, role, vendorId);

        assertThat(token).isNotBlank();
        assertThat(jwtService.isTokenValid(token)).isTrue();
        assertThat(jwtService.extractUserId(token)).isEqualTo(userId);
        assertThat(jwtService.extractRole(token)).isEqualTo(role);
        assertThat(jwtService.extractVendorId(token)).isNull();

        Claims claims = jwtService.extractAllClaims(token);
        assertThat(claims.getSubject()).isEqualTo("12345");
        assertThat(claims.get("role")).isEqualTo("ROLE_CUSTOMER");
        assertThat(claims.getIssuedAt()).isNotNull();
        assertThat(claims.getExpiration()).isNotNull();
        // Check expiration is ~15 mins after issuedAt
        long diffSeconds = (claims.getExpiration().getTime() - claims.getIssuedAt().getTime()) / 1000;
        assertThat(diffSeconds).isEqualTo(900);
    }

    @Test
    @DisplayName("generateAccessToken: vendor token correctly embeds vendor_id and ROLE_VENDOR")
    void generateAccessToken_VendorClaimsValid() {
        Long userId = 500L;
        String role = "ROLE_VENDOR";
        Long vendorId = 42L;

        String token = jwtService.generateAccessToken(userId, role, vendorId);

        assertThat(jwtService.extractUserId(token)).isEqualTo(500L);
        assertThat(jwtService.extractRole(token)).isEqualTo("ROLE_VENDOR");
        assertThat(jwtService.extractVendorId(token)).isEqualTo(42L);
    }

    @Test
    @DisplayName("isTokenValid: returns false for invalid token strings")
    void isTokenValid_InvalidToken_ReturnsFalse() {
        assertThat(jwtService.isTokenValid("invalid.jwt.token")).isFalse();
        assertThat(jwtService.isTokenValid("")).isFalse();
    }
}
