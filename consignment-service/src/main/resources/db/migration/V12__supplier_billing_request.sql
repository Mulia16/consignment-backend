-- Supplier Consignment Billing Request Header
CREATE TABLE IF NOT EXISTS supplier_billing_request (
    id                    VARCHAR(64)  PRIMARY KEY,
    doc_no                VARCHAR(50)  NOT NULL UNIQUE,
    company               VARCHAR(100),
    period_type           VARCHAR(20)  NOT NULL,
    from_date             DATE         NOT NULL,
    to_date               DATE         NOT NULL,
    store                 VARCHAR(50),
    supplier_code         VARCHAR(50)  NOT NULL,
    supplier_contract     VARCHAR(50),
    supplier_type         VARCHAR(30),
    carry_forward_decimal BOOLEAN      NOT NULL DEFAULT TRUE,
    status                VARCHAR(20)  NOT NULL DEFAULT 'HELD',
    process_status        VARCHAR(30)  NOT NULL DEFAULT 'COMPLETED',
    error_reason          TEXT,
    process_date          TIMESTAMP,
    created_by            VARCHAR(50)  NOT NULL,
    released_at           TIMESTAMP,
    created_at            TIMESTAMP    NOT NULL DEFAULT NOW(),
    updated_at            TIMESTAMP    NOT NULL DEFAULT NOW()
);

-- Supplier Consignment Billing Request Detail (per item)
CREATE TABLE IF NOT EXISTS supplier_billing_request_detail (
    id                  VARCHAR(64)    PRIMARY KEY,
    billing_id          VARCHAR(64)    NOT NULL,
    item_code           VARCHAR(50)    NOT NULL,
    uom                 VARCHAR(20)    NOT NULL,
    sales_qty           NUMERIC(18,4)  NOT NULL DEFAULT 0,
    sales_return_qty    NUMERIC(18,4)  NOT NULL DEFAULT 0,
    bf_qty              NUMERIC(18,4)  NOT NULL DEFAULT 0,
    billing_qty         NUMERIC(18,4)  NOT NULL DEFAULT 0,
    cf_qty              NUMERIC(18,4)  NOT NULL DEFAULT 0,
    unit_cost           NUMERIC(18,4),
    total_cost          NUMERIC(18,4),
    total_supplier_qty  NUMERIC(18,4),
    CONSTRAINT fk_scbr_detail_header FOREIGN KEY (billing_id)
        REFERENCES supplier_billing_request(id) ON DELETE CASCADE
);

-- Add scbr_id column to consignment_unpost for SCBR settlement tracking
ALTER TABLE consignment_unpost
    ADD COLUMN IF NOT EXISTS scbr_id VARCHAR(64);

-- Indexes
CREATE INDEX IF NOT EXISTS idx_scbr_filter ON supplier_billing_request(store, supplier_code, status, from_date, to_date);
CREATE INDEX IF NOT EXISTS idx_scbr_detail ON supplier_billing_request_detail(billing_id);
