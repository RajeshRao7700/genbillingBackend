package com.nexbill.security;

import com.nexbill.entity.Tenant;
import com.nexbill.entity.User;
import com.nexbill.entity.UserRole;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.List;

@Getter
@Builder
@AllArgsConstructor
public class CustomUserDetails implements UserDetails {

    private final Long id;
    private final Long tenantId;
    private final String businessName;
    private final String username;
    private final String password;
    private final String fullName;
    private final String email;
    private final String phone;
    private final UserRole role;
    private final String status;
    private final Boolean isActive;
    private final Collection<? extends GrantedAuthority> authorities;

    public static CustomUserDetails fromEntity(User user, Tenant tenant) {
        String businessName = tenant != null ? tenant.getEffectiveBusinessName() : "";
        List<GrantedAuthority> authorities = List.of(
                new SimpleGrantedAuthority("ROLE_" + user.getRole().name())
        );

        return CustomUserDetails.builder()
                .id(user.getId())
                .tenantId(user.getTenantId())
                .businessName(businessName)
                .username(user.getUsername())
                .password(user.getPasswordHash())
                .fullName(user.getFullName())
                .email(user.getEmail())
                .phone(user.getPhone())
                .role(user.getRole())
                .status(user.getStatus())
                .isActive(user.getIsActive())
                .authorities(authorities)
                .build();
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return authorities;
    }

    @Override
    public String getPassword() {
        return password;
    }

    @Override
    public String getUsername() {
        return username;
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return !"SUSPENDED".equalsIgnoreCase(status);
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return Boolean.TRUE.equals(isActive)
                && !"INACTIVE".equalsIgnoreCase(status)
                && !"DEACTIVATED".equalsIgnoreCase(status);
    }
}
