-- ============================================================================
-- NEXBILL Multi-Tenant Phase 2 Migration V3
-- Schema upgrades for Authentication, Multi-Tenancy and User Management
-- ============================================================================

-- 1. Upgrade TENANTS table with business_name, owner_name, and status
ALTER TABLE tenants ADD COLUMN business_name VARCHAR(150) NULL;
ALTER TABLE tenants ADD COLUMN owner_name VARCHAR(150) NULL;
ALTER TABLE tenants ADD COLUMN status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE';

-- Make physical address and tax registration optional for initial owner registration
ALTER TABLE tenants MODIFY COLUMN gstin VARCHAR(20) NULL;
ALTER TABLE tenants MODIFY COLUMN address TEXT NULL;
ALTER TABLE tenants MODIFY COLUMN city VARCHAR(80) NULL;
ALTER TABLE tenants MODIFY COLUMN state VARCHAR(80) NULL;
ALTER TABLE tenants MODIFY COLUMN state_code VARCHAR(10) NULL;
ALTER TABLE tenants MODIFY COLUMN pincode VARCHAR(10) NULL;

-- Backfill existing Phase 1 demo tenants
UPDATE tenants SET business_name = name WHERE business_name IS NULL;
UPDATE tenants SET owner_name = 'Chowmow Foods Admin' WHERE id = 1;
UPDATE tenants SET owner_name = 'Instant Food Admin' WHERE id = 2;

-- 2. Upgrade USERS table with status and global unique email constraint
ALTER TABLE users ADD COLUMN status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE';
ALTER TABLE users ADD CONSTRAINT uk_users_email UNIQUE (email);

-- Ensure existing users have ACTIVE status
UPDATE users SET status = 'ACTIVE' WHERE status IS NULL;
