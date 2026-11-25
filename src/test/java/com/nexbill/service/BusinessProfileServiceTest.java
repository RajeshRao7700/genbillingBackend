package com.nexbill.service;

import com.nexbill.dto.BusinessProfileResponse;
import com.nexbill.dto.BusinessSettingsResponse;
import com.nexbill.dto.UpdateBusinessProfileRequest;
import com.nexbill.dto.UpdateBusinessSettingsRequest;
import com.nexbill.entity.BusinessMode;
import com.nexbill.entity.BusinessSettings;
import com.nexbill.entity.Tenant;
import com.nexbill.exception.ResourceNotFoundException;
import com.nexbill.multitenancy.CurrentTenantService;
import com.nexbill.repository.BusinessSettingsRepository;
import com.nexbill.repository.TenantRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("BusinessProfileService Tests")
class BusinessProfileServiceTest {

    @Mock private CurrentTenantService currentTenantService;
    @Mock private TenantRepository tenantRepository;
    @Mock private BusinessSettingsRepository businessSettingsRepository;

    @InjectMocks private BusinessProfileService businessProfileService;

    private Tenant tenant;
    private BusinessSettings settings;

    @BeforeEach
    void setUp() {
        tenant = Tenant.builder()
                .id(1L)
                .name("Chowmow Foods")
                .businessName("Chowmow Foods")
                .ownerName("Ram Kumar")
                .code("CHOWMOW")
                .email("info@chowmow.in")
                .phone("9876543210")
                .city("Hyderabad")
                .state("Telangana")
                .pincode("500001")
                .country("India")
                .businessMode(BusinessMode.BUSINESS)
                .status("ACTIVE")
                .isActive(true)
                .build();

        settings = BusinessSettings.builder()
                .id(1L)
                .tenantId(1L)
                .invoicePrefix("INV-2026-")
                .currency("INR")
                .currencySymbol("₹")
                .defaultTaxRate(new BigDecimal("5.00"))
                .taxScheme("REGULAR")
                .isGstEnabled(true)
                .enableRoundOff(true)
                .build();
    }

    // -------------------------------------------------------------------------
    // getProfile
    // -------------------------------------------------------------------------

    @Test
    @DisplayName("getProfile: returns business profile for authenticated tenant")
    void getProfile_returnsProfile() {
        when(currentTenantService.getRequiredCurrentTenant()).thenReturn(tenant);

        BusinessProfileResponse response = businessProfileService.getProfile();

        assertThat(response.getBusinessName()).isEqualTo("Chowmow Foods");
        assertThat(response.getOwnerName()).isEqualTo("Ram Kumar");
        assertThat(response.getCity()).isEqualTo("Hyderabad");
        assertThat(response.getCountry()).isEqualTo("India");
        verify(currentTenantService).getRequiredCurrentTenant();
    }

    // -------------------------------------------------------------------------
    // updateProfile
    // -------------------------------------------------------------------------

    @Test
    @DisplayName("updateProfile: updates allowed fields and saves")
    void updateProfile_updatesFields() {
        when(currentTenantService.getRequiredCurrentTenant()).thenReturn(tenant);
        when(tenantRepository.save(any(Tenant.class))).thenAnswer(inv -> inv.getArgument(0));

        UpdateBusinessProfileRequest request = UpdateBusinessProfileRequest.builder()
                .businessName("Chowmow Foods Updated")
                .city("Secunderabad")
                .gstin("22AAAAA0000A1Z5")
                .build();

        BusinessProfileResponse response = businessProfileService.updateProfile(request);

        assertThat(response.getBusinessName()).isEqualTo("Chowmow Foods Updated");
        assertThat(response.getCity()).isEqualTo("Secunderabad");
        assertThat(response.getGstin()).isEqualTo("22AAAAA0000A1Z5");
        verify(tenantRepository).save(any(Tenant.class));
    }

    @Test
    @DisplayName("updateProfile: null fields are ignored (partial update)")
    void updateProfile_nullFieldsIgnored() {
        when(currentTenantService.getRequiredCurrentTenant()).thenReturn(tenant);
        when(tenantRepository.save(any(Tenant.class))).thenAnswer(inv -> inv.getArgument(0));

        UpdateBusinessProfileRequest request = UpdateBusinessProfileRequest.builder()
                .city("Kukatpally")
                .build();

        BusinessProfileResponse response = businessProfileService.updateProfile(request);

        assertThat(response.getBusinessName()).isEqualTo("Chowmow Foods"); // unchanged
        assertThat(response.getCity()).isEqualTo("Kukatpally");             // updated
    }

    // -------------------------------------------------------------------------
    // getSettings
    // -------------------------------------------------------------------------

    @Test
    @DisplayName("getSettings: returns settings for tenant")
    void getSettings_returnsSettings() {
        when(currentTenantService.getRequiredTenantId()).thenReturn(1L);
        when(businessSettingsRepository.findByTenantId(1L)).thenReturn(Optional.of(settings));

        BusinessSettingsResponse response = businessProfileService.getSettings();

        assertThat(response.getCurrency()).isEqualTo("INR");
        assertThat(response.getTaxScheme()).isEqualTo("REGULAR");
    }

    @Test
    @DisplayName("getSettings: throws ResourceNotFoundException if settings not found")
    void getSettings_throwsIfNotFound() {
        when(currentTenantService.getRequiredTenantId()).thenReturn(99L);
        when(businessSettingsRepository.findByTenantId(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> businessProfileService.getSettings())
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Business settings not found");
    }

    // -------------------------------------------------------------------------
    // updateSettings
    // -------------------------------------------------------------------------

    @Test
    @DisplayName("updateSettings: updates and saves settings")
    void updateSettings_updatesFields() {
        when(currentTenantService.getRequiredTenantId()).thenReturn(1L);
        when(businessSettingsRepository.findByTenantId(1L)).thenReturn(Optional.of(settings));
        when(businessSettingsRepository.save(any(BusinessSettings.class))).thenAnswer(inv -> inv.getArgument(0));

        UpdateBusinessSettingsRequest request = UpdateBusinessSettingsRequest.builder()
                .invoicePrefix("BILL-2026-")
                .defaultTaxRate(new BigDecimal("18.00"))
                .build();

        BusinessSettingsResponse response = businessProfileService.updateSettings(request);

        assertThat(response.getInvoicePrefix()).isEqualTo("BILL-2026-");
        assertThat(response.getDefaultTaxRate()).isEqualByComparingTo(new BigDecimal("18.00"));
        verify(businessSettingsRepository).save(any(BusinessSettings.class));
    }
}
