-- ============================================================================
-- NEXBILL Phase 3 Migration V4
-- Business Profile enhancements, Branch enhancements, Employee fields
-- ============================================================================

-- ============================================================================
-- 1. TENANTS TABLE – Add Phase 3 business profile fields
-- ============================================================================

ALTER TABLE tenants ADD COLUMN alternate_phone  VARCHAR(25)  NULL AFTER phone;
ALTER TABLE tenants ADD COLUMN pan              VARCHAR(15)  NULL AFTER gstin;
ALTER TABLE tenants ADD COLUMN business_type    VARCHAR(80)  NULL AFTER business_mode;
ALTER TABLE tenants ADD COLUMN country          VARCHAR(80)  NOT NULL DEFAULT 'India' AFTER state_code;
ALTER TABLE tenants ADD COLUMN next_invoice_number INT        NOT NULL DEFAULT 1 AFTER invoice_footer_message;
-- Note: invoice_footer_message already serves as default_invoice_footer; reused via alias in service layer.

-- ============================================================================
-- 2. BRANCHES TABLE – Add Phase 3 extended branch fields
-- ============================================================================

ALTER TABLE branches ADD COLUMN email    VARCHAR(120) NULL AFTER phone;
ALTER TABLE branches ADD COLUMN city     VARCHAR(80)  NULL AFTER address;
ALTER TABLE branches ADD COLUMN state    VARCHAR(80)  NULL AFTER city;
ALTER TABLE branches ADD COLUMN pincode  VARCHAR(10)  NULL AFTER state;
ALTER TABLE branches ADD COLUMN gstin    VARCHAR(20)  NULL AFTER pincode;
ALTER TABLE branches ADD COLUMN status   VARCHAR(20)  NOT NULL DEFAULT 'ACTIVE' AFTER gstin;

-- ============================================================================
-- 3. USERS TABLE – No structural change needed.
--    branch_id, role, status, is_active already present from V1/V3.
-- ============================================================================

-- ============================================================================
-- 4. Indexes for performance
-- ============================================================================

-- Index branches by status for active-branch queries
CREATE INDEX idx_branches_status ON branches (tenant_id, status);

-- Index users by role for last-owner protection queries
CREATE INDEX idx_users_role_active ON users (tenant_id, role, is_active);
