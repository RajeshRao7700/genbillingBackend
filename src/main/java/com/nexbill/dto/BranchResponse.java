package com.nexbill.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BranchResponse {

    private Long id;
    private Long tenantId;
    private String name;
    private String branchCode;
    private String phone;
    private String email;
    private String address;
    private String city;
    private String state;
    private String pincode;
    private String gstin;
    private Boolean isMainBranch;
    private String status;
    private Boolean isActive;
    private Instant createdAt;
    private Instant updatedAt;
}
