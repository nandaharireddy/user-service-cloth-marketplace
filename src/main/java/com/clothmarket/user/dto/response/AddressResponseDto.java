package com.clothmarket.user.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.OffsetDateTime;

/**
 * Response DTO representing a saved user address entity.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Address response data")
public class AddressResponseDto {

    @Schema(description = "Unique address identifier", example = "42")
    private Long id;

    @Schema(description = "Associated user identifier", example = "101")
    private Long userId;

    @Schema(description = "Address label", example = "Home")
    private String label;

    @Schema(description = "Primary street address", example = "Flat 402, Sunshine Apartments, 12th Main Road")
    private String line1;

    @Schema(description = "Secondary address line", example = "Near Indiranagar Metro Station")
    private String line2;

    @Schema(description = "City", example = "Bengaluru")
    private String city;

    @Schema(description = "State", example = "Karnataka")
    private String state;

    @Schema(description = "Postal code", example = "560038")
    private String postalCode;

    @Schema(description = "Country", example = "India")
    private String country;

    @Schema(description = "Contact phone number", example = "+919876543210")
    private String phone;

    @Schema(description = "Whether this is the user's default shipping address", example = "true")
    private Boolean isDefault;

    @Schema(description = "Creation timestamp", example = "2026-08-26T18:00:00Z")
    private OffsetDateTime createdAt;
}
