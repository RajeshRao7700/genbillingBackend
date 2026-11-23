package com.nexbill.dto;

import com.nexbill.entity.UserRole;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Authentication Success Response with JWT and Tenant Identity")
public class AuthResponse {

    @Schema(description = "JWT Bearer access token")
    private String accessToken;

    @Builder.Default
    @Schema(description = "Token type", example = "Bearer")
    private String tokenType = "Bearer";

    @Schema(description = "Token validity duration in milliseconds", example = "86400000")
    private Long expiresIn;

    @Schema(description = "User role", example = "OWNER")
    private UserRole role;

    @Schema(description = "Authenticated user profile")
    private UserDto user;

    @Schema(description = "Associated tenant/business profile")
    private TenantDto tenant;
}
