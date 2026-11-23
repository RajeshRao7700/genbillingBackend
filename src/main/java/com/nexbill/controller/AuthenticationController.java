package com.nexbill.controller;

import com.nexbill.common.ApiResponse;
import com.nexbill.dto.AuthResponse;
import com.nexbill.dto.CurrentUserResponse;
import com.nexbill.dto.LoginRequest;
import com.nexbill.dto.RegisterRequest;
import com.nexbill.security.CustomUserDetails;
import com.nexbill.service.AuthenticationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Authentication & Onboarding", description = "Tenant business onboarding, secure login, and user context resolution")
public class AuthenticationController {

    private final AuthenticationService authenticationService;

    @PostMapping("/register")
    @Operation(
            summary = "Register Business Owner & Tenant",
            description = "Creates a new tenant business entity, creates the primary business owner user with OWNER role, hashes password via BCrypt, and returns an initial JWT authentication token within an atomic transaction."
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "201", description = "Business and Owner successfully registered"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Validation error in registration payload"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "409", description = "Email or username already in use")
    })
    public ResponseEntity<ApiResponse<AuthResponse>> register(@Valid @RequestBody RegisterRequest request) {
        log.info("Received registration request for business: {}", request.getBusinessName());
        AuthResponse response = authenticationService.register(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Business and owner registered successfully", response));
    }

    @PostMapping("/login")
    @Operation(
            summary = "Authenticate User & Issue JWT",
            description = "Authenticates user credentials (by email or username) using BCrypt verification, validates active tenant state, and returns a signed multi-tenant JWT access token."
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Authentication successful"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Invalid credentials or inactive account")
    })
    public ResponseEntity<ApiResponse<AuthResponse>> login(@Valid @RequestBody LoginRequest request) {
        AuthResponse response = authenticationService.login(request);
        return ResponseEntity.ok(ApiResponse.success("Login successful", response));
    }

    @GetMapping("/me")
    @SecurityRequirement(name = "BearerAuth")
    @Operation(
            summary = "Get Current Authenticated User Context",
            description = "Retrieves profile and business information for the currently authenticated identity, strictly bound to the authenticated JWT claims and database record."
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Current user profile retrieved"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Missing or expired JWT access token")
    })
    public ResponseEntity<ApiResponse<CurrentUserResponse>> getCurrentUser(
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        CurrentUserResponse response = authenticationService.getCurrentUser(userDetails);
        return ResponseEntity.ok(ApiResponse.success("Authenticated user retrieved successfully", response));
    }
}
