package com.clothmarket.user.service.impl;

import com.clothmarket.user.dao.RefreshTokenDao;
import com.clothmarket.user.dao.UserDao;
import com.clothmarket.user.dto.request.LoginRequestDto;
import com.clothmarket.user.dto.request.LogoutRequestDto;
import com.clothmarket.user.dto.request.RefreshTokenRequestDto;
import com.clothmarket.user.dto.request.RegisterRequestDto;
import com.clothmarket.user.dto.response.AuthResponseDto;
import com.clothmarket.user.dto.response.TokenRefreshResponseDto;
import com.clothmarket.user.dto.response.UserResponseDto;
import com.clothmarket.user.entity.RefreshTokenEntity;
import com.clothmarket.user.entity.UserEntity;
import com.clothmarket.user.exception.InvalidTokenException;
import com.clothmarket.user.exception.ResourceNotFoundException;
import com.clothmarket.user.exception.UserAlreadyExistsException;
import com.clothmarket.user.mapper.UserMapper;
import com.clothmarket.user.security.JwtService;
import com.clothmarket.user.service.AuthService;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.time.temporal.ChronoUnit;
import java.util.Optional;

/**
 * Service implementation for user identity orchestration, authentication, and JWT lifecycle.
 * Interacts with DAOs exclusively, fulfilling the 4-layer architecture standard.
 */
