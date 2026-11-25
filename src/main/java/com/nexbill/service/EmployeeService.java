package com.nexbill.service;

import com.nexbill.dto.*;
import com.nexbill.entity.Branch;
import com.nexbill.entity.User;
import com.nexbill.entity.UserRole;
import com.nexbill.exception.BusinessRuleException;
import com.nexbill.exception.DuplicateResourceException;
import com.nexbill.exception.ResourceNotFoundException;
import com.nexbill.exception.TenantAccessDeniedException;
import com.nexbill.multitenancy.CurrentTenantService;
import com.nexbill.repository.BranchRepository;
import com.nexbill.repository.UserRepository;
import com.nexbill.security.CustomUserDetails;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class EmployeeService {

    private final CurrentTenantService currentTenantService;
    private final UserRepository userRepository;
    private final BranchRepository branchRepository;
    private final PasswordEncoder passwordEncoder;

    // -------------------------------------------------------------------------
    // Create
    // -------------------------------------------------------------------------

    @Transactional
    public EmployeeResponse createEmployee(CreateEmployeeRequest request) {
        Long tenantId = currentTenantService.getRequiredTenantId();
        CustomUserDetails principal = currentPrincipal();

        // Prevent creation of OWNER by ADMIN
        if (request.getRole() == UserRole.OWNER && principal.getRole() != UserRole.OWNER) {
            throw new BusinessRuleException("Only the business OWNER can create another OWNER account.");
        }

        // Prevent privilege escalation above own role
        if (request.getRole().ordinal() < principal.getRole().ordinal()) {
            throw new BusinessRuleException(
                    "You cannot assign a role that is higher than your own role (" + principal.getRole() + ").");
        }

        // Global username uniqueness
        if (userRepository.existsByUsernameIgnoreCase(request.getUsername())) {
            throw new DuplicateResourceException("Username '" + request.getUsername() + "' is already taken");
        }

        // Global email uniqueness
        if (userRepository.existsByEmailIgnoreCase(request.getEmail())) {
            throw new DuplicateResourceException("Email '" + request.getEmail() + "' is already registered");
        }

        // Branch validation: must belong to same tenant
        Long branchId = null;
        if (request.getBranchId() != null) {
            Branch branch = branchRepository.findByIdAndTenantId(request.getBranchId(), tenantId)
                    .orElseThrow(() -> new TenantAccessDeniedException(
                            "Branch id " + request.getBranchId() + " does not belong to your business"));
            if (!branch.isStatusActive()) {
                throw new BusinessRuleException("Cannot assign employee to an inactive branch.");
            }
            branchId = branch.getId();
        }

        User employee = User.builder()
                .tenantId(tenantId)
                .branchId(branchId)
                .fullName(request.getFullName())
                .username(request.getUsername().toLowerCase())
                .email(request.getEmail().toLowerCase())
                .phone(request.getPhone())
                .passwordHash(passwordEncoder.encode(request.getPassword()))
                .role(request.getRole())
                .status("ACTIVE")
                .isActive(true)
                .build();

        User saved = userRepository.save(employee);
        log.info("Employee created: id={}, username={}, role={}, tenantId={}",
                saved.getId(), saved.getUsername(), saved.getRole(), tenantId);
        return toResponse(saved);
    }

    // -------------------------------------------------------------------------
    // Read
    // -------------------------------------------------------------------------

    @Transactional(readOnly = true)
    public Page<EmployeeResponse> getAllEmployees(Pageable pageable) {
        Long tenantId = currentTenantService.getRequiredTenantId();
        return userRepository.findAllByTenantId(tenantId, pageable).map(this::toResponse);
    }

    @Transactional(readOnly = true)
    public EmployeeResponse getEmployee(Long id) {
        Long tenantId = currentTenantService.getRequiredTenantId();
        User user = userRepository.findByIdAndTenantId(id, tenantId)
                .orElseThrow(() -> new ResourceNotFoundException("Employee not found with id: " + id));
        return toResponse(user);
    }

    // -------------------------------------------------------------------------
    // Update
    // -------------------------------------------------------------------------

    @Transactional
    public EmployeeResponse updateEmployee(Long id, UpdateEmployeeRequest request) {
        Long tenantId = currentTenantService.getRequiredTenantId();
        User user = userRepository.findByIdAndTenantId(id, tenantId)
                .orElseThrow(() -> new ResourceNotFoundException("Employee not found with id: " + id));

        if (request.getFullName() != null && !request.getFullName().isBlank()) {
            user.setFullName(request.getFullName());
        }
        if (request.getPhone() != null) {
            user.setPhone(request.getPhone());
        }
        if (request.getBranchId() != null) {
            Branch branch = branchRepository.findByIdAndTenantId(request.getBranchId(), tenantId)
                    .orElseThrow(() -> new TenantAccessDeniedException(
                            "Branch id " + request.getBranchId() + " does not belong to your business"));
            if (!branch.isStatusActive()) {
                throw new BusinessRuleException("Cannot assign employee to an inactive branch.");
            }
            user.setBranchId(branch.getId());
        }

        User saved = userRepository.save(user);
        log.info("Employee updated: id={}, tenantId={}", saved.getId(), tenantId);
        return toResponse(saved);
    }

    // -------------------------------------------------------------------------
    // Status PATCH
    // -------------------------------------------------------------------------

    @Transactional
    public EmployeeResponse updateEmployeeStatus(Long id, EmployeeStatusRequest request) {
        Long tenantId = currentTenantService.getRequiredTenantId();
        CustomUserDetails principal = currentPrincipal();

        User user = userRepository.findByIdAndTenantId(id, tenantId)
                .orElseThrow(() -> new ResourceNotFoundException("Employee not found with id: " + id));

        boolean isDeactivating = !"ACTIVE".equalsIgnoreCase(request.getStatus());

        // Guard: cannot deactivate yourself
        if (isDeactivating && principal.getId().equals(user.getId())) {
            throw new BusinessRuleException("You cannot deactivate your own account.");
        }

        // Guard: cannot remove last active OWNER
        if (isDeactivating && user.getRole() == UserRole.OWNER) {
            long activeOwners = userRepository.countByTenantIdAndRoleAndIsActiveTrue(tenantId, UserRole.OWNER);
            if (activeOwners <= 1) {
                throw new BusinessRuleException(
                        "Cannot deactivate the last active OWNER account. Assign another OWNER first.");
            }
        }

        user.setStatus(request.getStatus());
        user.setIsActive("ACTIVE".equalsIgnoreCase(request.getStatus()));

        User saved = userRepository.save(user);
        log.info("Employee status updated: id={}, status={}, tenantId={}", saved.getId(), saved.getStatus(), tenantId);
        return toResponse(saved);
    }

    // -------------------------------------------------------------------------
    // Role PATCH
    // -------------------------------------------------------------------------

    @Transactional
    public EmployeeResponse updateEmployeeRole(Long id, EmployeeRoleRequest request) {
        Long tenantId = currentTenantService.getRequiredTenantId();
        CustomUserDetails principal = currentPrincipal();

        User user = userRepository.findByIdAndTenantId(id, tenantId)
                .orElseThrow(() -> new ResourceNotFoundException("Employee not found with id: " + id));

        // Guard: cannot change own role
        if (principal.getId().equals(user.getId())) {
            throw new BusinessRuleException("You cannot change your own role.");
        }

        // Guard: ADMIN cannot assign OWNER role
        if (request.getRole() == UserRole.OWNER && principal.getRole() != UserRole.OWNER) {
            throw new BusinessRuleException("Only the business OWNER can assign the OWNER role.");
        }

        // Guard: cannot grant higher role than own
        if (request.getRole().ordinal() < principal.getRole().ordinal()) {
            throw new BusinessRuleException(
                    "You cannot assign a role higher than your own (" + principal.getRole() + ").");
        }

        // Guard: downgrading the last OWNER
        if (user.getRole() == UserRole.OWNER && request.getRole() != UserRole.OWNER) {
            long activeOwners = userRepository.countByTenantIdAndRoleAndIsActiveTrue(tenantId, UserRole.OWNER);
            if (activeOwners <= 1) {
                throw new BusinessRuleException(
                        "Cannot change the role of the last active OWNER. Assign another OWNER first.");
            }
        }

        user.setRole(request.getRole());
        User saved = userRepository.save(user);
        log.info("Employee role updated: id={}, role={}, tenantId={}", saved.getId(), saved.getRole(), tenantId);
        return toResponse(saved);
    }

    // -------------------------------------------------------------------------
    // Password Reset
    // -------------------------------------------------------------------------

    @Transactional
    public void resetEmployeePassword(Long id, ResetPasswordRequest request) {
        Long tenantId = currentTenantService.getRequiredTenantId();
        User user = userRepository.findByIdAndTenantId(id, tenantId)
                .orElseThrow(() -> new ResourceNotFoundException("Employee not found with id: " + id));

        user.setPasswordHash(passwordEncoder.encode(request.getNewPassword()));
        userRepository.save(user);
        log.info("Password reset for employee: id={}, tenantId={}", id, tenantId);
        // Password is never returned or logged
    }

    // -------------------------------------------------------------------------
    // Helper: get current principal from SecurityContext
    // -------------------------------------------------------------------------

    private CustomUserDetails currentPrincipal() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !(auth.getPrincipal() instanceof CustomUserDetails)) {
            throw new TenantAccessDeniedException("No authenticated user found in security context");
        }
        return (CustomUserDetails) auth.getPrincipal();
    }

    // -------------------------------------------------------------------------
    // Mapper
    // -------------------------------------------------------------------------

    private EmployeeResponse toResponse(User user) {
        // Optionally resolve branch name without an extra DB hit (lazy via repo if needed)
        return EmployeeResponse.builder()
                .id(user.getId())
                .tenantId(user.getTenantId())
                .branchId(user.getBranchId())
                .fullName(user.getFullName())
                .username(user.getUsername())
                .email(user.getEmail())
                .phone(user.getPhone())
                .role(user.getRole())
                .status(user.getStatus())
                .isActive(user.getIsActive())
                .createdAt(user.getCreatedAt())
                .updatedAt(user.getUpdatedAt())
                .build();
    }
}
