package com.nexbill.service;

import com.nexbill.dto.UpdateUserRequest;
import com.nexbill.dto.UserDto;
import com.nexbill.entity.User;
import com.nexbill.entity.UserRole;
import com.nexbill.exception.DuplicateResourceException;
import com.nexbill.exception.ResourceNotFoundException;
import com.nexbill.exception.TenantAccessDeniedException;
import com.nexbill.multitenancy.CurrentTenantService;
import com.nexbill.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Locale;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserService {

    private final UserRepository userRepository;
    private final CurrentTenantService currentTenantService;

    /**
     * Retrieves all users belonging strictly to the currently authenticated tenant.
     */
    @Transactional(readOnly = true)
    public List<UserDto> getUsersByCurrentTenant() {
        Long tenantId = currentTenantService.getRequiredTenantId();
        return userRepository.findAllByTenantId(tenantId).stream()
                .map(UserDto::fromEntity)
                .toList();
    }

    /**
     * Retrieves a single user's details, verifying tenant ownership.
     * Prevents cross-tenant information access.
     */
    @Transactional(readOnly = true)
    public UserDto getUserById(Long userId) {
        Long currentTenantId = currentTenantService.getRequiredTenantId();

        User user = userRepository.findByIdAndTenantId(userId, currentTenantId)
                .orElseGet(() -> {
                    // Check if the user exists under a different tenant to trigger strict security denial
                    if (userRepository.existsById(userId)) {
                        log.warn("Cross-tenant access violation: Tenant {} attempted to access User {} belonging to another tenant",
                                currentTenantId, userId);
                        throw new TenantAccessDeniedException("Cross-tenant access forbidden. You cannot access users outside your tenant.");
                    }
                    throw new ResourceNotFoundException("User not found with id: " + userId);
                });

        return UserDto.fromEntity(user);
    }

    /**
     * Updates permitted user profile fields within the current tenant.
     * Roles cannot be modified through ordinary profile-update operations.
     */
    @Transactional
    public UserDto updateUserProfile(Long userId, UpdateUserRequest request) {
        Long currentTenantId = currentTenantService.getRequiredTenantId();

        User user = userRepository.findByIdAndTenantId(userId, currentTenantId)
                .orElseGet(() -> {
                    if (userRepository.existsById(userId)) {
                        throw new TenantAccessDeniedException("Cross-tenant update forbidden.");
                    }
                    throw new ResourceNotFoundException("User not found with id: " + userId);
                });

        if (request.getFullName() != null && !request.getFullName().isBlank()) {
            user.setFullName(request.getFullName().trim());
        }

        if (request.getPhone() != null && !request.getPhone().isBlank()) {
            user.setPhone(request.getPhone().trim());
        }

        if (request.getEmail() != null && !request.getEmail().isBlank()) {
            String newEmail = request.getEmail().trim().toLowerCase(Locale.ROOT);
            if (!newEmail.equalsIgnoreCase(user.getEmail())) {
                if (userRepository.existsByEmailIgnoreCase(newEmail)) {
                    throw new DuplicateResourceException("Email already in use: " + newEmail);
                }
                user.setEmail(newEmail);
            }
        }

        user = userRepository.save(user);
        log.info("Updated profile for user id={} within tenant id={}", user.getId(), currentTenantId);
        return UserDto.fromEntity(user);
    }

    /**
     * Deactivates a user belonging to the current tenant.
     */
    @Transactional
    public void deactivateUser(Long userId) {
        Long currentTenantId = currentTenantService.getRequiredTenantId();

        User user = userRepository.findByIdAndTenantId(userId, currentTenantId)
                .orElseGet(() -> {
                    if (userRepository.existsById(userId)) {
                        throw new TenantAccessDeniedException("Cross-tenant deactivation forbidden.");
                    }
                    throw new ResourceNotFoundException("User not found with id: " + userId);
                });

        // Ensure owner cannot deactivate themselves if they are the sole owner
        if (user.getRole() == UserRole.OWNER) {
            long activeOwners = userRepository.findAllByTenantId(currentTenantId).stream()
                    .filter(u -> u.getRole() == UserRole.OWNER && u.isAccountActive())
                    .count();
            if (activeOwners <= 1) {
                throw new IllegalStateException("Cannot deactivate the sole active owner of the business");
            }
        }

        user.setIsActive(false);
        user.setStatus("INACTIVE");
        userRepository.save(user);
        log.info("Deactivated user id={} within tenant id={}", userId, currentTenantId);
    }
}
