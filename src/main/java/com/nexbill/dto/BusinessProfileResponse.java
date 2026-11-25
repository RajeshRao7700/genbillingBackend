package com.nexbill.dto;

import com.nexbill.entity.BusinessMode;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BusinessProfileResponse {

    private Long id;
    private String businessName;
    private String ownerName;
    private String businessPhone;
    private String alternatePhone;
    private String businessEmail;
    private String businessAddress;
    private String city;
    private String state;
    private String pincode;
    private String country;
    private String gstin;
    private String pan;
    private String logoUrl;
    private String website;
    private BusinessMode businessMode;
    private String businessType;
    private String defaultInvoiceFooter;
    private Integer nextInvoiceNumber;
    private Instant createdAt;
    private Instant updatedAt;
}
