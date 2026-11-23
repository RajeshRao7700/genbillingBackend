package com.nexbill.dto;

import com.fasterxml.jackson.annotation.JsonAlias;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Authentication Login Request")
public class LoginRequest {

    @NotBlank(message = "Username or email is required")
    @JsonAlias({"username", "email", "identifier"})
    @Schema(description = "Registered username or email address", example = "chowmow")
    private String usernameOrEmail;

    @NotBlank(message = "Password is required")
    @Schema(description = "Account password", example = "Chowmow@123")
    private String password;
}
