package com.nexbill.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.Instant;

@Entity
@Table(name = "users")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "tenant_id", nullable = false)
    private Long tenantId;

    @Column(name = "branch_id")
    private Long branchId;

    @Column(name = "username", length = 80, nullable = false, unique = true)
    private String username;

    @Column(name = "password_hash", length = 255, nullable = false)
    private String passwordHash;

    @Column(name = "full_name", length = 150, nullable = false)
    private String fullName;

    @Column(name = "email", length = 120, nullable = false, unique = true)
    private String email;

    @Column(name = "phone", length = 25)
    private String phone;

    @Enumerated(EnumType.STRING)
    @Column(name = "role", length = 30, nullable = false)
    @Builder.Default
    private UserRole role = UserRole.CASHIER;

    @Column(name = "status", length = 20, nullable = false)
    @Builder.Default
    private String status = "ACTIVE";

    @Column(name = "is_active", nullable = false)
    @Builder.Default
    private Boolean isActive = true;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    public boolean isAccountActive() {
        return Boolean.TRUE.equals(isActive) && !"INACTIVE".equalsIgnoreCase(status) && !"DEACTIVATED".equalsIgnoreCase(status);
    }

    @PrePersist
    @PreUpdate
    public void syncStatus() {
        if (this.status == null || this.status.isBlank()) {
            this.status = Boolean.TRUE.equals(this.isActive) ? "ACTIVE" : "INACTIVE";
        }
    }
}
