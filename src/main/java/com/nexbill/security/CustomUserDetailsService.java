package com.nexbill.security;

import com.nexbill.entity.Tenant;
import com.nexbill.entity.User;
import com.nexbill.repository.TenantRepository;
import com.nexbill.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class CustomUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;
    private final TenantRepository tenantRepository;

    @Override
    @Transactional(readOnly = true)
    public UserDetails loadUserByUsername(String identifier) throws UsernameNotFoundException {
        User user = userRepository.findByUsernameOrEmailIgnoreCase(identifier)
                .orElseThrow(() -> new UsernameNotFoundException("User not found with identifier: " + identifier));

        Tenant tenant = tenantRepository.findById(user.getTenantId())
                .orElseThrow(() -> new UsernameNotFoundException("Associated business tenant not found for user: " + identifier));

        if (!Boolean.TRUE.equals(tenant.getIsActive()) || "INACTIVE".equalsIgnoreCase(tenant.getStatus())) {
            log.warn("Login rejected: Tenant {} is inactive for user {}", tenant.getId(), user.getUsername());
            throw new DisabledException("Tenant business account is currently inactive or suspended");
        }

        return CustomUserDetails.fromEntity(user, tenant);
    }
}
