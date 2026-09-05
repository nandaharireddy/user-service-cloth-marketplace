package com.clothmarket.user.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.OffsetDateTime;

/**
 * Standardized error response model returned across all service endpoints.
 * Conforms to the project-wide error schema: timestamp, status, error, message, path.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Standard API error response payload")
public class ErrorResponseDto {

    @Schema(description = "Timestamp when the error occurred", example = "2026-08-26T18:00:00Z")
    private OffsetDateTime timestamp;

    @Schema(description = "HTTP status code", example = "400")
    private int status;

    @Schema(description = "HTTP error phrase", example = "Bad Request")
    private String error;

    @Schema(description = "Detailed error message or validation summary", example = "Email is already registered")
    private String message;

    @Schema(description = "Request URI path where the error occurred", example = "/auth/register")
    private String path;
}
