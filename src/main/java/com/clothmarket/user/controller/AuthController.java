package com.clothmarket.user.controller;

import com.clothmarket.user.dto.request.LoginRequestDto;
import com.clothmarket.user.dto.request.LogoutRequestDto;
import com.clothmarket.user.dto.request.RefreshTokenRequestDto;
import com.clothmarket.user.dto.request.RegisterRequestDto;
import com.clothmarket.user.dto.response.ApiResponseDto;
import com.clothmarket.user.dto.response.AuthResponseDto;
import com.clothmarket.user.dto.response.ErrorResponseDto;
import com.clothmarket.user.dto.response.TokenRefreshResponseDto;
import com.clothmarket.user.security.UserPrincipal;
import com.clothmarket.user.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Controller handling user authentication, credential verification, registration,
 * and JWT / refresh token lifecycle endpoints.
 */
@RestController
@RequestMapping({"/auth", "/api/v1/auth"})
@RequiredArgsConstructor
@Tag(name = "Authentication", description = "Endpoints for user registration, login, token refresh, and logout")
public class AuthController {

    private final AuthService authService;

    /**
     * Registers a new customer or vendor account and issues initial tokens.
     *
     * @param request registration request payload
     * @return 201 Created with AuthResponseDto
     */
    @PostMapping("/register")
    @Operation(summary = "Register a new user", description = "Creates a new user account with BCrypt password hashing and returns JWT access + refresh tokens.")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "User registered successfully",
                    content = @Content(schema = @Schema(implementation = AuthResponseDto.class))),
            @ApiResponse(responseCode = "400", description = "Invalid request input or validation failure",
                    content = @Content(schema = @Schema(implementation = ErrorResponseDto.class))),
            @ApiResponse(responseCode = "409", description = "Email or phone already registered",
                    content = @Content(schema = @Schema(implementation = ErrorResponseDto.class))),
            @ApiResponse(responseCode = "429", description = "Rate limit exceeded",
                    content = @Content(schema = @Schema(implementation = ErrorResponseDto.class)))
    })
    public ResponseEntity<AuthResponseDto> register(@Valid @RequestBody RegisterRequestDto request) {
        AuthResponseDto response = authService.register(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * Authenticates user credentials and issues access and refresh tokens.
     *
     * @param request login request payload
     * @return 200 OK with AuthResponseDto
     */
    @PostMapping("/login")
    @Operation(summary = "User login", description = "Verifies user credentials (email/phone and password) and issues a short-lived access token and refresh token.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Login successful",
                    content = @Content(schema = @Schema(implementation = AuthResponseDto.class))),
            @ApiResponse(responseCode = "400", description = "Malformed request payload",
                    content = @Content(schema = @Schema(implementation = ErrorResponseDto.class))),
            @ApiResponse(responseCode = "401", description = "Invalid email/phone or password",
                    content = @Content(schema = @Schema(implementation = ErrorResponseDto.class))),
            @ApiResponse(responseCode = "429", description = "Rate limit exceeded",
                    content = @Content(schema = @Schema(implementation = ErrorResponseDto.class)))
    })
    public ResponseEntity<AuthResponseDto> login(@Valid @RequestBody LoginRequestDto request) {
        AuthResponseDto response = authService.login(request);
        return ResponseEntity.ok(response);
    }

    /**
     * Exchanges a valid server-side refresh token for a new access token.
     *
     * @param request refresh token payload
     * @return 200 OK with TokenRefreshResponseDto
     */
    @PostMapping("/refresh")
    @Operation(summary = "Refresh access token", description = "Exchanges an active refresh token for a new JWT access token without requiring re-login.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Token refreshed successfully",
                    content = @Content(schema = @Schema(implementation = TokenRefreshResponseDto.class))),
            @ApiResponse(responseCode = "400", description = "Missing or malformed refresh token",
                    content = @Content(schema = @Schema(implementation = ErrorResponseDto.class))),
            @ApiResponse(responseCode = "401", description = "Invalid, expired, or revoked refresh token",
                    content = @Content(schema = @Schema(implementation = ErrorResponseDto.class)))
    })
    public ResponseEntity<TokenRefreshResponseDto> refreshToken(
            @Valid @RequestBody RefreshTokenRequestDto request) {
        TokenRefreshResponseDto response = authService.refreshToken(request);
        return ResponseEntity.ok(response);
    }

    /**
     * Logs out the user by revoking the refresh token server-side.
     *
     * @param request   optional logout payload with token to revoke
     * @param principal authenticated user principal if token was provided
     * @return 200 OK with confirmation message
     */
    @PostMapping("/logout")
    @Operation(summary = "User logout", description = "Invalidates the refresh token server-side to terminate active user session.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Logged out successfully",
                    content = @Content(schema = @Schema(implementation = ApiResponseDto.class)))
    })
    public ResponseEntity<ApiResponseDto> logout(
            @RequestBody(required = false) LogoutRequestDto request,
            @AuthenticationPrincipal UserPrincipal principal) {
        Long userId = principal != null ? principal.getUserId() : null;
        authService.logout(request, userId);
        return ResponseEntity.ok(ApiResponseDto.ofSuccess("Logged out successfully"));
    }
}
