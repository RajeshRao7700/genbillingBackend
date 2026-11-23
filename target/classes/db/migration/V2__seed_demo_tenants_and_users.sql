-- ============================================================================
-- NEXBILL Multi-Tenant Seed Migration V2
-- Seeds Demo Tenants: Chowmow Foods (Tenant 1) & Instant Food (Tenant 2)
-- Password Hashes: BCrypt 10 rounds for Chowmow@123 and Instant@123
-- ============================================================================

-- 1. SEED TENANTS
INSERT INTO tenants (id, name, code, gstin, email, phone, address, city, state, state_code, pincode, logo_url, website, invoice_footer_message, business_mode, is_active)
VALUES
(1, 'Chowmow Foods', 'chowmow', '36AABCC1234D1Z5', 'billing@chowmowfoods.com', '+91 98490 12345', 'Plot 12, Road No. 36, Jubilee Hills', 'Hyderabad', 'Telangana', '36', '500033', 'https://images.unsplash.com/photo-1555396273-367ea4eb4db5?w=120', 'www.chowmowfoods.com', 'Thank you for dining with Chowmow Foods! Fresh taste, genuine passion.', 'BUSINESS', TRUE),
(2, 'Instant Food', 'instantfood', '36AAACI9876E1Z2', 'billing@instantfood.in', '+91 97011 54321', 'Survey No. 45, Hitec City Main Road, Madhapur', 'Hyderabad', 'Telangana', '36', '500081', 'https://images.unsplash.com/photo-1568901346375-23c9450c58cd?w=120', 'www.instantfood.in', 'Thank you for choosing Instant Food! Quick meals, zero hassle.', 'BUSINESS', TRUE);

-- 2. SEED MAIN BRANCHES
INSERT INTO branches (id, tenant_id, name, branch_code, phone, address, is_main_branch, is_active)
VALUES
(1, 1, 'Chowmow Foods - Jubilee Hills Main', 'CMF-MAIN', '+91 98490 12345', 'Plot 12, Road No. 36, Jubilee Hills, Hyderabad - 500033', TRUE, TRUE),
(2, 2, 'Instant Food - Madhapur Flagship', 'IF-MAIN', '+91 97011 54321', 'Survey No. 45, Hitec City Main Road, Madhapur, Hyderabad - 500081', TRUE, TRUE);

-- 3. SEED USERS (Password: Chowmow@123 and Instant@123)
INSERT INTO users (id, tenant_id, branch_id, username, password_hash, full_name, email, phone, role, is_active)
VALUES
(1, 1, 1, 'chowmow', '$2a$10$SLpdsJI3xZ8SnaJfRlbd5OxmP2zk7ky1nkwf9Bti8BnBInRUF4Mze', 'Chowmow Foods Admin', 'admin@chowmowfoods.com', '+91 98490 12345', 'OWNER', TRUE),
(2, 2, 2, 'instantfood', '$2a$10$pXcNY.8/QSNGwNB2QmoXCevTXuvna1O/FDX/3pybPjXoYYaA7t.gK', 'Instant Food Admin', 'admin@instantfood.in', '+91 97011 54321', 'OWNER', TRUE);

-- 4. SEED BUSINESS SETTINGS
INSERT INTO business_settings (id, tenant_id, invoice_prefix, quotation_prefix, purchase_prefix, currency, currency_symbol, default_tax_rate, tax_scheme, bank_name, account_number, ifsc_code, upi_id, terms_conditions, is_gst_enabled, enable_round_off)
VALUES
(1, 1, 'CMF-2026-', 'CMF-QT-2026-', 'CMF-PO-2026-', 'INR', '₹', 5.00, 'REGULAR', 'ICICI Bank Ltd', '623405012398', 'ICIC0006234', 'chowmow@icici', '1. Goods & services once sold are subject to store policy.\n2. Invoices subject to Hyderabad Jurisdiction.', TRUE, TRUE),
(2, 2, 'IF-2026-', 'IF-QT-2026-', 'IF-PO-2026-', 'INR', '₹', 5.00, 'REGULAR', 'HDFC Bank Ltd', '50200088912345', 'HDFC0000456', 'instantfood@hdfcbank', '1. Ready-to-eat products must be checked at counter.\n2. Subject to Hyderabad Jurisdiction.', TRUE, TRUE);

-- 5. SEED CATEGORIES
INSERT INTO categories (id, tenant_id, name, description, is_active)
VALUES
-- Chowmow Foods Categories
(1, 1, 'Noodles & Fried Rice', 'Wok-tossed noodles, fried rice and Indo-Chinese specialties', TRUE),
(2, 1, 'Biryanis & Meals', 'Hyderabadi dum biryanis and authentic curry meal boxes', TRUE),
(3, 1, 'Starters & Appetizers', 'Crispy appetizers, chicken tikka and street appetizers', TRUE),
(4, 1, 'Beverages & Coolers', 'Fresh juices, mojitos and cold brews', TRUE),
-- Instant Food Categories
(5, 2, 'Burgers & Sandwiches', 'Gourmet burgers, brioche sandwiches and subs', TRUE),
(6, 2, 'Pizzas & Garlic Bread', 'Hand-stretched crust pizzas and mozzarella garlic bread', TRUE),
(7, 2, 'Ready-to-Eat Bowls', 'Fast heating dal makhani, paneer and rice bowls', TRUE),
(8, 2, 'Instant Soups & Noodles', 'Instant broth cup noodles and hot soups', TRUE);

