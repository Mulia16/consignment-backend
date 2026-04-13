-- =============================================================
-- SEED: Master Data for Development / Testing
-- =============================================================

-- Item Setup
INSERT INTO consignment_item_setup (item_code, hierarchy, item_model, item_name, variant, unit_retail, mvc, category_l1, category_l2, category_l3, sync_flag, deleted_flag)
VALUES
    ('ITEM001', 'CONSIGNMENT', 'LP-15-2024',  'Laptop Pro 15',     '16GB/512GB Silver', 15000000, 12000000, 'Electronics', 'Computers',  'Laptops',  true, false),
    ('ITEM002', 'CONSIGNMENT', 'LP-13-2024',  'Laptop Air 13',     '8GB/256GB Gold',    10000000,  8000000, 'Electronics', 'Computers',  'Laptops',  true, false),
    ('ITEM003', 'CONSIGNMENT', 'TAB-10-2024', 'Tablet Pro 10',     '128GB WiFi Black',   7000000,  5500000, 'Electronics', 'Tablets',    'Android',  true, false),
    ('ITEM004', 'CONSIGNMENT', 'PHN-X-2024',  'Smartphone X',      '256GB Midnight',    12000000,  9500000, 'Electronics', 'Phones',     'Android',  true, false),
    ('ITEM005', 'CONSIGNMENT', 'MON-27-2024', 'Monitor 27 inch',   '4K IPS Silver',      8000000,  6500000, 'Electronics', 'Peripherals','Monitors', true, false),
    ('ITEM006', 'CONSIGNMENT', 'KBD-WL-2024', 'Wireless Keyboard', 'Compact White',      1500000,  1200000, 'Electronics', 'Peripherals','Input',    true, false),
    ('ITEM007', 'CONSIGNMENT', 'MSE-WL-2024', 'Wireless Mouse',    'Ergonomic Black',     800000,   650000, 'Electronics', 'Peripherals','Input',    true, false),
    ('ITEM008', 'CONSIGNMENT', 'SSD-1T-2024', 'SSD 1TB',           'NVMe M.2',           2500000,  2000000, 'Electronics', 'Storage',    'SSD',      true, false)
ON CONFLICT (item_code) DO NOTHING;

-- External Supplier Setup (SUPP001 → COMP01/STORE01, COMP01/STORE02)
INSERT INTO consignment_external_supplier (id, item_code, supplier_code, supplier_type, supplier_contract, consignee_company, consignee_store, current_inventory_qty)
VALUES
    -- SUPP001 / CONTRACT-2024-001 → COMP01/STORE01
    (gen_random_uuid()::text, 'ITEM001', 'SUPP001', 'EXTERNAL', 'CONTRACT-2024-001', 'COMP01', 'STORE01', 50),
    (gen_random_uuid()::text, 'ITEM002', 'SUPP001', 'EXTERNAL', 'CONTRACT-2024-001', 'COMP01', 'STORE01', 30),
    (gen_random_uuid()::text, 'ITEM003', 'SUPP001', 'EXTERNAL', 'CONTRACT-2024-001', 'COMP01', 'STORE01', 40),
    (gen_random_uuid()::text, 'ITEM004', 'SUPP001', 'EXTERNAL', 'CONTRACT-2024-001', 'COMP01', 'STORE01', 25),
    -- SUPP001 / CONTRACT-2024-001 → COMP01/STORE02
    (gen_random_uuid()::text, 'ITEM001', 'SUPP001', 'EXTERNAL', 'CONTRACT-2024-001', 'COMP01', 'STORE02', 20),
    (gen_random_uuid()::text, 'ITEM002', 'SUPP001', 'EXTERNAL', 'CONTRACT-2024-001', 'COMP01', 'STORE02', 15),
    -- SUPP002 / CONTRACT-2024-002 → COMP01/STORE01
    (gen_random_uuid()::text, 'ITEM005', 'SUPP002', 'EXTERNAL', 'CONTRACT-2024-002', 'COMP01', 'STORE01', 30),
    (gen_random_uuid()::text, 'ITEM006', 'SUPP002', 'EXTERNAL', 'CONTRACT-2024-002', 'COMP01', 'STORE01', 100),
    (gen_random_uuid()::text, 'ITEM007', 'SUPP002', 'EXTERNAL', 'CONTRACT-2024-002', 'COMP01', 'STORE01', 80),
    (gen_random_uuid()::text, 'ITEM008', 'SUPP002', 'EXTERNAL', 'CONTRACT-2024-002', 'COMP01', 'STORE01', 60),
    -- SUPP002 / CONTRACT-2024-002 → COMP02/STORE03
    (gen_random_uuid()::text, 'ITEM005', 'SUPP002', 'EXTERNAL', 'CONTRACT-2024-002', 'COMP02', 'STORE03', 20),
    (gen_random_uuid()::text, 'ITEM006', 'SUPP002', 'EXTERNAL', 'CONTRACT-2024-002', 'COMP02', 'STORE03', 50)
