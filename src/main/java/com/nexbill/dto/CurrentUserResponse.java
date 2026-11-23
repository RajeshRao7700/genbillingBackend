package com.nexbill.dto;

import com.nexbill.entity.BusinessMode;
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
@Schema(description = "Currently Authenticated User and Business Context")
public class CurrentUserResponse {

    @Schema(description = "Unique user identifier", example = "1")
    private Long userId;

    @Schema(description = "Username used for login", example = "chowmow")
    private String username;

    @Schema(description = "Full name of the user", example = "Chowmow Foods Admin")
    private String name;

    @Schema(description = "Email address", example = "admin@chowmowfoods.com")
    private String email;

    @Schema(description = "Phone number", example = "+91 98490 12345")
    private String phone;

    @Schema(description = "Assigned security role", example = "OWNER")
    private UserRole role;

    @Schema(description = "Bound tenant identifier", example = "1")
    private Long tenantId;

    @Schema(description = "Bound tenant business name", example = "Chowmow Foods")
    private String businessName;

    @Schema(description = "Business operating mode", example = "BUSINESS")
    private BusinessMode businessMode;
}
