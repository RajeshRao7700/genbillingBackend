package com.nexbill.dto;

import com.nexbill.entity.User;
import com.nexbill.entity.UserRole;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "User Profile Data Transfer Object")
public class UserDto {

    private Long id;
    private Long tenantId;
    private Long branchId;
    private String username;
    private String fullName;
    private String email;
    private String phone;
    private UserRole role;
    private String status;
    private Boolean isActive;
    private Instant createdAt;

    public static UserDto fromEntity(User user) {
        if (user == null) {
            return null;
        }
        return UserDto.builder()
                .id(user.getId())
                .tenantId(user.getTenantId())
                .branchId(user.getBranchId())
                .username(user.getUsername())
                .fullName(user.getFullName())
                .email(user.getEmail())
                .phone(user.getPhone())
                .role(user.getRole())
                .status(user.getStatus())
                .isActive(user.getIsActive())
                .createdAt(user.getCreatedAt())
                .build();
    }
}
