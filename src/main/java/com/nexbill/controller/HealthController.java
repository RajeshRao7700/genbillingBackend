package com.nexbill.controller;

import com.nexbill.common.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.flywaydb.core.Flyway;
import org.flywaydb.core.api.MigrationInfo;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/health")
@RequiredArgsConstructor
@Tag(name = "Health & System", description = "System diagnostic, database connectivity and migration verification")
public class HealthController {

    private final JdbcTemplate jdbcTemplate;
    private final Flyway flyway;

    @GetMapping
    @Operation(summary = "System Health & Database Status", description = "Verifies MySQL connectivity, Flyway migration state, and active tenant schema")
    public ResponseEntity<ApiResponse<Map<String, Object>>> checkHealth() {
        Map<String, Object> details = new HashMap<>();
        details.put("service", "NEXBILL Multi-Tenant SaaS Backend");
        details.put("status", "UP");
        details.put("javaVersion", System.getProperty("java.version"));

        // 1. Database Connectivity Check
        try {
            String dbVersion = jdbcTemplate.queryForObject("SELECT VERSION()", String.class);
            details.put("database", "MySQL " + dbVersion);
            details.put("databaseStatus", "CONNECTED");
        } catch (Exception ex) {
            details.put("databaseStatus", "ERROR: " + ex.getMessage());
        }

        // 2. Flyway Migration Status
        try {
            MigrationInfo currentMigration = flyway.info().current();
            List<Map<String, String>> appliedMigrations = Arrays.stream(flyway.info().applied())
                    .map(info -> Map.of(
                            "version", info.getVersion().getVersion(),
                            "description", info.getDescription(),
                            "installedOn", info.getInstalledOn().toString()
                    ))
                    .toList();

            details.put("flywayCurrentVersion", currentMigration != null ? currentMigration.getVersion().getVersion() : "NONE");
            details.put("appliedMigrations", appliedMigrations);
        } catch (Exception ex) {
            details.put("flywayStatus", "ERROR: " + ex.getMessage());
        }

        // 3. Multi-Tenant Schema Validation (Verify sample tenant count)
        try {
            Integer tenantCount = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM tenants", Integer.class);
            Integer productCount = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM products", Integer.class);
            details.put("activeTenantsCount", tenantCount);
            details.put("seededProductsCount", productCount);
        } catch (Exception ex) {
            details.put("tenantValidation", "Tables pending or initializing");
        }

        return ResponseEntity.ok(ApiResponse.success("NEXBILL backend operational", details));
    }
}
