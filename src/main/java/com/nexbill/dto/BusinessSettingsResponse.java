package com.nexbill.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.Instant;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BusinessSettingsResponse {

    private Long id;
    private Long tenantId;
    private String invoicePrefix;
    private String quotationPrefix;
    private String purchasePrefix;
    private String currency;
    private String currencySymbol;
    private BigDecimal defaultTaxRate;
    private String taxScheme;
    private String bankName;
    private String accountNumber;
    private String ifscCode;
    private String upiId;
    private String termsConditions;
    private Boolean isGstEnabled;
    private Boolean enableRoundOff;
    private Instant createdAt;
    private Instant updatedAt;
}
