package com.clothmarket.user.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Response DTO returned upon successful login or registration.
 * Contains short-lived JWT access token, long-lived refresh token, and user profile.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Authentication response with JWT tokens and user summary")
public class AuthResponseDto {

    @Schema(description = "Short-lived JWT Bearer access token (15-minute validity)", example = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...")
    @JsonProperty("access_token")
    private String accessToken;

    @Schema(description = "Long-lived server-side refresh token", example = "550e8400-e29b-41d4-a716-446655440000")
    @JsonProperty("refresh_token")
    private String refreshToken;

    @Schema(description = "Token authentication scheme", example = "Bearer")
    @JsonProperty("token_type")
    @Builder.Default
    private String tokenType = "Bearer";

    @Schema(description = "Access token validity in seconds", example = "900")
    @JsonProperty("expires_in")
    private Long expiresIn;

    @Schema(description = "Authenticated user profile details")
    private UserResponseDto user;
}
