package com.nexbill.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.Instant;

@Entity
@Table(name = "business_settings")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BusinessSettings {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "tenant_id", nullable = false, unique = true)
    private Long tenantId;

    @Column(name = "invoice_prefix", length = 30, nullable = false)
    @Builder.Default
    private String invoicePrefix = "INV-2026-";

    @Column(name = "quotation_prefix", length = 30, nullable = false)
    @Builder.Default
    private String quotationPrefix = "QT-2026-";

    @Column(name = "purchase_prefix", length = 30, nullable = false)
    @Builder.Default
    private String purchasePrefix = "PO-2026-";

    @Column(name = "currency", length = 10, nullable = false)
    @Builder.Default
    private String currency = "INR";

    @Column(name = "currency_symbol", length = 10, nullable = false)
    @Builder.Default
    private String currencySymbol = "₹";

    @Column(name = "default_tax_rate", precision = 5, scale = 2, nullable = false)
    @Builder.Default
    private BigDecimal defaultTaxRate = new BigDecimal("5.00");

    @Column(name = "tax_scheme", length = 30, nullable = false)
    @Builder.Default
    private String taxScheme = "REGULAR";

    @Column(name = "bank_name", length = 100)
    private String bankName;

    @Column(name = "account_number", length = 50)
    private String accountNumber;

    @Column(name = "ifsc_code", length = 30)
    private String ifscCode;

    @Column(name = "upi_id", length = 100)
    private String upiId;

    @Column(name = "terms_conditions", columnDefinition = "TEXT")
    private String termsConditions;

    @Column(name = "is_gst_enabled", nullable = false)
    @Builder.Default
    private Boolean isGstEnabled = true;

    @Column(name = "enable_round_off", nullable = false)
    @Builder.Default
    private Boolean enableRoundOff = true;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;
}
