package com.nexbill.service;

import com.nexbill.dto.BusinessProfileResponse;
import com.nexbill.dto.BusinessSettingsResponse;
import com.nexbill.dto.UpdateBusinessProfileRequest;
import com.nexbill.dto.UpdateBusinessSettingsRequest;
import com.nexbill.entity.BusinessSettings;
import com.nexbill.entity.Tenant;
import com.nexbill.exception.ResourceNotFoundException;
import com.nexbill.multitenancy.CurrentTenantService;
import com.nexbill.repository.BusinessSettingsRepository;
import com.nexbill.repository.TenantRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class BusinessProfileService {

    private final CurrentTenantService currentTenantService;
    private final TenantRepository tenantRepository;
    private final BusinessSettingsRepository businessSettingsRepository;

    // -------------------------------------------------------------------------
    // Business Profile
    // -------------------------------------------------------------------------

    @Transactional(readOnly = true)
    public BusinessProfileResponse getProfile() {
        Tenant tenant = currentTenantService.getRequiredCurrentTenant();
        return toProfileResponse(tenant);
    }

    @Transactional
    public BusinessProfileResponse updateProfile(UpdateBusinessProfileRequest request) {
        Tenant tenant = currentTenantService.getRequiredCurrentTenant();
        log.info("Updating business profile for tenantId={}", tenant.getId());

        if (request.getBusinessName() != null && !request.getBusinessName().isBlank()) {
            tenant.setBusinessName(request.getBusinessName());
            tenant.setName(request.getBusinessName());
        }
        if (request.getOwnerName() != null) {
            tenant.setOwnerName(request.getOwnerName());
        }
        if (request.getBusinessPhone() != null) {
            tenant.setPhone(request.getBusinessPhone());
        }
        if (request.getAlternatePhone() != null) {
            tenant.setAlternatePhone(request.getAlternatePhone());
        }
        if (request.getBusinessEmail() != null) {
            tenant.setEmail(request.getBusinessEmail());
        }
        if (request.getBusinessAddress() != null) {
            tenant.setAddress(request.getBusinessAddress());
        }
        if (request.getCity() != null) {
            tenant.setCity(request.getCity());
        }
        if (request.getState() != null) {
            tenant.setState(request.getState());
        }
        if (request.getPincode() != null) {
            tenant.setPincode(request.getPincode());
        }
        if (request.getCountry() != null) {
            tenant.setCountry(request.getCountry());
        }
        if (request.getGstin() != null) {
            tenant.setGstin(request.getGstin());
        }
        if (request.getPan() != null) {
            tenant.setPan(request.getPan());
        }
        if (request.getLogoUrl() != null) {
            tenant.setLogoUrl(request.getLogoUrl());
        }
        if (request.getWebsite() != null) {
            tenant.setWebsite(request.getWebsite());
        }
        if (request.getBusinessMode() != null) {
            tenant.setBusinessMode(request.getBusinessMode());
        }
        if (request.getBusinessType() != null) {
            tenant.setBusinessType(request.getBusinessType());
        }
        if (request.getDefaultInvoiceFooter() != null) {
            tenant.setInvoiceFooterMessage(request.getDefaultInvoiceFooter());
        }

        Tenant saved = tenantRepository.save(tenant);
        log.info("Business profile updated for tenantId={}", saved.getId());
        return toProfileResponse(saved);
    }

    // -------------------------------------------------------------------------
    // Business Settings
    // -------------------------------------------------------------------------

    @Transactional(readOnly = true)
    public BusinessSettingsResponse getSettings() {
        Long tenantId = currentTenantService.getRequiredTenantId();
        BusinessSettings settings = businessSettingsRepository.findByTenantId(tenantId)
                .orElseThrow(() -> new ResourceNotFoundException("Business settings not found for tenant"));
        return toSettingsResponse(settings);
    }

    @Transactional
    public BusinessSettingsResponse updateSettings(UpdateBusinessSettingsRequest request) {
        Long tenantId = currentTenantService.getRequiredTenantId();
        BusinessSettings settings = businessSettingsRepository.findByTenantId(tenantId)
                .orElseThrow(() -> new ResourceNotFoundException("Business settings not found for tenant"));

        log.info("Updating business settings for tenantId={}", tenantId);

        if (request.getInvoicePrefix() != null && !request.getInvoicePrefix().isBlank()) {
            settings.setInvoicePrefix(request.getInvoicePrefix());
        }
        if (request.getQuotationPrefix() != null && !request.getQuotationPrefix().isBlank()) {
            settings.setQuotationPrefix(request.getQuotationPrefix());
        }
        if (request.getPurchasePrefix() != null && !request.getPurchasePrefix().isBlank()) {
            settings.setPurchasePrefix(request.getPurchasePrefix());
        }
        if (request.getCurrency() != null && !request.getCurrency().isBlank()) {
            settings.setCurrency(request.getCurrency());
        }
        if (request.getCurrencySymbol() != null && !request.getCurrencySymbol().isBlank()) {
            settings.setCurrencySymbol(request.getCurrencySymbol());
        }
        if (request.getDefaultTaxRate() != null) {
            settings.setDefaultTaxRate(request.getDefaultTaxRate());
        }
        if (request.getTaxScheme() != null && !request.getTaxScheme().isBlank()) {
            settings.setTaxScheme(request.getTaxScheme());
        }
        if (request.getBankName() != null) {
            settings.setBankName(request.getBankName());
        }
        if (request.getAccountNumber() != null) {
            settings.setAccountNumber(request.getAccountNumber());
        }
        if (request.getIfscCode() != null) {
            settings.setIfscCode(request.getIfscCode());
        }
        if (request.getUpiId() != null) {
            settings.setUpiId(request.getUpiId());
        }
        if (request.getTermsConditions() != null) {
            settings.setTermsConditions(request.getTermsConditions());
        }
        if (request.getIsGstEnabled() != null) {
            settings.setIsGstEnabled(request.getIsGstEnabled());
        }
        if (request.getEnableRoundOff() != null) {
            settings.setEnableRoundOff(request.getEnableRoundOff());
        }

        BusinessSettings saved = businessSettingsRepository.save(settings);
        log.info("Business settings updated for tenantId={}", tenantId);
        return toSettingsResponse(saved);
    }

    // -------------------------------------------------------------------------
    // Mappers
    // -------------------------------------------------------------------------

    private BusinessProfileResponse toProfileResponse(Tenant tenant) {
        return BusinessProfileResponse.builder()
                .id(tenant.getId())
                .businessName(tenant.getEffectiveBusinessName())
                .ownerName(tenant.getOwnerName())
                .businessPhone(tenant.getPhone())
                .alternatePhone(tenant.getAlternatePhone())
                .businessEmail(tenant.getEmail())
                .businessAddress(tenant.getAddress())
                .city(tenant.getCity())
                .state(tenant.getState())
                .pincode(tenant.getPincode())
                .country(tenant.getCountry())
                .gstin(tenant.getGstin())
                .pan(tenant.getPan())
                .logoUrl(tenant.getLogoUrl())
                .website(tenant.getWebsite())
                .businessMode(tenant.getBusinessMode())
                .businessType(tenant.getBusinessType())
                .defaultInvoiceFooter(tenant.getInvoiceFooterMessage())
                .nextInvoiceNumber(tenant.getNextInvoiceNumber())
                .createdAt(tenant.getCreatedAt())
                .updatedAt(tenant.getUpdatedAt())
                .build();
    }

    private BusinessSettingsResponse toSettingsResponse(BusinessSettings s) {
        return BusinessSettingsResponse.builder()
                .id(s.getId())
                .tenantId(s.getTenantId())
                .invoicePrefix(s.getInvoicePrefix())
                .quotationPrefix(s.getQuotationPrefix())
                .purchasePrefix(s.getPurchasePrefix())
                .currency(s.getCurrency())
                .currencySymbol(s.getCurrencySymbol())
                .defaultTaxRate(s.getDefaultTaxRate())
                .taxScheme(s.getTaxScheme())
                .bankName(s.getBankName())
                .accountNumber(s.getAccountNumber())
                .ifscCode(s.getIfscCode())
                .upiId(s.getUpiId())
                .termsConditions(s.getTermsConditions())
                .isGstEnabled(s.getIsGstEnabled())
                .enableRoundOff(s.getEnableRoundOff())
                .createdAt(s.getCreatedAt())
                .updatedAt(s.getUpdatedAt())
                .build();
    }
}
