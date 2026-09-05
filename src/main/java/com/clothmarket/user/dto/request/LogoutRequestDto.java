package com.clothmarket.user.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Request DTO for user logout, providing the refresh token to be invalidated.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "User logout payload for token invalidation")
public class LogoutRequestDto {

    @Schema(description = "Optional refresh token to specifically revoke", example = "550e8400-e29b-41d4-a716-446655440000")
    private String refreshToken;
}
