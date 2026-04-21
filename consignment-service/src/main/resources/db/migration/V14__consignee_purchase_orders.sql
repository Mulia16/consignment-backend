-- V14__consignee_purchase_orders.sql
-- Creates the consignee_purchase_orders table for storing POs from HCMM (stub)
-- Requirements: 9.1, 9.2, 9.3

CREATE TABLE IF NOT EXISTS consignee_purchase_orders (
    id          BIGSERIAL    PRIMARY KEY,
    po_number   VARCHAR(50)  NOT NULL UNIQUE,
    store       VARCHAR(50)  NOT NULL,
    item_code   VARCHAR(50),
    item_name   VARCHAR(200),
    ordered_qty INTEGER,
    status      VARCHAR(20),
    po_date     DATE,
    synced_at   TIMESTAMP,
    created_at  TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at  TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX IF NOT EXISTS idx_cpo_store ON consignee_purchase_orders (store);
