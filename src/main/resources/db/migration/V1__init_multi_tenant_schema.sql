-- ============================================================================
-- NEXBILL Multi-Tenant SaaS Schema - Migration V1
-- Shared Database, Shared Schema Architecture with Strict Tenant Isolation
-- ============================================================================

-- 1. TENANTS TABLE
CREATE TABLE IF NOT EXISTS tenants (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(150) NOT NULL,
    code VARCHAR(50) NOT NULL,
    gstin VARCHAR(20) NOT NULL,
    email VARCHAR(100) NOT NULL,
    phone VARCHAR(25) NOT NULL,
    address TEXT NOT NULL,
    city VARCHAR(80) NOT NULL,
    state VARCHAR(80) NOT NULL,
    state_code VARCHAR(10) NOT NULL,
    pincode VARCHAR(10) NOT NULL,
    logo_url VARCHAR(500),
    website VARCHAR(150),
    invoice_footer_message TEXT,
    business_mode VARCHAR(20) NOT NULL DEFAULT 'BUSINESS', -- 'SIMPLE' or 'BUSINESS'
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT uk_tenant_code UNIQUE (code)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 2. BRANCHES TABLE (Supports Multi-Branch Business Operations)
CREATE TABLE IF NOT EXISTS branches (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    tenant_id BIGINT NOT NULL,
    name VARCHAR(150) NOT NULL,
    branch_code VARCHAR(50) NOT NULL,
    phone VARCHAR(25),
    address TEXT,
    is_main_branch BOOLEAN NOT NULL DEFAULT FALSE,
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT fk_branches_tenant FOREIGN KEY (tenant_id) REFERENCES tenants(id) ON DELETE CASCADE,
    CONSTRAINT uk_tenant_branch_code UNIQUE (tenant_id, branch_code),
    INDEX idx_branches_tenant (tenant_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 3. USERS TABLE
CREATE TABLE IF NOT EXISTS users (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    tenant_id BIGINT NOT NULL,
    branch_id BIGINT,
    username VARCHAR(80) NOT NULL,
    password_hash VARCHAR(255) NOT NULL,
    full_name VARCHAR(150) NOT NULL,
    email VARCHAR(120) NOT NULL,
    phone VARCHAR(25),
    role VARCHAR(30) NOT NULL DEFAULT 'CASHIER', -- 'OWNER', 'ADMIN', 'MANAGER', 'CASHIER', 'STAFF'
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT fk_users_tenant FOREIGN KEY (tenant_id) REFERENCES tenants(id) ON DELETE CASCADE,
    CONSTRAINT fk_users_branch FOREIGN KEY (branch_id) REFERENCES branches(id) ON DELETE SET NULL,
    CONSTRAINT uk_tenant_username UNIQUE (tenant_id, username),
    CONSTRAINT uk_system_username UNIQUE (username),
    INDEX idx_users_tenant (tenant_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 4. BUSINESS SETTINGS TABLE
CREATE TABLE IF NOT EXISTS business_settings (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    tenant_id BIGINT NOT NULL,
    invoice_prefix VARCHAR(30) NOT NULL DEFAULT 'INV-2026-',
    quotation_prefix VARCHAR(30) NOT NULL DEFAULT 'QT-2026-',
    purchase_prefix VARCHAR(30) NOT NULL DEFAULT 'PO-2026-',
    currency VARCHAR(10) NOT NULL DEFAULT 'INR',
    currency_symbol VARCHAR(10) NOT NULL DEFAULT '₹',
    default_tax_rate DECIMAL(5,2) NOT NULL DEFAULT 5.00,
    tax_scheme VARCHAR(30) NOT NULL DEFAULT 'REGULAR', -- 'REGULAR', 'COMPOSITION'
    bank_name VARCHAR(100),
    account_number VARCHAR(50),
    ifsc_code VARCHAR(30),
    upi_id VARCHAR(100),
    terms_conditions TEXT,
    is_gst_enabled BOOLEAN NOT NULL DEFAULT TRUE,
    enable_round_off BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT fk_settings_tenant FOREIGN KEY (tenant_id) REFERENCES tenants(id) ON DELETE CASCADE,
    CONSTRAINT uk_tenant_settings UNIQUE (tenant_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 5. CATEGORIES TABLE
CREATE TABLE IF NOT EXISTS categories (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    tenant_id BIGINT NOT NULL,
    name VARCHAR(100) NOT NULL,
    description TEXT,
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT fk_categories_tenant FOREIGN KEY (tenant_id) REFERENCES tenants(id) ON DELETE CASCADE,
    CONSTRAINT uk_tenant_category UNIQUE (tenant_id, name),
    INDEX idx_categories_tenant (tenant_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 6. PRODUCTS TABLE
CREATE TABLE IF NOT EXISTS products (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    tenant_id BIGINT NOT NULL,
    category_id BIGINT,
    name VARCHAR(200) NOT NULL,
    sku VARCHAR(80) NOT NULL,
    barcode VARCHAR(80),
    description TEXT,
    purchase_price DECIMAL(14,2) NOT NULL DEFAULT 0.00,
    selling_price DECIMAL(14,2) NOT NULL DEFAULT 0.00,
    mrp DECIMAL(14,2),
    unit VARCHAR(30) NOT NULL DEFAULT 'Pcs',
    gst_percentage DECIMAL(5,2) NOT NULL DEFAULT 0.00,
    hsn_code VARCHAR(20),
    opening_stock DECIMAL(14,3) NOT NULL DEFAULT 0.000,
    current_stock DECIMAL(14,3) NOT NULL DEFAULT 0.000,
    min_stock_alert DECIMAL(14,3) NOT NULL DEFAULT 5.000,
    image_url VARCHAR(500),
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT fk_products_tenant FOREIGN KEY (tenant_id) REFERENCES tenants(id) ON DELETE CASCADE,
    CONSTRAINT fk_products_category FOREIGN KEY (category_id) REFERENCES categories(id) ON DELETE SET NULL,
    CONSTRAINT uk_tenant_sku UNIQUE (tenant_id, sku),
    INDEX idx_products_tenant (tenant_id),
    INDEX idx_products_barcode (tenant_id, barcode)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 7. STOCK MOVEMENTS TABLE (Mandatory Audit Trail for Every Stock Change)
CREATE TABLE IF NOT EXISTS stock_movements (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    tenant_id BIGINT NOT NULL,
    branch_id BIGINT,
    product_id BIGINT NOT NULL,
    movement_type VARCHAR(30) NOT NULL, -- 'OPENING', 'PURCHASE', 'SALE', 'SALE_RETURN', 'PURCHASE_RETURN', 'ADJUSTMENT_ADD', 'ADJUSTMENT_SUBTRACT'
    quantity DECIMAL(14,3) NOT NULL,
    unit_price DECIMAL(14,2) DEFAULT 0.00,
    previous_stock DECIMAL(14,3) NOT NULL,
    new_stock DECIMAL(14,3) NOT NULL,
    reason VARCHAR(255),
    reference_type VARCHAR(50), -- 'INVOICE', 'PURCHASE_ORDER', 'MANUAL_AUDIT'
    reference_id VARCHAR(80),
    created_by BIGINT,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_stock_movements_tenant FOREIGN KEY (tenant_id) REFERENCES tenants(id) ON DELETE CASCADE,
    CONSTRAINT fk_stock_movements_product FOREIGN KEY (product_id) REFERENCES products(id) ON DELETE CASCADE,
    INDEX idx_stock_movements_tenant (tenant_id),
    INDEX idx_stock_movements_product (tenant_id, product_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 8. CUSTOMERS TABLE
CREATE TABLE IF NOT EXISTS customers (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    tenant_id BIGINT NOT NULL,
    name VARCHAR(150) NOT NULL,
    phone VARCHAR(25),
    email VARCHAR(120),
    gstin VARCHAR(20),
    address TEXT,
    city VARCHAR(80),
    state VARCHAR(80),
    customer_type VARCHAR(50) DEFAULT 'Regular',
    opening_balance DECIMAL(14,2) NOT NULL DEFAULT 0.00,
    current_balance DECIMAL(14,2) NOT NULL DEFAULT 0.00,
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT fk_customers_tenant FOREIGN KEY (tenant_id) REFERENCES tenants(id) ON DELETE CASCADE,
    INDEX idx_customers_tenant (tenant_id),
    INDEX idx_customers_phone (tenant_id, phone)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 9. SUPPLIERS TABLE
CREATE TABLE IF NOT EXISTS suppliers (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    tenant_id BIGINT NOT NULL,
    name VARCHAR(150) NOT NULL,
    contact_person VARCHAR(100),
    phone VARCHAR(25),
    email VARCHAR(120),
    gstin VARCHAR(20),
    address TEXT,
    city VARCHAR(80),
    state VARCHAR(80),
    opening_balance DECIMAL(14,2) NOT NULL DEFAULT 0.00,
    current_balance DECIMAL(14,2) NOT NULL DEFAULT 0.00,
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT fk_suppliers_tenant FOREIGN KEY (tenant_id) REFERENCES tenants(id) ON DELETE CASCADE,
    INDEX idx_suppliers_tenant (tenant_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 10. INVOICES TABLE (Supports both ITEMIZED and SIMPLE_TOTAL sales)
CREATE TABLE IF NOT EXISTS invoices (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    tenant_id BIGINT NOT NULL,
    branch_id BIGINT,
    invoice_number VARCHAR(80) NOT NULL,
    invoice_type VARCHAR(20) NOT NULL DEFAULT 'ITEMIZED', -- 'ITEMIZED' or 'SIMPLE_TOTAL'
    customer_id BIGINT,
    customer_name VARCHAR(150) NOT NULL,
    customer_phone VARCHAR(25),
    customer_gstin VARCHAR(20),
    customer_address TEXT,
    invoice_date DATE NOT NULL,
    invoice_time TIME,
    subtotal DECIMAL(14,2) NOT NULL DEFAULT 0.00,
    item_discount DECIMAL(14,2) NOT NULL DEFAULT 0.00,
    order_discount DECIMAL(14,2) NOT NULL DEFAULT 0.00,
    taxable_amount DECIMAL(14,2) NOT NULL DEFAULT 0.00,
    cgst DECIMAL(14,2) NOT NULL DEFAULT 0.00,
    sgst DECIMAL(14,2) NOT NULL DEFAULT 0.00,
    igst DECIMAL(14,2) NOT NULL DEFAULT 0.00,
    total_tax DECIMAL(14,2) NOT NULL DEFAULT 0.00,
    round_off DECIMAL(6,2) NOT NULL DEFAULT 0.00,
    grand_total DECIMAL(14,2) NOT NULL DEFAULT 0.00,
    paid_amount DECIMAL(14,2) NOT NULL DEFAULT 0.00,
    balance_amount DECIMAL(14,2) NOT NULL DEFAULT 0.00,
    payment_method VARCHAR(50) NOT NULL DEFAULT 'Cash',
    payment_status VARCHAR(20) NOT NULL DEFAULT 'PAID', -- 'PAID', 'PARTIAL', 'UNPAID', 'CANCELLED'
    notes TEXT,
    cancel_reason VARCHAR(255),
    cancelled_at TIMESTAMP NULL,
    created_by BIGINT,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT fk_invoices_tenant FOREIGN KEY (tenant_id) REFERENCES tenants(id) ON DELETE CASCADE,
    CONSTRAINT fk_invoices_customer FOREIGN KEY (customer_id) REFERENCES customers(id) ON DELETE SET NULL,
    CONSTRAINT uk_tenant_invoice_number UNIQUE (tenant_id, invoice_number),
    INDEX idx_invoices_tenant (tenant_id),
    INDEX idx_invoices_date (tenant_id, invoice_date)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 11. INVOICE ITEMS TABLE
CREATE TABLE IF NOT EXISTS invoice_items (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    tenant_id BIGINT NOT NULL,
    invoice_id BIGINT NOT NULL,
    product_id BIGINT,
    item_name VARCHAR(200) NOT NULL,
    sku VARCHAR(80),
    hsn VARCHAR(20),
    quantity DECIMAL(14,3) NOT NULL,
    unit VARCHAR(30) NOT NULL DEFAULT 'Pcs',
    selling_price DECIMAL(14,2) NOT NULL,
    discount DECIMAL(14,2) NOT NULL DEFAULT 0.00,
    gst_rate DECIMAL(5,2) NOT NULL DEFAULT 0.00,
    taxable_amount DECIMAL(14,2) NOT NULL,
    cgst DECIMAL(14,2) NOT NULL DEFAULT 0.00,
    sgst DECIMAL(14,2) NOT NULL DEFAULT 0.00,
    igst DECIMAL(14,2) NOT NULL DEFAULT 0.00,
    total DECIMAL(14,2) NOT NULL,
    CONSTRAINT fk_invoice_items_tenant FOREIGN KEY (tenant_id) REFERENCES tenants(id) ON DELETE CASCADE,
    CONSTRAINT fk_invoice_items_invoice FOREIGN KEY (invoice_id) REFERENCES invoices(id) ON DELETE CASCADE,
    CONSTRAINT fk_invoice_items_product FOREIGN KEY (product_id) REFERENCES products(id) ON DELETE SET NULL,
    INDEX idx_invoice_items_tenant (tenant_id),
    INDEX idx_invoice_items_invoice (invoice_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 12. PURCHASES TABLE
CREATE TABLE IF NOT EXISTS purchases (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    tenant_id BIGINT NOT NULL,
    branch_id BIGINT,
    purchase_number VARCHAR(80) NOT NULL,
    supplier_id BIGINT,
    supplier_name VARCHAR(150) NOT NULL,
    supplier_gstin VARCHAR(20),
    purchase_date DATE NOT NULL,
    subtotal DECIMAL(14,2) NOT NULL DEFAULT 0.00,
    tax_amount DECIMAL(14,2) NOT NULL DEFAULT 0.00,
    discount DECIMAL(14,2) NOT NULL DEFAULT 0.00,
    total_amount DECIMAL(14,2) NOT NULL DEFAULT 0.00,
    payment_status VARCHAR(20) NOT NULL DEFAULT 'PAID', -- 'PAID', 'PENDING', 'CANCELLED'
    payment_method VARCHAR(50) NOT NULL DEFAULT 'Bank Transfer',
    notes TEXT,
    created_by BIGINT,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT fk_purchases_tenant FOREIGN KEY (tenant_id) REFERENCES tenants(id) ON DELETE CASCADE,
    CONSTRAINT fk_purchases_supplier FOREIGN KEY (supplier_id) REFERENCES suppliers(id) ON DELETE SET NULL,
    CONSTRAINT uk_tenant_purchase_number UNIQUE (tenant_id, purchase_number),
    INDEX idx_purchases_tenant (tenant_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 13. PURCHASE ITEMS TABLE
CREATE TABLE IF NOT EXISTS purchase_items (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    tenant_id BIGINT NOT NULL,
    purchase_id BIGINT NOT NULL,
    product_id BIGINT,
    item_name VARCHAR(200) NOT NULL,
    sku VARCHAR(80),
    quantity DECIMAL(14,3) NOT NULL,
    unit VARCHAR(30) NOT NULL DEFAULT 'Pcs',
    purchase_rate DECIMAL(14,2) NOT NULL,
    gst_rate DECIMAL(5,2) NOT NULL DEFAULT 0.00,
    total DECIMAL(14,2) NOT NULL,
    CONSTRAINT fk_purchase_items_tenant FOREIGN KEY (tenant_id) REFERENCES tenants(id) ON DELETE CASCADE,
    CONSTRAINT fk_purchase_items_purchase FOREIGN KEY (purchase_id) REFERENCES purchases(id) ON DELETE CASCADE,
    CONSTRAINT fk_purchase_items_product FOREIGN KEY (product_id) REFERENCES products(id) ON DELETE SET NULL,
    INDEX idx_purchase_items_tenant (tenant_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 14. EXPENSE CATEGORIES TABLE
CREATE TABLE IF NOT EXISTS expense_categories (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    tenant_id BIGINT NOT NULL,
    name VARCHAR(100) NOT NULL,
    description TEXT,
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT fk_exp_cats_tenant FOREIGN KEY (tenant_id) REFERENCES tenants(id) ON DELETE CASCADE,
    CONSTRAINT uk_tenant_expense_cat UNIQUE (tenant_id, name),
    INDEX idx_expense_cats_tenant (tenant_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 15. EXPENSES TABLE
CREATE TABLE IF NOT EXISTS expenses (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    tenant_id BIGINT NOT NULL,
    branch_id BIGINT,
    expense_category_id BIGINT,
    category_name VARCHAR(100) NOT NULL,
    description TEXT,
    amount DECIMAL(14,2) NOT NULL,
    payment_method VARCHAR(50) NOT NULL DEFAULT 'Cash',
    expense_date DATE NOT NULL,
    reference_number VARCHAR(100),
    created_by BIGINT,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT fk_expenses_tenant FOREIGN KEY (tenant_id) REFERENCES tenants(id) ON DELETE CASCADE,
    CONSTRAINT fk_expenses_category FOREIGN KEY (expense_category_id) REFERENCES expense_categories(id) ON DELETE SET NULL,
    INDEX idx_expenses_tenant (tenant_id),
    INDEX idx_expenses_date (tenant_id, expense_date)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 16. PAYMENTS TABLE (Unified Financial Transaction Log)
CREATE TABLE IF NOT EXISTS payments (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    tenant_id BIGINT NOT NULL,
    branch_id BIGINT,
    payment_type VARCHAR(50) NOT NULL, -- 'INVOICE_RECEIPT', 'CUSTOMER_SETTLEMENT', 'PURCHASE_PAYMENT', 'SUPPLIER_SETTLEMENT', 'EXPENSE_PAYMENT'
    flow_type VARCHAR(10) NOT NULL, -- 'INFLOW' or 'OUTFLOW'
    reference_type VARCHAR(50),
    reference_id VARCHAR(80),
    party_type VARCHAR(50) NOT NULL, -- 'CUSTOMER', 'SUPPLIER', 'EXPENSE_PAYEE', 'WALK_IN'
    party_id BIGINT,
    party_name VARCHAR(150) NOT NULL,
    payment_mode VARCHAR(50) NOT NULL, -- 'CASH', 'UPI', 'CARD', 'BANK_TRANSFER'
    amount DECIMAL(14,2) NOT NULL,
    transaction_reference VARCHAR(100),
    notes TEXT,
    payment_date DATE NOT NULL,
    created_by BIGINT,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_payments_tenant FOREIGN KEY (tenant_id) REFERENCES tenants(id) ON DELETE CASCADE,
    INDEX idx_payments_tenant (tenant_id),
    INDEX idx_payments_date (tenant_id, payment_date)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 17. AUDIT LOGS TABLE
CREATE TABLE IF NOT EXISTS audit_logs (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    tenant_id BIGINT NOT NULL,
    user_id BIGINT,
    action VARCHAR(100) NOT NULL,
    entity_type VARCHAR(80) NOT NULL,
    entity_id VARCHAR(80),
    details TEXT,
    ip_address VARCHAR(45),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_audit_logs_tenant FOREIGN KEY (tenant_id) REFERENCES tenants(id) ON DELETE CASCADE,
    INDEX idx_audit_logs_tenant (tenant_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
