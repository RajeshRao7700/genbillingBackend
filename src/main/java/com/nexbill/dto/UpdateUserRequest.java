package com.nexbill.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "User Profile Update Request")
public class UpdateUserRequest {

    @Size(max = 150, message = "Full name cannot exceed 150 characters")
    @Schema(description = "Updated full name", example = "Pavan Chowdary")
    private String fullName;

    @Size(min = 7, max = 25, message = "Phone number must be between 7 and 25 characters")
    @Schema(description = "Updated phone number", example = "+91 98490 99999")
    private String phone;

    @Email(message = "Invalid email format")
    @Size(max = 120, message = "Email cannot exceed 120 characters")
    @Schema(description = "Updated email address", example = "pavan.new@chowmowfoods.com")
    private String email;
}
