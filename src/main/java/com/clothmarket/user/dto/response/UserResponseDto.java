package com.clothmarket.user.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.OffsetDateTime;

/**
 * Response DTO representing public/safe user profile data.
 * Does not expose password hashes or sensitive security internal tokens.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "User profile response data")
public class UserResponseDto {

    @Schema(description = "Unique user identifier", example = "101")
    private Long id;

    @Schema(description = "User's full name", example = "Jane Doe")
    private String fullName;

    @Schema(description = "User's registered email address", example = "jane.doe@example.com")
    private String email;

    @Schema(description = "User's registered phone number", example = "+919876543210")
    private String phone;

    @Schema(description = "Whether the user is an onboarded seller/vendor", example = "false")
    private Boolean isVendor;

    @Schema(description = "Timestamp when the account was registered", example = "2026-08-26T18:00:00Z")
    private OffsetDateTime createdAt;

    @Schema(description = "Timestamp when the account was last updated", example = "2026-08-26T18:00:00Z")
    private OffsetDateTime updatedAt;
}
