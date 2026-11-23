package com.nexbill.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.Instant;

@Entity
@Table(name = "tenants")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Tenant {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "name", length = 150, nullable = false)
    private String name;

    @Column(name = "business_name", length = 150)
    private String businessName;

    @Column(name = "owner_name", length = 150)
    private String ownerName;

    @Column(name = "code", length = 50, nullable = false, unique = true)
    private String code;

    @Column(name = "gstin", length = 20)
    private String gstin;

    @Column(name = "email", length = 100, nullable = false)
    private String email;

    @Column(name = "phone", length = 25, nullable = false)
    private String phone;

    @Column(name = "address", columnDefinition = "TEXT")
    private String address;

    @Column(name = "city", length = 80)
    private String city;

    @Column(name = "state", length = 80)
    private String state;

    @Column(name = "state_code", length = 10)
    private String stateCode;

    @Column(name = "pincode", length = 10)
    private String pincode;

    @Column(name = "logo_url", length = 500)
    private String logoUrl;

    @Column(name = "website", length = 150)
    private String website;

    @Column(name = "invoice_footer_message", columnDefinition = "TEXT")
    private String invoiceFooterMessage;

    @Enumerated(EnumType.STRING)
    @Column(name = "business_mode", length = 20, nullable = false)
    @Builder.Default
    private BusinessMode businessMode = BusinessMode.BUSINESS;

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

    public String getEffectiveBusinessName() {
        return businessName != null && !businessName.isBlank() ? businessName : name;
    }

    @PrePersist
    @PreUpdate
    public void syncBusinessName() {
        if (this.businessName == null || this.businessName.isBlank()) {
            this.businessName = this.name;
        }
        if (this.name == null || this.name.isBlank()) {
            this.name = this.businessName;
        }
        if (this.status == null || this.status.isBlank()) {
            this.status = Boolean.TRUE.equals(this.isActive) ? "ACTIVE" : "INACTIVE";
        }
    }
}
