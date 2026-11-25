package com.nexbill.controller;

import com.nexbill.common.ApiResponse;
import com.nexbill.common.PagedResponse;
import com.nexbill.dto.*;
import com.nexbill.service.EmployeeService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/employees")
@RequiredArgsConstructor
@Tag(name = "Employee Management", description = "Create and manage business employees")
public class EmployeeController {

    private final EmployeeService employeeService;

    @PostMapping
    @PreAuthorize("hasAnyRole('OWNER','ADMIN')")
    @Operation(summary = "Create a new employee")
    public ResponseEntity<ApiResponse<EmployeeResponse>> createEmployee(
            @Valid @RequestBody CreateEmployeeRequest request) {
        EmployeeResponse employee = employeeService.createEmployee(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Employee created successfully", employee));
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('OWNER','ADMIN')")
    @Operation(summary = "List all employees for the authenticated tenant")
    public ResponseEntity<ApiResponse<PagedResponse<EmployeeResponse>>> getAllEmployees(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "asc") String sortDir) {

        Sort sort = sortDir.equalsIgnoreCase("desc")
                ? Sort.by(sortBy).descending()
                : Sort.by(sortBy).ascending();
        Page<EmployeeResponse> employees = employeeService.getAllEmployees(PageRequest.of(page, size, sort));
        return ResponseEntity.ok(ApiResponse.success("Employees retrieved successfully",
                PagedResponse.of(employees)));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('OWNER','ADMIN')")
    @Operation(summary = "Get employee by ID")
    public ResponseEntity<ApiResponse<EmployeeResponse>> getEmployee(@PathVariable Long id) {
        EmployeeResponse employee = employeeService.getEmployee(id);
        return ResponseEntity.ok(ApiResponse.success("Employee retrieved successfully", employee));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('OWNER','ADMIN')")
    @Operation(summary = "Update employee details (name, phone, branch)")
    public ResponseEntity<ApiResponse<EmployeeResponse>> updateEmployee(
            @PathVariable Long id,
            @Valid @RequestBody UpdateEmployeeRequest request) {
        EmployeeResponse employee = employeeService.updateEmployee(id, request);
        return ResponseEntity.ok(ApiResponse.success("Employee updated successfully", employee));
    }

    @PatchMapping("/{id}/status")
    @PreAuthorize("hasAnyRole('OWNER','ADMIN')")
    @Operation(summary = "Update employee status (ACTIVE / INACTIVE / DEACTIVATED)")
    public ResponseEntity<ApiResponse<EmployeeResponse>> updateEmployeeStatus(
            @PathVariable Long id,
            @Valid @RequestBody EmployeeStatusRequest request) {
        EmployeeResponse employee = employeeService.updateEmployeeStatus(id, request);
        return ResponseEntity.ok(ApiResponse.success("Employee status updated successfully", employee));
    }

    @PatchMapping("/{id}/role")
    @PreAuthorize("hasAnyRole('OWNER','ADMIN')")
    @Operation(summary = "Change employee role")
    public ResponseEntity<ApiResponse<EmployeeResponse>> updateEmployeeRole(
            @PathVariable Long id,
            @Valid @RequestBody EmployeeRoleRequest request) {
        EmployeeResponse employee = employeeService.updateEmployeeRole(id, request);
        return ResponseEntity.ok(ApiResponse.success("Employee role updated successfully", employee));
    }

    @PostMapping("/{id}/reset-password")
    @PreAuthorize("hasAnyRole('OWNER','ADMIN')")
    @Operation(summary = "Reset employee password – password never returned in response")
    public ResponseEntity<ApiResponse<Void>> resetEmployeePassword(
            @PathVariable Long id,
            @Valid @RequestBody ResetPasswordRequest request) {
        employeeService.resetEmployeePassword(id, request);
        return ResponseEntity.ok(ApiResponse.success("Employee password reset successfully", null));
    }
}
