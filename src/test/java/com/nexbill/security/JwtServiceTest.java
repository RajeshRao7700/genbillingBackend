package com.nexbill.security;

import com.nexbill.entity.UserRole;
import io.jsonwebtoken.Claims;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class JwtServiceTest {

    private JwtService jwtService;
    private final String secret = "dGhpcy1pcy1hLXZlcnktc2VjdXJlLTI1Ni1iaXQtc2VjcmV0LWtleS1mb3ItbmV4YmlsbC1tdWx0aS10ZW5hbnQtYXBwbGljYXRpb24=";
    private final long expirationMs = 3600000; // 1 hour

    @BeforeEach
    void setUp() {
        jwtService = new JwtService(secret, expirationMs, "nexbill-test");
    }

    private CustomUserDetails createTestUserDetails(Long userId, Long tenantId, String username, UserRole role) {
        return CustomUserDetails.builder()
                .id(userId)
                .tenantId(tenantId)
                .businessName("Test Shop")
                .username(username)
                .password("encoded_pass")
                .fullName("Test User")
                .email(username + "@test.com")
                .role(role)
                .status("ACTIVE")
                .isActive(true)
                .authorities(List.of(new SimpleGrantedAuthority("ROLE_" + role.name())))
                .build();
    }

    @Test
    @DisplayName("Generate signed JWT access token and extract claims successfully")
    void testGenerateAndExtractClaims() {
        CustomUserDetails userDetails = createTestUserDetails(10L, 1L, "shop_a_owner", UserRole.OWNER);

        String token = jwtService.generateToken(userDetails);
        assertNotNull(token);
        assertFalse(token.isBlank());

        assertEquals("shop_a_owner", jwtService.extractUsername(token));
        assertEquals(10L, jwtService.extractUserId(token));
        assertEquals(1L, jwtService.extractTenantId(token));
        assertEquals("OWNER", jwtService.extractRole(token));
        assertEquals("Test Shop", jwtService.extractBusinessName(token));
        assertFalse(jwtService.isTokenExpired(token));
        assertTrue(jwtService.validateToken(token, userDetails));
    }

    @Test
    @DisplayName("Validate JWT token with mismatched username fails")
    void testValidateTokenWithDifferentUserFails() {
        CustomUserDetails user1 = createTestUserDetails(10L, 1L, "user1", UserRole.OWNER);
        CustomUserDetails user2 = createTestUserDetails(20L, 2L, "user2", UserRole.OWNER);

        String token = jwtService.generateToken(user1);
        assertFalse(jwtService.validateToken(token, user2));
    }

    @Test
    @DisplayName("Validate expired JWT token detection")
    void testExpiredTokenValidation() {
        // Create service with -1000ms expiration (already expired)
        JwtService expiredService = new JwtService(secret, -1000, "nexbill-test");
        CustomUserDetails userDetails = createTestUserDetails(10L, 1L, "expired_user", UserRole.OWNER);

        String expiredToken = expiredService.generateToken(userDetails);
        assertTrue(expiredService.isTokenExpired(expiredToken));
        assertFalse(expiredService.validateToken(expiredToken, userDetails));
    }

    @Test
    @DisplayName("Tampered JWT token signature is rejected")
    void testTamperedTokenFails() {
        CustomUserDetails userDetails = createTestUserDetails(10L, 1L, "user1", UserRole.OWNER);
        String token = jwtService.generateToken(userDetails);

        // Tamper signature by modifying last characters
        String tamperedToken = token.substring(0, token.length() - 5) + "ABCDE";
        assertFalse(jwtService.validateToken(tamperedToken, userDetails));
    }
}