@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private static final Logger log = LoggerFactory.getLogger(AuthServiceImpl.class);

    private final UserDao userDao;
    private final RefreshTokenDao refreshTokenDao;
    private final UserMapper userMapper;
    private final JwtService jwtService;
    private final PasswordEncoder passwordEncoder;

    @Value("${jwt.refresh-token-expiration-ms:604800000}")
    private long refreshTokenExpirationMs;

    /**
     * Registers a new user account with BCrypt-hashed credentials and issues initial authentication tokens.
     *
     * @param request user registration payload containing email, phone, and plaintext password
     * @return AuthResponseDto containing access token, refresh token, and sanitized user profile
     */
    @Override
    @Transactional
    public AuthResponseDto register(RegisterRequestDto request) {
        String normalizedEmail = request.getEmail().trim().toLowerCase();

        if (userDao.existsByEmail(normalizedEmail)) {
            log.warn("Registration rejected: Email {} is already registered", normalizedEmail);
            throw new UserAlreadyExistsException("An account with email " + normalizedEmail + " already exists.");
        }

        if (request.getPhone() != null && !request.getPhone().isBlank()) {
            String normalizedPhone = request.getPhone().trim();
            if (userDao.existsByPhone(normalizedPhone)) {
                log.warn("Registration rejected: Phone {} is already registered", normalizedPhone);
                throw new UserAlreadyExistsException("An account with phone " + normalizedPhone + " already exists.");
            }
        }

        // Hash plaintext password using BCrypt with work factor 12
        String passwordHash = passwordEncoder.encode(request.getPassword());
        UserEntity userEntity = userMapper.toEntity(request, passwordHash);
        UserEntity savedUser = userDao.save(userEntity);

        log.info("Successfully registered new user with ID: {}", savedUser.getId());

        String role = Boolean.TRUE.equals(savedUser.getIsVendor()) ? "ROLE_VENDOR" : "ROLE_CUSTOMER";
        String accessToken = jwtService.generateAccessToken(savedUser.getId(), role, null);
        String rawRefreshToken = jwtService.generateRefreshToken();

        // Persist refresh token server-side for revocation support
        saveRefreshToken(savedUser.getId(), rawRefreshToken);

        UserResponseDto userDto = userMapper.toResponseDto(savedUser);

        return AuthResponseDto.builder()
                .accessToken(accessToken)
                .refreshToken(rawRefreshToken)
                .tokenType("Bearer")
                .expiresIn(jwtService.getAccessTokenExpirationSeconds())
                .user(userDto)
                .build();
    }

    /**
     * Authenticates user credentials (email or phone + password) and issues authentication tokens.
     *
     * @param request login payload containing email/phone and password
     * @return AuthResponseDto containing access token, refresh token, and user profile
     */
    @Override
    @Transactional
    public AuthResponseDto login(LoginRequestDto request) {
        String identifier = request.getEmail().trim();

        // Support login via either email address or phone number
        Optional<UserEntity> userOptional = identifier.contains("@")
                ? userDao.findByEmail(identifier.toLowerCase())
                : userDao.findByPhone(identifier);

        if (userOptional.isEmpty()) {
            // Check email lookup fallback in case phone without @ was searched
            userOptional = userDao.findByEmail(identifier.toLowerCase());
        }

        if (userOptional.isEmpty()) {
            log.warn("Login failed: User not found for identifier: {}", identifier);
            throw new BadCredentialsException("Invalid email/phone or password");
        }

        UserEntity user = userOptional.get();

        if (!passwordEncoder.matches(request.getPassword(), user.getPasswordHash())) {
            log.warn("Login failed: Incorrect password for user ID: {}", user.getId());
            throw new BadCredentialsException("Invalid email/phone or password");
        }

        log.info("User ID: {} logged in successfully", user.getId());

        String role = Boolean.TRUE.equals(user.getIsVendor()) ? "ROLE_VENDOR" : "ROLE_CUSTOMER";
        String accessToken = jwtService.generateAccessToken(user.getId(), role, null);
        String rawRefreshToken = jwtService.generateRefreshToken();

        saveRefreshToken(user.getId(), rawRefreshToken);

        UserResponseDto userDto = userMapper.toResponseDto(user);

        return AuthResponseDto.builder()
                .accessToken(accessToken)
                .refreshToken(rawRefreshToken)
                .tokenType("Bearer")
                .expiresIn(jwtService.getAccessTokenExpirationSeconds())
                .user(userDto)
                .build();
    }

    /**
     * Exchanges an active, unexpired, non-revoked refresh token for a newly minted access token.
     *
     * @param request refresh token payload
     * @return TokenRefreshResponseDto containing new access token and active refresh token
     */
    @Override
    @Transactional
    public TokenRefreshResponseDto refreshToken(RefreshTokenRequestDto request) {
        String rawToken = request.getRefreshToken().trim();

        RefreshTokenEntity tokenEntity = refreshTokenDao.findByToken(rawToken)
                .orElseThrow(() -> new InvalidTokenException("Invalid refresh token"));

        if (Boolean.TRUE.equals(tokenEntity.getRevoked())) {
            log.warn("Refresh token attempt with revoked token for user ID: {}", tokenEntity.getUserId());
            throw new InvalidTokenException("Refresh token has been revoked. Please log in again.");
        }

        if (tokenEntity.getExpiryDate().isBefore(OffsetDateTime.now(ZoneOffset.UTC))) {
            log.warn("Refresh token attempt with expired token for user ID: {}", tokenEntity.getUserId());
            throw new InvalidTokenException("Refresh token has expired. Please log in again.");
        }

        UserEntity user = userDao.findById(tokenEntity.getUserId())
                .orElseThrow(() -> new ResourceNotFoundException("User associated with token no longer exists"));

        String role = Boolean.TRUE.equals(user.getIsVendor()) ? "ROLE_VENDOR" : "ROLE_CUSTOMER";
        String newAccessToken = jwtService.generateAccessToken(user.getId(), role, null);

        log.debug("Successfully refreshed access token for user ID: {}", user.getId());

        return TokenRefreshResponseDto.builder()
                .accessToken(newAccessToken)
                .refreshToken(rawToken)
                .tokenType("Bearer")
                .expiresIn(jwtService.getAccessTokenExpirationSeconds())
                .build();
    }

    /**
     * Invalidates refresh tokens server-side to terminate active user sessions.
     *
     * @param request       optional logout payload containing specific token to invalidate
     * @param currentUserId optional authenticated user ID to revoke all active tokens
     */
    @Override
    @Transactional
    public void logout(LogoutRequestDto request, Long currentUserId) {
        if (request != null && request.getRefreshToken() != null && !request.getRefreshToken().isBlank()) {
            refreshTokenDao.revokeByToken(request.getRefreshToken().trim());
            log.info("Explicitly revoked refresh token during logout");
        }

        if (currentUserId != null) {
            refreshTokenDao.revokeAllByUserId(currentUserId);
            log.info("Revoked all active refresh tokens for user ID: {}", currentUserId);
        }
    }

    private void saveRefreshToken(Long userId, String rawToken) {
        OffsetDateTime expiryDate = OffsetDateTime.now(ZoneOffset.UTC)
                .plus(refreshTokenExpirationMs, ChronoUnit.MILLIS);

        RefreshTokenEntity refreshTokenEntity = RefreshTokenEntity.builder()
                .userId(userId)
                .token(rawToken)
                .expiryDate(expiryDate)
                .revoked(false)
                .build();

        refreshTokenDao.save(refreshTokenEntity);
    }
}
