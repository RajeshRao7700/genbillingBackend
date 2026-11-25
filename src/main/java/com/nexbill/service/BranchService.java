package com.nexbill.service;

import com.nexbill.dto.BranchRequest;
import com.nexbill.dto.BranchResponse;
import com.nexbill.dto.BranchStatusRequest;
import com.nexbill.entity.Branch;
import com.nexbill.exception.BusinessRuleException;
import com.nexbill.exception.DuplicateResourceException;
import com.nexbill.exception.ResourceNotFoundException;
import com.nexbill.multitenancy.CurrentTenantService;
import com.nexbill.repository.BranchRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class BranchService {

    private final CurrentTenantService currentTenantService;
    private final BranchRepository branchRepository;

    // -------------------------------------------------------------------------
    // Create
    // -------------------------------------------------------------------------

    @Transactional
    public BranchResponse createBranch(BranchRequest request) {
        Long tenantId = currentTenantService.getRequiredTenantId();

        // Enforce unique branch code within tenant
        if (branchRepository.existsByTenantIdAndBranchCodeIgnoreCase(tenantId, request.getBranchCode())) {
            throw new DuplicateResourceException(
                    "Branch code '" + request.getBranchCode() + "' already exists in your business");
        }

        boolean wantsMain = Boolean.TRUE.equals(request.getIsMainBranch());

        // Guard: only one main branch per tenant
        if (wantsMain && branchRepository.countByTenantIdAndIsMainBranchTrueAndIsActiveTrue(tenantId) > 0) {
            throw new BusinessRuleException(
                    "Your business already has an active main branch. Deactivate it before assigning a new main branch.");
        }

        Branch branch = Branch.builder()
                .tenantId(tenantId)
                .name(request.getName())
                .branchCode(request.getBranchCode().toUpperCase())
                .phone(request.getPhone())
                .email(request.getEmail())
                .address(request.getAddress())
                .city(request.getCity())
                .state(request.getState())
                .pincode(request.getPincode())
                .gstin(request.getGstin())
                .isMainBranch(wantsMain)
                .status("ACTIVE")
                .isActive(true)
                .build();

        Branch saved = branchRepository.save(branch);
        log.info("Branch created: id={}, code={}, tenantId={}", saved.getId(), saved.getBranchCode(), tenantId);
        return toResponse(saved);
    }

    // -------------------------------------------------------------------------
    // Read
    // -------------------------------------------------------------------------

    @Transactional(readOnly = true)
    public Page<BranchResponse> getAllBranches(Pageable pageable) {
        Long tenantId = currentTenantService.getRequiredTenantId();
        return branchRepository.findAllByTenantId(tenantId, pageable).map(this::toResponse);
    }

    @Transactional(readOnly = true)
    public BranchResponse getBranchById(Long id) {
        Long tenantId = currentTenantService.getRequiredTenantId();
        Branch branch = branchRepository.findByIdAndTenantId(id, tenantId)
                .orElseThrow(() -> new ResourceNotFoundException("Branch not found with id: " + id));
        return toResponse(branch);
    }

    // -------------------------------------------------------------------------
    // Update
    // -------------------------------------------------------------------------

    @Transactional
    public BranchResponse updateBranch(Long id, BranchRequest request) {
        Long tenantId = currentTenantService.getRequiredTenantId();
        Branch branch = branchRepository.findByIdAndTenantId(id, tenantId)
                .orElseThrow(() -> new ResourceNotFoundException("Branch not found with id: " + id));

        // Guard: branch code uniqueness (exclude self)
        if (request.getBranchCode() != null
                && branchRepository.existsByTenantIdAndBranchCodeIgnoreCaseAndIdNot(
                        tenantId, request.getBranchCode(), id)) {
            throw new DuplicateResourceException(
                    "Branch code '" + request.getBranchCode() + "' is already used by another branch");
        }

        boolean wantsMain = Boolean.TRUE.equals(request.getIsMainBranch());
        // Guard: only one main branch per tenant (if this branch is not already main)
        if (wantsMain && !Boolean.TRUE.equals(branch.getIsMainBranch())
                && branchRepository.countByTenantIdAndIsMainBranchTrueAndIsActiveTrue(tenantId) > 0) {
            throw new BusinessRuleException(
                    "Your business already has an active main branch. Change the existing main branch first.");
        }

        if (request.getName() != null && !request.getName().isBlank()) {
            branch.setName(request.getName());
        }
        if (request.getBranchCode() != null && !request.getBranchCode().isBlank()) {
            branch.setBranchCode(request.getBranchCode().toUpperCase());
        }
        if (request.getPhone() != null) branch.setPhone(request.getPhone());
        if (request.getEmail() != null) branch.setEmail(request.getEmail());
        if (request.getAddress() != null) branch.setAddress(request.getAddress());
        if (request.getCity() != null) branch.setCity(request.getCity());
        if (request.getState() != null) branch.setState(request.getState());
        if (request.getPincode() != null) branch.setPincode(request.getPincode());
        if (request.getGstin() != null) branch.setGstin(request.getGstin());
        if (request.getIsMainBranch() != null) branch.setIsMainBranch(request.getIsMainBranch());

        Branch saved = branchRepository.save(branch);
        log.info("Branch updated: id={}, tenantId={}", saved.getId(), tenantId);
        return toResponse(saved);
    }

    // -------------------------------------------------------------------------
    // Status PATCH
    // -------------------------------------------------------------------------

    @Transactional
    public BranchResponse updateBranchStatus(Long id, BranchStatusRequest request) {
        Long tenantId = currentTenantService.getRequiredTenantId();
        Branch branch = branchRepository.findByIdAndTenantId(id, tenantId)
                .orElseThrow(() -> new ResourceNotFoundException("Branch not found with id: " + id));

        // Guard: cannot deactivate main branch if it's the only active branch
        if ("INACTIVE".equalsIgnoreCase(request.getStatus()) && Boolean.TRUE.equals(branch.getIsMainBranch())) {
            long activeBranches = branchRepository.countByTenantIdAndIsActiveTrue(tenantId);
            if (activeBranches <= 1) {
                throw new BusinessRuleException(
                        "Cannot deactivate the main branch when it is the only active branch for your business.");
            }
        }

        branch.setStatus(request.getStatus());
        branch.setIsActive("ACTIVE".equalsIgnoreCase(request.getStatus()));

        Branch saved = branchRepository.save(branch);
        log.info("Branch status updated: id={}, status={}, tenantId={}", saved.getId(), saved.getStatus(), tenantId);
        return toResponse(saved);
    }

    // -------------------------------------------------------------------------
    // Delete (soft-delete via deactivation; hard-delete only when safe)
    // -------------------------------------------------------------------------

    @Transactional
    public void deleteBranch(Long id) {
        Long tenantId = currentTenantService.getRequiredTenantId();
        Branch branch = branchRepository.findByIdAndTenantId(id, tenantId)
                .orElseThrow(() -> new ResourceNotFoundException("Branch not found with id: " + id));

        if (Boolean.TRUE.equals(branch.getIsMainBranch())) {
            throw new BusinessRuleException("Cannot delete the main branch of a business.");
        }

        // Soft-delete via deactivation so future invoice/purchase FK references remain intact
        branch.setStatus("INACTIVE");
        branch.setIsActive(false);
        branchRepository.save(branch);
        log.info("Branch soft-deleted (deactivated): id={}, tenantId={}", id, tenantId);
    }

    // -------------------------------------------------------------------------
    // Mapper
    // -------------------------------------------------------------------------

    public BranchResponse toResponse(Branch b) {
        return BranchResponse.builder()
                .id(b.getId())
                .tenantId(b.getTenantId())
                .name(b.getName())
                .branchCode(b.getBranchCode())
                .phone(b.getPhone())
                .email(b.getEmail())
                .address(b.getAddress())
                .city(b.getCity())
                .state(b.getState())
                .pincode(b.getPincode())
                .gstin(b.getGstin())
                .isMainBranch(b.getIsMainBranch())
                .status(b.getStatus())
                .isActive(b.getIsActive())
                .createdAt(b.getCreatedAt())
                .updatedAt(b.getUpdatedAt())
                .build();
    }
}
