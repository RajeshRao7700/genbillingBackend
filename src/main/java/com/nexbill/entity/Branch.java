package com.nexbill.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.Instant;

@Entity
@Table(name = "branches")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Branch {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "tenant_id", nullable = false)
    private Long tenantId;

    @Column(name = "name", length = 150, nullable = false)
    private String name;

    @Column(name = "branch_code", length = 50, nullable = false)
    private String branchCode;

    @Column(name = "phone", length = 25)
    private String phone;

    @Column(name = "email", length = 120)
    private String email;

    @Column(name = "address", columnDefinition = "TEXT")
    private String address;

    @Column(name = "city", length = 80)
    private String city;

    @Column(name = "state", length = 80)
    private String state;

    @Column(name = "pincode", length = 10)
    private String pincode;

    @Column(name = "gstin", length = 20)
    private String gstin;

    @Column(name = "is_main_branch", nullable = false)
    @Builder.Default
    private Boolean isMainBranch = false;

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

    public boolean isStatusActive() {
        return "ACTIVE".equalsIgnoreCase(this.status) && Boolean.TRUE.equals(this.isActive);
    }
}
