package com.nexbill.dto;

import com.nexbill.entity.BusinessMode;
import com.nexbill.entity.Tenant;
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
@Schema(description = "Tenant Business Data Transfer Object")
public class TenantDto {

    private Long id;
    private String businessName;
    private String ownerName;
    private String code;
    private String email;
    private String phone;
    private String gstin;
    private String address;
    private String city;
    private String state;
    private String pincode;
    private BusinessMode businessMode;
    private String status;
    private Boolean isActive;
    private Instant createdAt;

    public static TenantDto fromEntity(Tenant tenant) {
        if (tenant == null) {
            return null;
        }
        return TenantDto.builder()
                .id(tenant.getId())
                .businessName(tenant.getEffectiveBusinessName())
                .ownerName(tenant.getOwnerName())
                .code(tenant.getCode())
                .email(tenant.getEmail())
                .phone(tenant.getPhone())
                .gstin(tenant.getGstin())
                .address(tenant.getAddress())
                .city(tenant.getCity())
                .state(tenant.getState())
                .pincode(tenant.getPincode())
                .businessMode(tenant.getBusinessMode())
                .status(tenant.getStatus())
                .isActive(tenant.getIsActive())
                .createdAt(tenant.getCreatedAt())
                .build();
    }
}
