package com.clothmarket.user.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Request DTO for partially updating an existing shipping address.
 * All fields are optional to allow selective attribute modifications.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Update address payload with optional fields for partial modification")
public class AddressUpdateRequestDto {

    @Schema(description = "Updated address label or type tag", example = "Work")
    @Size(max = 50, message = "Label cannot exceed 50 characters")
    private String label;

    @Schema(description = "Updated primary street address", example = "Suite 500, Tech Park")
    @Size(max = 255, message = "Line 1 cannot exceed 255 characters")
    private String line1;

    @Schema(description = "Updated secondary address line or landmark", example = "Tower B")
    @Size(max = 255, message = "Line 2 cannot exceed 255 characters")
    private String line2;

    @Schema(description = "Updated city name", example = "Bengaluru")
    @Size(max = 100, message = "City cannot exceed 100 characters")
    private String city;

    @Schema(description = "Updated state name", example = "Karnataka")
    @Size(max = 100, message = "State cannot exceed 100 characters")
    private String state;

    @Schema(description = "Updated postal code", example = "560100")
    @Size(max = 20, message = "Postal code cannot exceed 20 characters")
    private String postalCode;

    @Schema(description = "Updated country name", example = "India")
    @Size(max = 100, message = "Country cannot exceed 100 characters")
    private String country;

    @Schema(description = "Updated contact phone number", example = "+919876543210")
    @Pattern(regexp = "^$|^[+0-9\\s-]{7,20}$", message = "Phone number format is invalid")
    private String phone;

    @Schema(description = "Set whether this address becomes the default address", example = "true")
    private Boolean isDefault;
}