ON CONFLICT DO NOTHING;

-- Internal Supplier Setup (INT-SUPP01 = STORE01 supplies to STORE02)
INSERT INTO consignment_internal_supplier (id, item_code, supplier_code, supplier_store, consignee_company, consignee_store)
VALUES
    (gen_random_uuid()::text, 'ITEM001', 'INT-SUPP01', 'STORE01', 'COMP01', 'STORE02'),
    (gen_random_uuid()::text, 'ITEM002', 'INT-SUPP01', 'STORE01', 'COMP01', 'STORE02'),
    (gen_random_uuid()::text, 'ITEM003', 'INT-SUPP01', 'STORE01', 'COMP01', 'STORE02')
ON CONFLICT DO NOTHING;

-- Item Prices
INSERT INTO consignment_item_price (id, item_code, company, store, supplier_code, supplier_contract, customer_code, unit_price, currency, effective_from)
VALUES
    -- SUPP001 / CONTRACT-2024-001 prices
    (gen_random_uuid()::text, 'ITEM001', 'COMP01', 'STORE01', 'SUPP001', 'CONTRACT-2024-001', '', 15000000, 'IDR', '2024-01-01'),
    (gen_random_uuid()::text, 'ITEM002', 'COMP01', 'STORE01', 'SUPP001', 'CONTRACT-2024-001', '', 10000000, 'IDR', '2024-01-01'),
    (gen_random_uuid()::text, 'ITEM003', 'COMP01', 'STORE01', 'SUPP001', 'CONTRACT-2024-001', '', 7000000,  'IDR', '2024-01-01'),
    (gen_random_uuid()::text, 'ITEM004', 'COMP01', 'STORE01', 'SUPP001', 'CONTRACT-2024-001', '', 12000000, 'IDR', '2024-01-01'),
    -- SUPP002 / CONTRACT-2024-002 prices
    (gen_random_uuid()::text, 'ITEM005', 'COMP01', 'STORE01', 'SUPP002', 'CONTRACT-2024-002', '', 8000000,  'IDR', '2024-01-01'),
    (gen_random_uuid()::text, 'ITEM006', 'COMP01', 'STORE01', 'SUPP002', 'CONTRACT-2024-002', '', 1500000,  'IDR', '2024-01-01'),
    (gen_random_uuid()::text, 'ITEM007', 'COMP01', 'STORE01', 'SUPP002', 'CONTRACT-2024-002', '', 800000,   'IDR', '2024-01-01'),
    (gen_random_uuid()::text, 'ITEM008', 'COMP01', 'STORE01', 'SUPP002', 'CONTRACT-2024-002', '', 2500000,  'IDR', '2024-01-01')
ON CONFLICT DO NOTHING;

-- Supplier Book Value Inventory (initial stock)
INSERT INTO supplier_book_value_inventory (store, sku, supplier_code, supplier_contract, purchase_qty, closing_qty, unbill_qty)
VALUES
    ('STORE01', 'ITEM001', 'SUPP001', 'CONTRACT-2024-001', 50, 50, 0),
    ('STORE01', 'ITEM002', 'SUPP001', 'CONTRACT-2024-001', 30, 30, 0),
    ('STORE01', 'ITEM003', 'SUPP001', 'CONTRACT-2024-001', 40, 40, 0),
    ('STORE01', 'ITEM004', 'SUPP001', 'CONTRACT-2024-001', 25, 25, 0),
    ('STORE01', 'ITEM005', 'SUPP002', 'CONTRACT-2024-002', 30, 30, 0),
    ('STORE01', 'ITEM006', 'SUPP002', 'CONTRACT-2024-002', 100, 100, 0),
    ('STORE01', 'ITEM007', 'SUPP002', 'CONTRACT-2024-002', 80, 80, 0),
    ('STORE01', 'ITEM008', 'SUPP002', 'CONTRACT-2024-002', 60, 60, 0),
    ('STORE02', 'ITEM001', 'SUPP001', 'CONTRACT-2024-001', 20, 20, 0),
    ('STORE02', 'ITEM002', 'SUPP001', 'CONTRACT-2024-001', 15, 15, 0),
    ('STORE03', 'ITEM005', 'SUPP002', 'CONTRACT-2024-002', 20, 20, 0),
    ('STORE03', 'ITEM006', 'SUPP002', 'CONTRACT-2024-002', 50, 50, 0)
ON CONFLICT (store, sku, supplier_code, supplier_contract) DO NOTHING;
