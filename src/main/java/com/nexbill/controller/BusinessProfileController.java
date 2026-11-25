package com.nexbill.controller;

import com.nexbill.common.ApiResponse;
import com.nexbill.dto.BusinessProfileResponse;
import com.nexbill.dto.BusinessSettingsResponse;
import com.nexbill.dto.UpdateBusinessProfileRequest;
import com.nexbill.dto.UpdateBusinessSettingsRequest;
import com.nexbill.service.BusinessProfileService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/business")
@RequiredArgsConstructor
@Tag(name = "Business Profile", description = "Business profile and settings management")
public class BusinessProfileController {

    private final BusinessProfileService businessProfileService;

    // -------------------------------------------------------------------------
    // Business Profile
    // -------------------------------------------------------------------------

    @GetMapping("/profile")
    @PreAuthorize("hasAnyRole('OWNER','ADMIN','MANAGER','CASHIER','STAFF')")
    @Operation(summary = "Get business profile of the authenticated tenant")
    public ResponseEntity<ApiResponse<BusinessProfileResponse>> getProfile() {
        BusinessProfileResponse profile = businessProfileService.getProfile();
        return ResponseEntity.ok(ApiResponse.success("Business profile retrieved successfully", profile));
    }

    @PutMapping("/profile")
    @PreAuthorize("hasAnyRole('OWNER','ADMIN')")
    @Operation(summary = "Update business profile – OWNER or ADMIN only")
    public ResponseEntity<ApiResponse<BusinessProfileResponse>> updateProfile(
            @Valid @RequestBody UpdateBusinessProfileRequest request) {
        BusinessProfileResponse profile = businessProfileService.updateProfile(request);
        return ResponseEntity.ok(ApiResponse.success("Business profile updated successfully", profile));
    }

    // -------------------------------------------------------------------------
    // Business Settings
    // -------------------------------------------------------------------------

    @GetMapping("/settings")
    @PreAuthorize("hasAnyRole('OWNER','ADMIN')")
    @Operation(summary = "Get business settings – OWNER or ADMIN only")
    public ResponseEntity<ApiResponse<BusinessSettingsResponse>> getSettings() {
        BusinessSettingsResponse settings = businessProfileService.getSettings();
        return ResponseEntity.ok(ApiResponse.success("Business settings retrieved successfully", settings));
    }

    @PutMapping("/settings")
    @PreAuthorize("hasAnyRole('OWNER','ADMIN')")
    @Operation(summary = "Update business settings – OWNER or ADMIN only")
    public ResponseEntity<ApiResponse<BusinessSettingsResponse>> updateSettings(
            @Valid @RequestBody UpdateBusinessSettingsRequest request) {
        BusinessSettingsResponse settings = businessProfileService.updateSettings(request);
        return ResponseEntity.ok(ApiResponse.success("Business settings updated successfully", settings));
    }
}
