package com.nexbill.dto;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.nexbill.entity.BusinessMode;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Business Owner & Tenant Registration Request")
public class RegisterRequest {

    @NotBlank(message = "Business name is required")
    @Size(max = 150, message = "Business name cannot exceed 150 characters")
    @Schema(description = "Registered commercial trade name", example = "Chowmow Foods")
    private String businessName;

    @NotBlank(message = "Owner full name is required")
    @Size(max = 150, message = "Owner name cannot exceed 150 characters")
    @Schema(description = "Full name of the business proprietor or director", example = "Pavan Chowdary")
    private String ownerName;

    @NotBlank(message = "Email is required")
    @Email(message = "Invalid email format")
    @Size(max = 120, message = "Email cannot exceed 120 characters")
    @Schema(description = "Primary business and owner communication email", example = "pavan@chowmowfoods.com")
    private String email;

    @NotBlank(message = "Username is required")
    @Size(min = 3, max = 80, message = "Username must be between 3 and 80 characters")
    @Schema(description = "Unique alphanumeric login identifier", example = "pavan_owner")
    private String username;

    @NotBlank(message = "Phone number is required")
    @Size(min = 7, max = 25, message = "Phone number must be between 7 and 25 characters")
    @JsonAlias({"phoneNumber", "phone"})
    @Schema(description = "Primary contact mobile number", example = "+91 98490 12345")
    private String phoneNumber;

    @NotBlank(message = "Password is required")
    @Size(min = 6, max = 100, message = "Password must be at least 6 characters")
    @Schema(description = "Secret login password", example = "Chowmow@2026")
    private String password;

    @NotNull(message = "Business mode is required (SIMPLE or BUSINESS)")
    @Builder.Default
    @Schema(description = "POS operating mode: SIMPLE (quick total entry) or BUSINESS (itemized inventory)", example = "BUSINESS")
    private BusinessMode businessMode = BusinessMode.BUSINESS;

    public String getEffectivePhone() {
        return phoneNumber;
    }
}
