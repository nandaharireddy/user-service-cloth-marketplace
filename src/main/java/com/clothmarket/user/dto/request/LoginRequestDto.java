package com.clothmarket.user.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Request DTO for user login and credential verification.
 * Accepts either email or phone along with the password.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "User login credentials payload")
public class LoginRequestDto {

    @Schema(description = "Registered email address or phone number", example = "jane.doe@example.com", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "Email or phone is required")
    private String email;

    @Schema(description = "Account password", example = "StrongP@ssw0rd!", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "Password is required")
    private String password;
}
