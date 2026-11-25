package com.nexbill.dto;

import com.nexbill.entity.UserRole;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EmployeeResponse {

    private Long id;
    private Long tenantId;
    private Long branchId;
    private String branchName;
    private String fullName;
    private String username;
    private String email;
    private String phone;
    private UserRole role;
    private String status;
    private Boolean isActive;
    private Instant createdAt;
    private Instant updatedAt;
}
