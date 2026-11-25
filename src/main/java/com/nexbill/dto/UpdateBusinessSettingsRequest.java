package com.nexbill.dto;

import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdateBusinessSettingsRequest {

    @Size(max = 30, message = "Invoice prefix must not exceed 30 characters")
    private String invoicePrefix;

    @Size(max = 30, message = "Quotation prefix must not exceed 30 characters")
    private String quotationPrefix;

    @Size(max = 30, message = "Purchase prefix must not exceed 30 characters")
    private String purchasePrefix;

    @Size(max = 10)
    private String currency;

    @Size(max = 10)
    private String currencySymbol;

    @DecimalMin(value = "0.0", inclusive = true, message = "Default tax rate cannot be negative")
    @DecimalMax(value = "100.0", message = "Default tax rate cannot exceed 100%")
    private BigDecimal defaultTaxRate;

    @Pattern(regexp = "^(REGULAR|COMPOSITION)$", message = "Tax scheme must be REGULAR or COMPOSITION")
    private String taxScheme;

    @Size(max = 100)
    private String bankName;

    @Size(max = 50)
    private String accountNumber;

    @Pattern(regexp = "^[A-Z]{4}0[A-Z0-9]{6}$|^$", message = "Invalid IFSC code format")
    private String ifscCode;

    @Size(max = 100)
    private String upiId;

    private String termsConditions;

    private Boolean isGstEnabled;

    private Boolean enableRoundOff;
}
