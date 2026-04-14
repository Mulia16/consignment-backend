-- Consignment Unpost Sales Inventory
-- Populated by ACMM via API (POS Sales, B2B Sales, Online Sales)
-- Used as source for Customer Consignment Billing Request (CBR) computation
CREATE TABLE IF NOT EXISTS consignment_unpost (
    id              BIGSERIAL PRIMARY KEY,
    store           VARCHAR(50)  NOT NULL,   -- consignee store (outlet)
    sku             VARCHAR(50)  NOT NULL,   -- item code
    sales_qty       NUMERIC(18,4) NOT NULL DEFAULT 0,
    sales_return_qty NUMERIC(18,4) NOT NULL DEFAULT 0,
    sales_date      DATE         NOT NULL,
    source_type     VARCHAR(20)  NOT NULL DEFAULT 'POS',  -- POS / B2B / ONLINE
    source_ref      VARCHAR(100),            -- reference from ACMM (e.g. invoice no)
    is_settled      BOOLEAN      NOT NULL DEFAULT FALSE,  -- TRUE after CBR released
    cbr_id          VARCHAR(64),             -- FK to customer_billing_request after settled
    created_at      TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at      TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX IF NOT EXISTS idx_consignment_unpost_store_sku ON consignment_unpost(store, sku, is_settled);
CREATE INDEX IF NOT EXISTS idx_consignment_unpost_date ON consignment_unpost(sales_date, is_settled);

-- ============================================================
-- SEEDER: Sample unpost data for testing CBR computation
-- Simulates POS Sales from ACMM for stores under STORE01 (internal supplier)
-- ============================================================
INSERT INTO consignment_unpost (store, sku, sales_qty, sales_return_qty, sales_date, source_type, source_ref, is_settled)
VALUES
    -- STORE01 consignee outlets (customers under STORE01 internal supplier)
    ('STORE01', 'ITEM001', 9.5,  0, '2026-04-05', 'POS',    'POS-INV-0001', false),
    ('STORE01', 'ITEM002', 10.0, 0, '2026-04-05', 'POS',    'POS-INV-0001', false),
    ('STORE01', 'ITEM003', 8.0,  0, '2026-04-06', 'POS',    'POS-INV-0002', false),
    ('STORE01', 'ITEM001', 8.0,  0, '2026-04-07', 'B2B',    'B2B-ORD-0001', false),
    ('STORE01', 'ITEM002', 8.0,  0, '2026-04-07', 'B2B',    'B2B-ORD-0001', false),
    ('STORE01', 'ITEM003', 8.0,  0, '2026-04-07', 'B2B',    'B2B-ORD-0001', false),
    ('STORE01', 'ITEM001', 7.0,  0, '2026-04-08', 'ONLINE', 'ONL-ORD-0001', false),
    ('STORE01', 'ITEM002', 7.0,  0, '2026-04-08', 'ONLINE', 'ONL-ORD-0001', false),
    -- STORE02 consignee outlets
    ('STORE02', 'ITEM001', 15.0, 1.0, '2026-04-05', 'POS',  'POS-INV-0003', false),
    ('STORE02', 'ITEM002', 15.0, 1.0, '2026-04-05', 'POS',  'POS-INV-0003', false),
    -- STORE03 consignee outlets
    ('STORE03', 'ITEM005', 30.0, 1.0, '2026-04-06', 'POS',  'POS-INV-0004', false),
    ('STORE03', 'ITEM006', 30.0, 1.0, '2026-04-06', 'POS',  'POS-INV-0004', false);
