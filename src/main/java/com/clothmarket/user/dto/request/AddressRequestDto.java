package com.clothmarket.user.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Request DTO for creating a new shipping address for the authenticated user.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Create address payload")
public class AddressRequestDto {

    @Schema(description = "Address label or type tag", example = "Home", allowableValues = {"Home", "Work", "Other"})
    @Size(max = 50, message = "Label cannot exceed 50 characters")
    private String label;

    @Schema(description = "Primary street address, flat/house number, building", example = "Flat 402, Sunshine Apartments, 12th Main Road", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "Address line 1 is required")
    @Size(max = 255, message = "Line 1 cannot exceed 255 characters")
    private String line1;

    @Schema(description = "Secondary address line, landmark, or area", example = "Near Indiranagar Metro Station")
    @Size(max = 255, message = "Line 2 cannot exceed 255 characters")
    private String line2;

    @Schema(description = "City name", example = "Bengaluru", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "City is required")
    @Size(max = 100, message = "City cannot exceed 100 characters")
    private String city;

    @Schema(description = "State or Province name", example = "Karnataka", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "State is required")
    @Size(max = 100, message = "State cannot exceed 100 characters")
    private String state;

    @Schema(description = "Postal or ZIP code", example = "560038", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "Postal code is required")
    @Size(max = 20, message = "Postal code cannot exceed 20 characters")
    private String postalCode;

    @Schema(description = "Country name", example = "India", defaultValue = "India")
    @Size(max = 100, message = "Country cannot exceed 100 characters")
    @Builder.Default
    private String country = "India";

    @Schema(description = "Recipient contact phone number for delivery", example = "+919876543210", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "Contact phone number is required")
    @Pattern(regexp = "^[+0-9\\s-]{7,20}$", message = "Phone number format is invalid")
    private String phone;

    @Schema(description = "Flag marking this address as the user's primary default address", example = "true")
    @Builder.Default
    private Boolean isDefault = false;
}
