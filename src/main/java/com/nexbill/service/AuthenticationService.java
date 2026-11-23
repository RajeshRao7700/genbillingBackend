package com.nexbill.service;

import com.nexbill.dto.*;
import com.nexbill.entity.*;
import com.nexbill.exception.DuplicateResourceException;
import com.nexbill.exception.InvalidCredentialsException;
import com.nexbill.exception.ResourceNotFoundException;
import com.nexbill.repository.BranchRepository;
import com.nexbill.repository.BusinessSettingsRepository;
import com.nexbill.repository.TenantRepository;
import com.nexbill.repository.UserRepository;
import com.nexbill.security.CustomUserDetails;
import com.nexbill.security.JwtService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.Locale;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthenticationService {

    private final UserRepository userRepository;
    private final TenantRepository tenantRepository;
    private final BranchRepository branchRepository;
    private final BusinessSettingsRepository businessSettingsRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    @Transactional
    public AuthResponse register(RegisterRequest request) {
        log.info("Processing business owner registration for email={}, username={}, business={}",
                request.getEmail(), request.getUsername(), request.getBusinessName());

        // 1. Validate uniqueness
        if (userRepository.existsByEmailIgnoreCase(request.getEmail().trim())) {
            throw new DuplicateResourceException("Email is already registered: " + request.getEmail());
        }
        if (userRepository.existsByUsernameIgnoreCase(request.getUsername().trim())) {
            throw new DuplicateResourceException("Username is already taken: " + request.getUsername());
        }

        // 2. Generate unique tenant code
        String baseCode = request.getBusinessName().trim().toLowerCase(Locale.ROOT)
                .replaceAll("[^a-z0-9]", "");
        if (baseCode.length() < 3) {
            baseCode = "shop" + UUID.randomUUID().toString().substring(0, 4);
        }
        String tenantCode = baseCode;
        if (tenantRepository.existsByCode(tenantCode)) {
            tenantCode = baseCode + "-" + UUID.randomUUID().toString().substring(0, 4);
        }

        // 3. Create Tenant
        Tenant tenant = Tenant.builder()
                .name(request.getBusinessName().trim())
                .businessName(request.getBusinessName().trim())
                .ownerName(request.getOwnerName().trim())
                .code(tenantCode)
                .email(request.getEmail().trim())
                .phone(request.getEffectivePhone().trim())
                .businessMode(request.getBusinessMode())
                .status("ACTIVE")
                .isActive(true)
                .build();
        tenant = tenantRepository.save(tenant);
        log.info("Created tenant record id={}, code={}", tenant.getId(), tenant.getCode());

        // 4. Create Main Branch for Tenant
        Branch mainBranch = Branch.builder()
                .tenantId(tenant.getId())
                .name(request.getBusinessName().trim() + " - Main Branch")
                .branchCode("MAIN")
                .phone(request.getEffectivePhone().trim())
                .isMainBranch(true)
                .isActive(true)
                .build();
        mainBranch = branchRepository.save(mainBranch);

        // 5. Create Default Business Settings
        BusinessSettings settings = BusinessSettings.builder()
                .tenantId(tenant.getId())
                .invoicePrefix("INV-2026-")
                .quotationPrefix("QT-2026-")
                .purchasePrefix("PO-2026-")
                .currency("INR")
                .currencySymbol("₹")
                .defaultTaxRate(new BigDecimal("5.00"))
                .taxScheme("REGULAR")
                .isGstEnabled(true)
                .enableRoundOff(true)
                .build();
        businessSettingsRepository.save(settings);

        // 6. Create Business Owner User
        User owner = User.builder()
                .tenantId(tenant.getId())
                .branchId(mainBranch.getId())
                .username(request.getUsername().trim())
                .passwordHash(passwordEncoder.encode(request.getPassword()))
                .fullName(request.getOwnerName().trim())
                .email(request.getEmail().trim().toLowerCase(Locale.ROOT))
                .phone(request.getEffectivePhone().trim())
                .role(UserRole.OWNER)
                .status("ACTIVE")
                .isActive(true)
                .build();
        owner = userRepository.save(owner);
        log.info("Created business owner user id={}, role=OWNER for tenant id={}", owner.getId(), tenant.getId());

        // 7. Generate JWT access token
        CustomUserDetails userDetails = CustomUserDetails.fromEntity(owner, tenant);
        String token = jwtService.generateToken(userDetails);

        return AuthResponse.builder()
                .accessToken(token)
                .tokenType("Bearer")
                .expiresIn(jwtService.getExpirationMs())
                .role(owner.getRole())
                .user(UserDto.fromEntity(owner))
                .tenant(TenantDto.fromEntity(tenant))
                .build();
    }

    @Transactional(readOnly = true)
    public AuthResponse login(LoginRequest request) {
        String identifier = request.getUsernameOrEmail().trim();
        log.info("Authenticating user login for identifier={}", identifier);

        User user = userRepository.findByUsernameOrEmailIgnoreCase(identifier)
                .orElseThrow(() -> new InvalidCredentialsException("Invalid username/email or password"));

        if (!passwordEncoder.matches(request.getPassword(), user.getPasswordHash())) {
            log.warn("Password mismatch for identifier={}", identifier);
            throw new InvalidCredentialsException("Invalid username/email or password");
        }

        if (!user.isAccountActive()) {
            log.warn("Login rejected: User id={} account is deactivated or suspended", user.getId());
            throw new DisabledException("User account is inactive or has been deactivated");
        }

        Tenant tenant = tenantRepository.findById(user.getTenantId())
                .orElseThrow(() -> new ResourceNotFoundException("Tenant not found for user: " + identifier));

        if (!Boolean.TRUE.equals(tenant.getIsActive()) || "INACTIVE".equalsIgnoreCase(tenant.getStatus())) {
            log.warn("Login rejected: Tenant id={} is deactivated", tenant.getId());
            throw new DisabledException("Tenant business account is currently inactive or suspended");
        }

        CustomUserDetails userDetails = CustomUserDetails.fromEntity(user, tenant);
        String token = jwtService.generateToken(userDetails);

        return AuthResponse.builder()
                .accessToken(token)
                .tokenType("Bearer")
                .expiresIn(jwtService.getExpirationMs())
                .role(user.getRole())
                .user(UserDto.fromEntity(user))
                .tenant(TenantDto.fromEntity(tenant))
                .build();
    }

    @Transactional(readOnly = true)
    public CurrentUserResponse getCurrentUser(CustomUserDetails principal) {
        if (principal == null) {
            throw new InvalidCredentialsException("User is not authenticated");
        }

        User user = userRepository.findById(principal.getId())
                .orElseThrow(() -> new ResourceNotFoundException("User not found for ID: " + principal.getId()));

        Tenant tenant = tenantRepository.findById(principal.getTenantId())
                .orElseThrow(() -> new ResourceNotFoundException("Tenant not found for ID: " + principal.getTenantId()));

        return CurrentUserResponse.builder()
                .userId(user.getId())
                .username(user.getUsername())
                .name(user.getFullName())
                .email(user.getEmail())
                .phone(user.getPhone())
                .role(user.getRole())
                .tenantId(tenant.getId())
                .businessName(tenant.getEffectiveBusinessName())
                .businessMode(tenant.getBusinessMode())
                .build();
    }
}
