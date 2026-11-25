package com.nexbill.dto;

import com.nexbill.entity.BusinessMode;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdateBusinessProfileRequest {

    @Size(max = 150, message = "Business name must not exceed 150 characters")
    private String businessName;

    @Size(max = 150, message = "Owner name must not exceed 150 characters")
    private String ownerName;

    @Pattern(regexp = "^[+]?[0-9\\-\\s]{7,25}$", message = "Invalid phone number format")
    private String businessPhone;

    @Pattern(regexp = "^[+]?[0-9\\-\\s]{7,25}$", message = "Invalid alternate phone number format")
    private String alternatePhone;

    @Email(message = "Invalid business email format")
    @Size(max = 100)
    private String businessEmail;

    @Size(max = 2000, message = "Address must not exceed 2000 characters")
    private String businessAddress;

    @Size(max = 80)
    private String city;

    @Size(max = 80)
    private String state;

    @Pattern(regexp = "^[1-9][0-9]{5}$", message = "PIN code must be a valid 6-digit Indian PIN code")
    private String pincode;

    @Size(max = 80)
    private String country;

    @Pattern(regexp = "^[0-9]{2}[A-Z]{5}[0-9]{4}[A-Z]{1}[1-9A-Z]{1}Z[0-9A-Z]{1}$",
            message = "Invalid GSTIN format (e.g. 22AAAAA0000A1Z5)")
    private String gstin;

    @Pattern(regexp = "^[A-Z]{5}[0-9]{4}[A-Z]{1}$", message = "Invalid PAN format (e.g. ABCDE1234F)")
    private String pan;

    @Size(max = 500)
    private String logoUrl;

    @Size(max = 150)
    private String website;

    private BusinessMode businessMode;

    @Size(max = 80)
    private String businessType;

    private String defaultInvoiceFooter;
}
