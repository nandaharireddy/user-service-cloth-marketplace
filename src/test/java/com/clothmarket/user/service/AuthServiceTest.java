package com.clothmarket.user.service;

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
import com.clothmarket.user.exception.UserAlreadyExistsException;
import com.clothmarket.user.mapper.UserMapper;
import com.clothmarket.user.security.JwtService;
import com.clothmarket.user.service.impl.AuthServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Unit tests for {@link AuthServiceImpl} verifying registration, login, refresh token, and logout logic.
 */
@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private UserDao userDao;

    @Mock
    private RefreshTokenDao refreshTokenDao;

    @Mock
    private UserMapper userMapper;

    @Mock
    private JwtService jwtService;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private AuthServiceImpl authService;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(authService, "refreshTokenExpirationMs", 604800000L);
    }

    @Test
    @DisplayName("register: successfully registers a new customer user and returns tokens")
    void register_Success() {
        RegisterRequestDto request = RegisterRequestDto.builder()
                .fullName("John Doe")
                .email("john@example.com")
                .phone("+919876543210")
                .password("Password123!")
                .isVendor(false)
                .build();

        UserEntity userEntity = UserEntity.builder()
                .id(1L)
                .fullName("John Doe")
                .email("john@example.com")
                .phone("+919876543210")
                .passwordHash("hashed-password")
                .isVendor(false)
                .build();

        UserResponseDto userDto = UserResponseDto.builder()
                .id(1L)
                .fullName("John Doe")
                .email("john@example.com")
                .phone("+919876543210")
                .isVendor(false)
                .build();

        when(userDao.existsByEmail("john@example.com")).thenReturn(false);
        when(userDao.existsByPhone("+919876543210")).thenReturn(false);
        when(passwordEncoder.encode("Password123!")).thenReturn("hashed-password");
        when(userMapper.toEntity(eq(request), eq("hashed-password"))).thenReturn(userEntity);
        when(userDao.save(any(UserEntity.class))).thenReturn(userEntity);
        when(jwtService.generateAccessToken(1L, "ROLE_CUSTOMER", null)).thenReturn("jwt-access-token");
        when(jwtService.generateRefreshToken()).thenReturn("refresh-token-uuid");
        when(jwtService.getAccessTokenExpirationSeconds()).thenReturn(900L);
        when(userMapper.toResponseDto(userEntity)).thenReturn(userDto);

        AuthResponseDto response = authService.register(request);

        assertThat(response).isNotNull();
        assertThat(response.getAccessToken()).isEqualTo("jwt-access-token");
        assertThat(response.getRefreshToken()).isEqualTo("refresh-token-uuid");
        assertThat(response.getExpiresIn()).isEqualTo(900L);
        assertThat(response.getUser().getEmail()).isEqualTo("john@example.com");

        verify(refreshTokenDao).save(any(RefreshTokenEntity.class));
    }

    @Test
    @DisplayName("register: throws UserAlreadyExistsException when email is already in use")
    void register_DuplicateEmail_ThrowsException() {
        RegisterRequestDto request = RegisterRequestDto.builder()
                .fullName("John Doe")
                .email("john@example.com")
                .password("Password123!")
                .build();

        when(userDao.existsByEmail("john@example.com")).thenReturn(true);

        assertThatThrownBy(() -> authService.register(request))
                .isInstanceOf(UserAlreadyExistsException.class)
                .hasMessageContaining("john@example.com");

        verify(userDao, never()).save(any());
    }

    @Test
    @DisplayName("login: successfully verifies credentials and returns access token")
    void login_Success() {
        LoginRequestDto request = LoginRequestDto.builder()
                .email("john@example.com")
                .password("Password123!")
                .build();

        UserEntity userEntity = UserEntity.builder()
                .id(1L)
                .fullName("John Doe")
                .email("john@example.com")
                .passwordHash("hashed-password")
                .isVendor(false)
                .build();

        UserResponseDto userDto = UserResponseDto.builder()
                .id(1L)
                .fullName("John Doe")
                .email("john@example.com")
                .isVendor(false)
                .build();

        when(userDao.findByEmail("john@example.com")).thenReturn(Optional.of(userEntity));
        when(passwordEncoder.matches("Password123!", "hashed-password")).thenReturn(true);
        when(jwtService.generateAccessToken(1L, "ROLE_CUSTOMER", null)).thenReturn("jwt-access-token");
        when(jwtService.generateRefreshToken()).thenReturn("refresh-token-uuid");
        when(jwtService.getAccessTokenExpirationSeconds()).thenReturn(900L);
        when(userMapper.toResponseDto(userEntity)).thenReturn(userDto);

        AuthResponseDto response = authService.login(request);

        assertThat(response).isNotNull();
        assertThat(response.getAccessToken()).isEqualTo("jwt-access-token");
        assertThat(response.getRefreshToken()).isEqualTo("refresh-token-uuid");
        verify(refreshTokenDao).save(any(RefreshTokenEntity.class));
    }

    @Test
    @DisplayName("login: throws BadCredentialsException when password does not match")
    void login_WrongPassword_ThrowsException() {
        LoginRequestDto request = LoginRequestDto.builder()
                .email("john@example.com")
                .password("WrongPassword")
                .build();

        UserEntity userEntity = UserEntity.builder()
                .id(1L)
                .email("john@example.com")
                .passwordHash("hashed-password")
                .build();

        when(userDao.findByEmail("john@example.com")).thenReturn(Optional.of(userEntity));
        when(passwordEncoder.matches("WrongPassword", "hashed-password")).thenReturn(false);

        assertThatThrownBy(() -> authService.login(request))
                .isInstanceOf(BadCredentialsException.class)
                .hasMessageContaining("Invalid email/phone or password");
    }

    @Test
    @DisplayName("refreshToken: successfully issues a new access token for valid refresh token")
    void refreshToken_Success() {
        RefreshTokenRequestDto request = RefreshTokenRequestDto.builder()
                .refreshToken("valid-refresh-token")
                .build();

        RefreshTokenEntity tokenEntity = RefreshTokenEntity.builder()
                .id(10L)
                .userId(1L)
                .token("valid-refresh-token")
                .revoked(false)
                .expiryDate(OffsetDateTime.now(ZoneOffset.UTC).plusDays(5))
                .build();

        UserEntity user = UserEntity.builder()
                .id(1L)
                .email("john@example.com")
                .isVendor(false)
                .build();

        when(refreshTokenDao.findByToken("valid-refresh-token")).thenReturn(Optional.of(tokenEntity));
        when(userDao.findById(1L)).thenReturn(Optional.of(user));
        when(jwtService.generateAccessToken(1L, "ROLE_CUSTOMER", null)).thenReturn("new-jwt-token");
        when(jwtService.getAccessTokenExpirationSeconds()).thenReturn(900L);

        TokenRefreshResponseDto response = authService.refreshToken(request);

        assertThat(response).isNotNull();
        assertThat(response.getAccessToken()).isEqualTo("new-jwt-token");
        assertThat(response.getRefreshToken()).isEqualTo("valid-refresh-token");
    }

    @Test
    @DisplayName("refreshToken: throws InvalidTokenException when token is revoked")
    void refreshToken_Revoked_ThrowsException() {
        RefreshTokenRequestDto request = RefreshTokenRequestDto.builder()
                .refreshToken("revoked-token")
                .build();

        RefreshTokenEntity tokenEntity = RefreshTokenEntity.builder()
                .id(10L)
                .userId(1L)
                .token("revoked-token")
                .revoked(true)
                .expiryDate(OffsetDateTime.now(ZoneOffset.UTC).plusDays(5))
                .build();

        when(refreshTokenDao.findByToken("revoked-token")).thenReturn(Optional.of(tokenEntity));

        assertThatThrownBy(() -> authService.refreshToken(request))
                .isInstanceOf(InvalidTokenException.class)
                .hasMessageContaining("revoked");
    }

    @Test
    @DisplayName("logout: revokes refresh token server-side")
    void logout_Success() {
        LogoutRequestDto request = LogoutRequestDto.builder()
                .refreshToken("token-to-revoke")
                .build();

        authService.logout(request, 1L);

        verify(refreshTokenDao).revokeByToken("token-to-revoke");
        verify(refreshTokenDao).revokeAllByUserId(1L);
    }
}
