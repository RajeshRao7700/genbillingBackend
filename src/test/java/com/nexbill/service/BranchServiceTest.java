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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("BranchService Tests")
class BranchServiceTest {

    @Mock private CurrentTenantService currentTenantService;
    @Mock private BranchRepository branchRepository;

    @InjectMocks private BranchService branchService;

    private static final Long TENANT_A = 1L;
    private static final Long TENANT_B = 2L;

    private Branch mainBranchA;

    @BeforeEach
    void setUp() {
        mainBranchA = Branch.builder()
                .id(1L)
                .tenantId(TENANT_A)
                .name("HYD Main")
                .branchCode("MAIN")
                .isMainBranch(true)
                .status("ACTIVE")
                .isActive(true)
                .build();
    }

    // -------------------------------------------------------------------------
    // createBranch
    // -------------------------------------------------------------------------

    @Test
    @DisplayName("createBranch: creates branch successfully for tenant")
    void createBranch_success() {
        when(currentTenantService.getRequiredTenantId()).thenReturn(TENANT_A);
        when(branchRepository.existsByTenantIdAndBranchCodeIgnoreCase(TENANT_A, "HYD2")).thenReturn(false);
        when(branchRepository.countByTenantIdAndIsMainBranchTrueAndIsActiveTrue(TENANT_A)).thenReturn(0L);
        when(branchRepository.save(any(Branch.class))).thenAnswer(inv -> {
            Branch b = inv.getArgument(0);
            b = Branch.builder().id(10L).tenantId(b.getTenantId()).name(b.getName())
                    .branchCode(b.getBranchCode()).isMainBranch(b.getIsMainBranch())
                    .status("ACTIVE").isActive(true).build();
            return b;
        });

        BranchRequest request = BranchRequest.builder()
                .name("Branch 2")
                .branchCode("HYD2")
                .city("Hyderabad")
                .build();

        BranchResponse response = branchService.createBranch(request);

        assertThat(response.getId()).isEqualTo(10L);
        assertThat(response.getBranchCode()).isEqualTo("HYD2");
        verify(branchRepository).save(any(Branch.class));
    }

    @Test
    @DisplayName("createBranch: throws DuplicateResourceException on duplicate branch code within tenant")
    void createBranch_duplicateCode_throws() {
        when(currentTenantService.getRequiredTenantId()).thenReturn(TENANT_A);
        when(branchRepository.existsByTenantIdAndBranchCodeIgnoreCase(TENANT_A, "MAIN")).thenReturn(true);

        BranchRequest request = BranchRequest.builder().name("HQ").branchCode("MAIN").build();

        assertThatThrownBy(() -> branchService.createBranch(request))
                .isInstanceOf(DuplicateResourceException.class)
                .hasMessageContaining("MAIN");
    }

    @Test
    @DisplayName("createBranch: throws BusinessRuleException if second main branch requested")
    void createBranch_secondMainBranch_throws() {
        when(currentTenantService.getRequiredTenantId()).thenReturn(TENANT_A);
        when(branchRepository.existsByTenantIdAndBranchCodeIgnoreCase(TENANT_A, "HQ2")).thenReturn(false);
        when(branchRepository.countByTenantIdAndIsMainBranchTrueAndIsActiveTrue(TENANT_A)).thenReturn(1L);

        BranchRequest request = BranchRequest.builder().name("HQ2").branchCode("HQ2").isMainBranch(true).build();

        assertThatThrownBy(() -> branchService.createBranch(request))
                .isInstanceOf(BusinessRuleException.class)
                .hasMessageContaining("main branch");
    }

    // -------------------------------------------------------------------------
    // getBranchById – tenant isolation
    // -------------------------------------------------------------------------

