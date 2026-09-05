package com.clothmarket.user.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.OffsetDateTime;

/**
 * Generic API response wrapper for simple acknowledgement messages.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Generic operation acknowledgement response")
public class ApiResponseDto {

    @Schema(description = "Whether the operation succeeded", example = "true")
    private boolean success;

    @Schema(description = "Descriptive confirmation message", example = "Logged out successfully")
    private String message;

    @Schema(description = "Timestamp of response generation", example = "2026-08-26T18:00:00Z")
    @Builder.Default
    private OffsetDateTime timestamp = OffsetDateTime.now();

    /**
     * Factory helper for creating success messages.
     *
     * @param message text message describing result
     * @return ApiResponseDto instance
     */
    public static ApiResponseDto ofSuccess(String message) {
        return ApiResponseDto.builder()
                .success(true)
                .message(message)
                .timestamp(OffsetDateTime.now())
                .build();
    }
}
