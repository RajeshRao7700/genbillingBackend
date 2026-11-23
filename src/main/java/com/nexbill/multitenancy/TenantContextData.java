package com.nexbill.multitenancy;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TenantContextData {
    private Long tenantId;
    private String businessName;
    private String tenantCode;
}