-- 6. SEED PRODUCTS
INSERT INTO products (id, tenant_id, category_id, name, sku, barcode, description, purchase_price, selling_price, mrp, unit, gst_percentage, hsn_code, opening_stock, current_stock, min_stock_alert, is_active)
VALUES
-- Chowmow Foods Products (Tenant 1)
(1, 1, 1, 'Veg Hakka Noodles', 'CMF-NOOD-01', '8901001001011', 'Classic wok tossed noodles with fresh vegetables', 65.00, 160.00, 180.00, 'Portion', 5.00, '1902', 50.000, 50.000, 10.000, TRUE),
(2, 1, 2, 'Hyderabadi Chicken Dum Biryani', 'CMF-BIRY-01', '8901001001028', 'Slow cooked aromatic basmati rice with spiced chicken', 120.00, 260.00, 290.00, 'Portion', 5.00, '2106', 30.000, 30.000, 8.000, TRUE),
(3, 1, 3, 'Crispy Corn Salt & Pepper', 'CMF-STRT-01', '8901001001042', 'Crispy fried sweet corn with cracked pepper and scallions', 55.00, 150.00, 170.00, 'Portion', 5.00, '2005', 40.000, 40.000, 8.000, TRUE),
(4, 1, 4, 'Fresh Lime Soda', 'CMF-BEV-01', '8901001001066', 'Chilled sparkling soda with fresh Telangana lime juice', 15.00, 60.00, 70.00, 'Glass', 12.00, '2202', 60.000, 60.000, 15.000, TRUE),
-- Instant Food Products (Tenant 2)
(5, 2, 5, 'Crispy Chicken Burger with Cheese', 'IF-BRG-01', '8902002002012', 'Crumb fried chicken breast patty with cheddar in brioche', 75.00, 180.00, 199.00, 'Pcs', 5.00, '1905', 45.000, 45.000, 10.000, TRUE),
(6, 2, 6, 'Veg Supreme 8-inch Pizza', 'IF-PIZ-01', '8902002002029', 'Baked mozzarella pizza with olives and bell peppers', 90.00, 220.00, 249.00, 'Pcs', 5.00, '1905', 30.000, 30.000, 8.000, TRUE),
(7, 2, 7, 'Ready-to-Eat Dal Makhani Bowl', 'IF-RTE-01', '8902002002036', 'Black lentils in pure cow butter and fresh cream', 62.00, 150.00, 165.00, 'Pack', 5.00, '2106', 50.000, 50.000, 12.000, TRUE),
(8, 2, 8, 'Instant Hot & Sour Cup Noodles', 'IF-NDL-01', '8902002002043', 'Quick-cook cup noodles with mushrooms and tangy spices', 32.00, 75.00, 85.00, 'Cup', 12.00, '1902', 5.000, 5.000, 10.000, TRUE);

-- 7. INITIAL STOCK MOVEMENTS FOR AUDIT TRACKING
INSERT INTO stock_movements (tenant_id, branch_id, product_id, movement_type, quantity, unit_price, previous_stock, new_stock, reason, reference_type, reference_id, created_by)
VALUES
(1, 1, 1, 'OPENING', 50.000, 65.00, 0.000, 50.000, 'Initial opening stock ledger entry', 'INITIAL_STOCK', 'INIT-001', 1),
(1, 1, 2, 'OPENING', 30.000, 120.00, 0.000, 30.000, 'Initial opening stock ledger entry', 'INITIAL_STOCK', 'INIT-002', 1),
(1, 1, 3, 'OPENING', 40.000, 55.00, 0.000, 40.000, 'Initial opening stock ledger entry', 'INITIAL_STOCK', 'INIT-003', 1),
(1, 1, 4, 'OPENING', 60.000, 15.00, 0.000, 60.000, 'Initial opening stock ledger entry', 'INITIAL_STOCK', 'INIT-004', 1),
(2, 2, 5, 'OPENING', 45.000, 75.00, 0.000, 45.000, 'Initial opening stock ledger entry', 'INITIAL_STOCK', 'INIT-005', 2),
(2, 2, 6, 'OPENING', 30.000, 90.00, 0.000, 30.000, 'Initial opening stock ledger entry', 'INITIAL_STOCK', 'INIT-006', 2),
(2, 2, 7, 'OPENING', 50.000, 62.00, 0.000, 50.000, 'Initial opening stock ledger entry', 'INITIAL_STOCK', 'INIT-007', 2),
(2, 2, 8, 'OPENING', 5.000, 32.00, 0.000, 5.000, 'Initial opening stock ledger entry', 'INITIAL_STOCK', 'INIT-008', 2);
