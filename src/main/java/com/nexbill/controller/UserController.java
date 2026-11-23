package com.nexbill.controller;

import com.nexbill.common.ApiResponse;
import com.nexbill.dto.UpdateUserRequest;
import com.nexbill.dto.UserDto;
import com.nexbill.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
@Slf4j
@SecurityRequirement(name = "BearerAuth")
@Tag(name = "User Management Foundation", description = "Tenant-isolated employee and profile management APIs")
public class UserController {

    private final UserService userService;

    @GetMapping
    @PreAuthorize("hasAnyRole('OWNER', 'ADMIN', 'MANAGER')")
    @Operation(
            summary = "List Users for Current Tenant",
            description = "Retrieves all user accounts belonging strictly to the currently authenticated business tenant."
    )
    public ResponseEntity<ApiResponse<List<UserDto>>> listUsers() {
        List<UserDto> users = userService.getUsersByCurrentTenant();
        return ResponseEntity.ok(ApiResponse.success("Tenant users retrieved successfully", users));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('OWNER', 'ADMIN', 'MANAGER')")
    @Operation(
            summary = "Get User Details Within Tenant",
            description = "Retrieves user details. Strictly verifies that the requested user belongs to the authenticated tenant."
    )
    public ResponseEntity<ApiResponse<UserDto>> getUserById(@PathVariable("id") Long id) {
        UserDto user = userService.getUserById(id);
        return ResponseEntity.ok(ApiResponse.success("User retrieved successfully", user));
    }

    @PutMapping("/{id}")
    @Operation(
            summary = "Update Permitted User Profile",
            description = "Updates permitted personal profile attributes. Role changes are prevented through this endpoint."
    )
    public ResponseEntity<ApiResponse<UserDto>> updateUser(
            @PathVariable("id") Long id,
            @Valid @RequestBody UpdateUserRequest request) {
        UserDto updated = userService.updateUserProfile(id, request);
        return ResponseEntity.ok(ApiResponse.success("User profile updated successfully", updated));
    }

    @PatchMapping("/{id}/deactivate")
    @PreAuthorize("hasAnyRole('OWNER', 'ADMIN')")
    @Operation(
            summary = "Deactivate User Within Tenant",
            description = "Deactivates an employee account under the current business tenant. Prevents further logins."
    )
    public ResponseEntity<ApiResponse<Void>> deactivateUser(@PathVariable("id") Long id) {
        userService.deactivateUser(id);
        return ResponseEntity.ok(ApiResponse.success("User deactivated successfully", null));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('OWNER')")
    @Operation(
            summary = "Delete / Deactivate User (Owner Only)",
            description = "Owner-only endpoint to disable user account."
    )
    public ResponseEntity<ApiResponse<Void>> deleteUser(@PathVariable("id") Long id) {
        userService.deactivateUser(id);
        return ResponseEntity.ok(ApiResponse.success("User deactivated successfully", null));
    }
}
