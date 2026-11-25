package com.nexbill.service;

import com.nexbill.dto.*;
import com.nexbill.entity.Branch;
import com.nexbill.entity.User;
import com.nexbill.entity.UserRole;
import com.nexbill.exception.BusinessRuleException;
import com.nexbill.exception.DuplicateResourceException;
import com.nexbill.exception.ResourceNotFoundException;
import com.nexbill.multitenancy.CurrentTenantService;
import com.nexbill.repository.BranchRepository;
import com.nexbill.repository.UserRepository;
import com.nexbill.security.CustomUserDetails;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("EmployeeService Tests")
class EmployeeServiceTest {

    @Mock private CurrentTenantService currentTenantService;
    @Mock private UserRepository userRepository;
    @Mock private BranchRepository branchRepository;
    @Mock private PasswordEncoder passwordEncoder;

    @InjectMocks private EmployeeService employeeService;

    private static final Long TENANT_A = 1L;
    private static final Long OWNER_ID  = 10L;
    private static final Long ADMIN_ID  = 20L;
    private static final Long STAFF_ID  = 30L;

    private User ownerUser;
    private User staffUser;
    private Branch activeBranch;

    @BeforeEach
    void setUp() {
        ownerUser = User.builder()
                .id(OWNER_ID).tenantId(TENANT_A).username("owner")
                .email("owner@chowmow.in").fullName("Ram Kumar")
                .passwordHash("hashed").role(UserRole.OWNER)
                .status("ACTIVE").isActive(true).build();

        staffUser = User.builder()
                .id(STAFF_ID).tenantId(TENANT_A).username("staff1")
                .email("staff1@chowmow.in").fullName("Staff One")
                .passwordHash("hashed").role(UserRole.STAFF)
                .status("ACTIVE").isActive(true).build();

        activeBranch = Branch.builder()
                .id(1L).tenantId(TENANT_A).name("Main").branchCode("MAIN")
                .isMainBranch(true).status("ACTIVE").isActive(true).build();

        setSecurityContext(OWNER_ID, TENANT_A, UserRole.OWNER);
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    // -------------------------------------------------------------------------
    // createEmployee
    // -------------------------------------------------------------------------

    @Test
    @DisplayName("createEmployee: creates employee successfully with hashed password")
    void createEmployee_success() {
        when(currentTenantService.getRequiredTenantId()).thenReturn(TENANT_A);
        when(userRepository.existsByUsernameIgnoreCase("cashier1")).thenReturn(false);
        when(userRepository.existsByEmailIgnoreCase("cashier1@chowmow.in")).thenReturn(false);
        when(branchRepository.findByIdAndTenantId(1L, TENANT_A)).thenReturn(Optional.of(activeBranch));
        when(passwordEncoder.encode("SecurePass1!")).thenReturn("hashed_pass");
        when(userRepository.save(any(User.class))).thenAnswer(inv -> {
            User u = inv.getArgument(0);
            u = User.builder().id(99L).tenantId(u.getTenantId()).username(u.getUsername())
                    .email(u.getEmail()).fullName(u.getFullName())
                    .passwordHash(u.getPasswordHash()).role(u.getRole())
                    .status("ACTIVE").isActive(true).build();
            return u;
        });

        CreateEmployeeRequest request = CreateEmployeeRequest.builder()
                .fullName("Cashier One").username("cashier1").email("cashier1@chowmow.in")
                .role(UserRole.CASHIER).branchId(1L).password("SecurePass1!").build();

        EmployeeResponse response = employeeService.createEmployee(request);

        assertThat(response.getId()).isEqualTo(99L);
        assertThat(response.getRole()).isEqualTo(UserRole.CASHIER);
        // Password must NOT be in response
        assertThat(response).doesNotHaveToString("SecurePass1!");
        verify(passwordEncoder).encode("SecurePass1!");
        verify(userRepository).save(any(User.class));
    }

    @Test
    @DisplayName("createEmployee: throws DuplicateResourceException on duplicate username")
    void createEmployee_duplicateUsername_throws() {
        when(currentTenantService.getRequiredTenantId()).thenReturn(TENANT_A);
        when(userRepository.existsByUsernameIgnoreCase("owner")).thenReturn(true);

        CreateEmployeeRequest request = CreateEmployeeRequest.builder()
                .fullName("Clone").username("owner").email("new@chowmow.in")
                .role(UserRole.STAFF).password("Pass1234!").build();

        assertThatThrownBy(() -> employeeService.createEmployee(request))
                .isInstanceOf(DuplicateResourceException.class)
                .hasMessageContaining("owner");
    }

    @Test
    @DisplayName("createEmployee: ADMIN cannot create OWNER")
    void createEmployee_adminCannotCreateOwner() {
        setSecurityContext(ADMIN_ID, TENANT_A, UserRole.ADMIN);
        when(currentTenantService.getRequiredTenantId()).thenReturn(TENANT_A);

        CreateEmployeeRequest request = CreateEmployeeRequest.builder()
                .fullName("New Owner").username("newowner").email("newowner@chowmow.in")
                .role(UserRole.OWNER).password("Pass1234!").build();

        assertThatThrownBy(() -> employeeService.createEmployee(request))
                .isInstanceOf(BusinessRuleException.class)
                .hasMessageContaining("OWNER");
    }

    @Test
    @DisplayName("createEmployee: cannot assign employee to another tenant's branch")
    void createEmployee_crossTenantBranch_throws() {
        when(currentTenantService.getRequiredTenantId()).thenReturn(TENANT_A);
        when(userRepository.existsByUsernameIgnoreCase("emp1")).thenReturn(false);
        when(userRepository.existsByEmailIgnoreCase("emp1@a.com")).thenReturn(false);
        // branchId 999 belongs to tenant B, so findByIdAndTenantId returns empty
        when(branchRepository.findByIdAndTenantId(999L, TENANT_A)).thenReturn(Optional.empty());

        CreateEmployeeRequest request = CreateEmployeeRequest.builder()
                .fullName("Emp").username("emp1").email("emp1@a.com")
                .role(UserRole.STAFF).branchId(999L).password("Pass1234!").build();

        assertThatThrownBy(() -> employeeService.createEmployee(request))
                .hasMessageContaining("does not belong to your business");
    }

    // -------------------------------------------------------------------------
    // updateEmployeeStatus
    // -------------------------------------------------------------------------

    @Test
    @DisplayName("updateEmployeeStatus: cannot deactivate last active OWNER")
    void updateStatus_lastOwner_throws() {
        when(currentTenantService.getRequiredTenantId()).thenReturn(TENANT_A);
        when(userRepository.findByIdAndTenantId(STAFF_ID, TENANT_A)).thenReturn(Optional.of(ownerUser));
        // ownerUser has OWNER role; count returns 1
        when(userRepository.countByTenantIdAndRoleAndIsActiveTrue(TENANT_A, UserRole.OWNER)).thenReturn(1L);

        EmployeeStatusRequest request = EmployeeStatusRequest.builder().status("INACTIVE").build();

        assertThatThrownBy(() -> employeeService.updateEmployeeStatus(STAFF_ID, request))
                .isInstanceOf(BusinessRuleException.class)
                .hasMessageContaining("last active OWNER");
    }

    @Test
    @DisplayName("updateEmployeeStatus: cannot deactivate own account")
    void updateStatus_selfDeactivation_throws() {
        when(currentTenantService.getRequiredTenantId()).thenReturn(TENANT_A);
        when(userRepository.findByIdAndTenantId(OWNER_ID, TENANT_A)).thenReturn(Optional.of(ownerUser));

        EmployeeStatusRequest request = EmployeeStatusRequest.builder().status("INACTIVE").build();

        assertThatThrownBy(() -> employeeService.updateEmployeeStatus(OWNER_ID, request))
                .isInstanceOf(BusinessRuleException.class)
                .hasMessageContaining("cannot deactivate your own");
    }

    @Test
    @DisplayName("updateEmployeeStatus: deactivated employee cannot authenticate (is_active=false)")
    void updateStatus_deactivated_isActiveFalse() {
        when(currentTenantService.getRequiredTenantId()).thenReturn(TENANT_A);
        when(userRepository.findByIdAndTenantId(STAFF_ID, TENANT_A)).thenReturn(Optional.of(staffUser));
        when(userRepository.save(any(User.class))).thenAnswer(inv -> inv.getArgument(0));

        EmployeeStatusRequest request = EmployeeStatusRequest.builder().status("INACTIVE").build();
        EmployeeResponse response = employeeService.updateEmployeeStatus(STAFF_ID, request);

        assertThat(response.getIsActive()).isFalse();
        assertThat(response.getStatus()).isEqualTo("INACTIVE");
    }

    // -------------------------------------------------------------------------
    // updateEmployeeRole
    // -------------------------------------------------------------------------

    @Test
    @DisplayName("updateEmployeeRole: cannot change own role")
    void updateRole_ownRole_throws() {
        when(currentTenantService.getRequiredTenantId()).thenReturn(TENANT_A);
        when(userRepository.findByIdAndTenantId(OWNER_ID, TENANT_A)).thenReturn(Optional.of(ownerUser));

        EmployeeRoleRequest request = EmployeeRoleRequest.builder().role(UserRole.ADMIN).build();

        assertThatThrownBy(() -> employeeService.updateEmployeeRole(OWNER_ID, request))
                .isInstanceOf(BusinessRuleException.class)
                .hasMessageContaining("cannot change your own role");
    }

    @Test
    @DisplayName("updateEmployeeRole: ADMIN cannot assign OWNER role")
    void updateRole_adminCannotGrantOwner() {
        setSecurityContext(ADMIN_ID, TENANT_A, UserRole.ADMIN);
        when(currentTenantService.getRequiredTenantId()).thenReturn(TENANT_A);
        when(userRepository.findByIdAndTenantId(STAFF_ID, TENANT_A)).thenReturn(Optional.of(staffUser));

        EmployeeRoleRequest request = EmployeeRoleRequest.builder().role(UserRole.OWNER).build();

        assertThatThrownBy(() -> employeeService.updateEmployeeRole(STAFF_ID, request))
                .isInstanceOf(BusinessRuleException.class)
                .hasMessageContaining("OWNER role");
    }

    @Test
    @DisplayName("updateEmployeeRole: cannot downgrade last active OWNER")
    void updateRole_lastOwnerDowngrade_throws() {
        when(currentTenantService.getRequiredTenantId()).thenReturn(TENANT_A);
        when(userRepository.findByIdAndTenantId(STAFF_ID, TENANT_A)).thenReturn(Optional.of(ownerUser));
        when(userRepository.countByTenantIdAndRoleAndIsActiveTrue(TENANT_A, UserRole.OWNER)).thenReturn(1L);

        EmployeeRoleRequest request = EmployeeRoleRequest.builder().role(UserRole.ADMIN).build();

        assertThatThrownBy(() -> employeeService.updateEmployeeRole(STAFF_ID, request))
                .isInstanceOf(BusinessRuleException.class)
                .hasMessageContaining("last active OWNER");
    }

    // -------------------------------------------------------------------------
    // resetEmployeePassword
    // -------------------------------------------------------------------------

    @Test
    @DisplayName("resetEmployeePassword: hashes password, never exposes it")
    void resetPassword_hashesAndSaves() {
        when(currentTenantService.getRequiredTenantId()).thenReturn(TENANT_A);
        when(userRepository.findByIdAndTenantId(STAFF_ID, TENANT_A)).thenReturn(Optional.of(staffUser));
        when(passwordEncoder.encode("NewSecure#1")).thenReturn("new_hash");
        when(userRepository.save(any(User.class))).thenAnswer(inv -> inv.getArgument(0));

        ResetPasswordRequest request = ResetPasswordRequest.builder().newPassword("NewSecure#1").build();
        employeeService.resetEmployeePassword(STAFF_ID, request);

        verify(passwordEncoder).encode("NewSecure#1");
        verify(userRepository).save(argThat(u -> "new_hash".equals(u.getPasswordHash())));
    }

    @Test
    @DisplayName("resetEmployeePassword: throws when employee not found in tenant")
    void resetPassword_notFound_throws() {
        when(currentTenantService.getRequiredTenantId()).thenReturn(TENANT_A);
        when(userRepository.findByIdAndTenantId(999L, TENANT_A)).thenReturn(Optional.empty());

        ResetPasswordRequest request = ResetPasswordRequest.builder().newPassword("NewSecure#1").build();

        assertThatThrownBy(() -> employeeService.resetEmployeePassword(999L, request))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    // -------------------------------------------------------------------------
    // Cross-tenant isolation
    // -------------------------------------------------------------------------

    @Test
    @DisplayName("getEmployee: tenant A cannot read tenant B employee")
    void getEmployee_crossTenant_throws() {
        when(currentTenantService.getRequiredTenantId()).thenReturn(TENANT_A);
        // ID 999 belongs to tenant B, so findByIdAndTenantId with TENANT_A returns empty
        when(userRepository.findByIdAndTenantId(999L, TENANT_A)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> employeeService.getEmployee(999L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("999");
    }

    // -------------------------------------------------------------------------
    // Helper
    // -------------------------------------------------------------------------

    private void setSecurityContext(Long userId, Long tenantId, UserRole role) {
        CustomUserDetails principal = CustomUserDetails.builder()
                .id(userId).tenantId(tenantId).username(role.name().toLowerCase())
                .password("x").role(role).status("ACTIVE").isActive(true)
                .authorities(List.of(new SimpleGrantedAuthority("ROLE_" + role.name())))
                .build();

        SecurityContext ctx = SecurityContextHolder.createEmptyContext();
        ctx.setAuthentication(new UsernamePasswordAuthenticationToken(
                principal, null, principal.getAuthorities()));
        SecurityContextHolder.setContext(ctx);
    }
}
