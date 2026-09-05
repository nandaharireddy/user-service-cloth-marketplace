package com.clothmarket.user.service;

import com.clothmarket.user.dto.request.LoginRequestDto;
import com.clothmarket.user.dto.request.LogoutRequestDto;
import com.clothmarket.user.dto.request.RefreshTokenRequestDto;
import com.clothmarket.user.dto.request.RegisterRequestDto;
import com.clothmarket.user.dto.response.AuthResponseDto;
import com.clothmarket.user.dto.response.TokenRefreshResponseDto;

/**
 * Service interface for user identity management, credential authentication,
 * JWT access token issuance, and server-side refresh token lifecycle.
 */
public interface AuthService {

    /**
     * Registers a new user account with BCrypt-hashed credentials and issues initial authentication tokens.
     *
     * @param request user registration payload containing email, phone, and plaintext password
     * @return AuthResponseDto containing access token, refresh token, and sanitized user profile
     */
    AuthResponseDto register(RegisterRequestDto request);

    /**
     * Authenticates user credentials (email or phone + password) and issues authentication tokens.
     *
     * @param request login payload containing email/phone and password
     * @return AuthResponseDto containing access token, refresh token, and user profile
     */
    AuthResponseDto login(LoginRequestDto request);

    /**
     * Exchanges an active, unexpired, non-revoked refresh token for a newly minted access token.
     *
     * @param request refresh token payload
     * @return TokenRefreshResponseDto containing new access token and active refresh token
     */
    TokenRefreshResponseDto refreshToken(RefreshTokenRequestDto request);

    /**
     * Invalidates refresh tokens server-side to terminate active user sessions.
     *
     * @param request       optional logout payload containing specific token to invalidate
     * @param currentUserId optional authenticated user ID to revoke all active tokens
     */
    void logout(LogoutRequestDto request, Long currentUserId);
}