    @Test
    @DisplayName("getBranchById: tenant A cannot access tenant B's branch")
    void getBranchById_crossTenant_throws() {
        when(currentTenantService.getRequiredTenantId()).thenReturn(TENANT_A);
        // findByIdAndTenantId with TENANT_A returns empty (branch belongs to TENANT_B)
        when(branchRepository.findByIdAndTenantId(99L, TENANT_A)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> branchService.getBranchById(99L))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    @DisplayName("getBranchById: returns correct branch for tenant")
    void getBranchById_success() {
        when(currentTenantService.getRequiredTenantId()).thenReturn(TENANT_A);
        when(branchRepository.findByIdAndTenantId(1L, TENANT_A)).thenReturn(Optional.of(mainBranchA));

        BranchResponse response = branchService.getBranchById(1L);

        assertThat(response.getId()).isEqualTo(1L);
        assertThat(response.getTenantId()).isEqualTo(TENANT_A);
    }

    // -------------------------------------------------------------------------
    // getAllBranches – paginated
    // -------------------------------------------------------------------------

    @Test
    @DisplayName("getAllBranches: returns paginated branches for tenant")
    void getAllBranches_paginated() {
        when(currentTenantService.getRequiredTenantId()).thenReturn(TENANT_A);
        Page<Branch> page = new PageImpl<>(List.of(mainBranchA));
        when(branchRepository.findAllByTenantId(eq(TENANT_A), any())).thenReturn(page);

        Page<BranchResponse> result = branchService.getAllBranches(PageRequest.of(0, 20));

        assertThat(result.getTotalElements()).isEqualTo(1);
        assertThat(result.getContent().get(0).getTenantId()).isEqualTo(TENANT_A);
    }

    // -------------------------------------------------------------------------
    // updateBranchStatus
    // -------------------------------------------------------------------------

    @Test
    @DisplayName("updateBranchStatus: cannot deactivate last active main branch")
    void updateBranchStatus_deactivateLastActive_throws() {
        when(currentTenantService.getRequiredTenantId()).thenReturn(TENANT_A);
        when(branchRepository.findByIdAndTenantId(1L, TENANT_A)).thenReturn(Optional.of(mainBranchA));
        when(branchRepository.countByTenantIdAndIsActiveTrue(TENANT_A)).thenReturn(1L);

        BranchStatusRequest request = BranchStatusRequest.builder().status("INACTIVE").build();

        assertThatThrownBy(() -> branchService.updateBranchStatus(1L, request))
                .isInstanceOf(BusinessRuleException.class)
                .hasMessageContaining("main branch");
    }

    @Test
    @DisplayName("updateBranchStatus: can deactivate main branch when other branches exist")
    void updateBranchStatus_deactivateMainWithOthers_success() {
        when(currentTenantService.getRequiredTenantId()).thenReturn(TENANT_A);
        when(branchRepository.findByIdAndTenantId(1L, TENANT_A)).thenReturn(Optional.of(mainBranchA));
        when(branchRepository.countByTenantIdAndIsActiveTrue(TENANT_A)).thenReturn(3L);
        when(branchRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        BranchStatusRequest request = BranchStatusRequest.builder().status("INACTIVE").build();
        BranchResponse response = branchService.updateBranchStatus(1L, request);

        assertThat(response.getStatus()).isEqualTo("INACTIVE");
    }

    // -------------------------------------------------------------------------
    // deleteBranch
    // -------------------------------------------------------------------------

    @Test
    @DisplayName("deleteBranch: cannot delete main branch")
    void deleteBranch_mainBranch_throws() {
        when(currentTenantService.getRequiredTenantId()).thenReturn(TENANT_A);
        when(branchRepository.findByIdAndTenantId(1L, TENANT_A)).thenReturn(Optional.of(mainBranchA));

        assertThatThrownBy(() -> branchService.deleteBranch(1L))
                .isInstanceOf(BusinessRuleException.class)
                .hasMessageContaining("main branch");
    }

    @Test
    @DisplayName("deleteBranch: soft-deletes non-main branch")
    void deleteBranch_nonMain_success() {
        Branch nonMain = Branch.builder().id(5L).tenantId(TENANT_A)
                .name("Branch2").branchCode("B2").isMainBranch(false)
                .status("ACTIVE").isActive(true).build();

        when(currentTenantService.getRequiredTenantId()).thenReturn(TENANT_A);
        when(branchRepository.findByIdAndTenantId(5L, TENANT_A)).thenReturn(Optional.of(nonMain));
        when(branchRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        branchService.deleteBranch(5L);

        verify(branchRepository).save(argThat(b -> "INACTIVE".equals(b.getStatus()) && !b.getIsActive()));
    }
}
