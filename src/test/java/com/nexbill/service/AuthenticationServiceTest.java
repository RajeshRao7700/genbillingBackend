package com.nexbill.service;

import com.nexbill.dto.AuthResponse;
import com.nexbill.dto.LoginRequest;
import com.nexbill.dto.RegisterRequest;
import com.nexbill.entity.*;
import com.nexbill.exception.DuplicateResourceException;
import com.nexbill.exception.InvalidCredentialsException;
import com.nexbill.repository.BranchRepository;
import com.nexbill.repository.BusinessSettingsRepository;
import com.nexbill.repository.TenantRepository;
import com.nexbill.repository.UserRepository;
import com.nexbill.security.CustomUserDetails;
import com.nexbill.security.JwtService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthenticationServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private TenantRepository tenantRepository;

    @Mock
    private BranchRepository branchRepository;

    @Mock
    private BusinessSettingsRepository businessSettingsRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtService jwtService;

    @InjectMocks
    private AuthenticationService authenticationService;

    private RegisterRequest registerRequest;

    @BeforeEach
    void setUp() {
        registerRequest = RegisterRequest.builder()
                .businessName("Apex Stores")
                .ownerName("John Doe")
                .email("john@apexstores.com")
                .username("johndoe")
                .phoneNumber("+91 99887 76655")
                .password("Secret@123")
                .businessMode(BusinessMode.BUSINESS)
                .build();
    }

    @Test
    @DisplayName("Successful registration creates tenant, main branch, settings, owner, and returns JWT")
    void testRegisterSuccess() {
        when(userRepository.existsByEmailIgnoreCase(anyString())).thenReturn(false);
        when(userRepository.existsByUsernameIgnoreCase(anyString())).thenReturn(false);
        when(tenantRepository.existsByCode(anyString())).thenReturn(false);

        Tenant savedTenant = Tenant.builder()
                .id(100L)
                .name("Apex Stores")
                .businessName("Apex Stores")
                .ownerName("John Doe")
                .code("apexstores")
                .email("john@apexstores.com")
                .phone("+91 99887 76655")
                .businessMode(BusinessMode.BUSINESS)
                .status("ACTIVE")
                .isActive(true)
                .build();
        when(tenantRepository.save(any(Tenant.class))).thenReturn(savedTenant);

        Branch savedBranch = Branch.builder()
                .id(200L)
                .tenantId(100L)
                .name("Apex Stores - Main Branch")
                .branchCode("MAIN")
                .isMainBranch(true)
                .isActive(true)
                .build();
        when(branchRepository.save(any(Branch.class))).thenReturn(savedBranch);

        when(passwordEncoder.encode("Secret@123")).thenReturn("$2a$10$hashedPassword123");

        User savedUser = User.builder()
                .id(300L)
                .tenantId(100L)
                .branchId(200L)
                .username("johndoe")
                .fullName("John Doe")
                .email("john@apexstores.com")
                .phone("+91 99887 76655")
                .passwordHash("$2a$10$hashedPassword123")
                .role(UserRole.OWNER)
                .status("ACTIVE")
                .isActive(true)
                .build();
        when(userRepository.save(any(User.class))).thenReturn(savedUser);
        when(jwtService.generateToken(any(CustomUserDetails.class))).thenReturn("mock.jwt.token");
        when(jwtService.getExpirationMs()).thenReturn(86400000L);

        AuthResponse response = authenticationService.register(registerRequest);

        assertNotNull(response);
        assertEquals("mock.jwt.token", response.getAccessToken());
        assertEquals("Bearer", response.getTokenType());
        assertEquals(UserRole.OWNER, response.getRole());
        assertEquals(300L, response.getUser().getId());
        assertEquals(100L, response.getTenant().getId());

        verify(userRepository).save(argThat(u -> u.getPasswordHash().startsWith("$2a$10$")));
    }

    @Test
    @DisplayName("Registration rejects duplicate email")
    void testRegisterDuplicateEmail() {
        when(userRepository.existsByEmailIgnoreCase("john@apexstores.com")).thenReturn(true);

        assertThrows(DuplicateResourceException.class, () -> authenticationService.register(registerRequest));
        verify(tenantRepository, never()).save(any());
        verify(userRepository, never()).save(any());
    }

    @Test
    @DisplayName("Registration rejects duplicate username")
    void testRegisterDuplicateUsername() {
        when(userRepository.existsByEmailIgnoreCase(anyString())).thenReturn(false);
        when(userRepository.existsByUsernameIgnoreCase("johndoe")).thenReturn(true);

        assertThrows(DuplicateResourceException.class, () -> authenticationService.register(registerRequest));
        verify(tenantRepository, never()).save(any());
        verify(userRepository, never()).save(any());
    }

    @Test
    @DisplayName("Login with invalid password throws InvalidCredentialsException")
    void testLoginInvalidPassword() {
        User user = User.builder()
                .id(1L)
                .tenantId(1L)
                .username("chowmow")
                .passwordHash("$2a$10$hash")
                .isActive(true)
                .status("ACTIVE")
                .build();

        when(userRepository.findByUsernameOrEmailIgnoreCase("chowmow")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("WrongPass", "$2a$10$hash")).thenReturn(false);

        LoginRequest loginRequest = LoginRequest.builder()
                .usernameOrEmail("chowmow")
                .password("WrongPass")
                .build();

        assertThrows(InvalidCredentialsException.class, () -> authenticationService.login(loginRequest));
    }
}
