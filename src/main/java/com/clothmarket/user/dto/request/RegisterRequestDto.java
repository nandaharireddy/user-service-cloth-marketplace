package com.clothmarket.user.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Request DTO for customer or vendor user registration.
 * Captures user identity, contact details, and account credentials.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "User registration payload")
public class RegisterRequestDto {

    @Schema(description = "Customer's full legal or preferred name", example = "Jane Doe", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "Full name is required")
    @Size(min = 2, max = 150, message = "Full name must be between 2 and 150 characters")
    private String fullName;

    @Schema(description = "Unique user email address", example = "jane.doe@example.com", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "Email is required")
    @Email(message = "Email must be a valid email address")
    @Size(max = 150, message = "Email cannot exceed 150 characters")
    private String email;

    @Schema(description = "Optional user phone number", example = "+919876543210")
    @Pattern(regexp = "^$|^[+0-9\\s-]{7,20}$", message = "Phone number must be valid if provided")
    private String phone;

    @Schema(description = "Plaintext password (hashed using BCrypt on arrival)", example = "StrongP@ssw0rd!", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "Password is required")
    @Size(min = 8, max = 100, message = "Password must be between 8 and 100 characters")
    private String password;

    @Schema(description = "Indicates if the user is registering as a seller/vendor", example = "false")
    @Builder.Default
    private Boolean isVendor = false;
}
