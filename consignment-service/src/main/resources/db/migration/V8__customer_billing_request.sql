-- Customer Consignment Billing Request Header
CREATE TABLE IF NOT EXISTS customer_billing_request (
    id              VARCHAR(64) PRIMARY KEY,
    doc_no          VARCHAR(50) NOT NULL UNIQUE,
    period_type     VARCHAR(20) NOT NULL,           -- MONTHLY / WEEKLY
    from_date       DATE NOT NULL,
    to_date         DATE NOT NULL,
    store           VARCHAR(50) NOT NULL,           -- internal supplier store
    customer_code   VARCHAR(50),                    -- null = all customers
    customer_branch VARCHAR(50),
    status          VARCHAR(20) NOT NULL DEFAULT 'HELD',   -- HELD / RELEASED
    process_status  VARCHAR(30) NOT NULL DEFAULT 'COMPLETED', -- COMPLETED / FAILED
    created_by      VARCHAR(50) NOT NULL,
    released_at     TIMESTAMP,
    created_at      TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at      TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- Customer Consignment Billing Request Detail (per customer per item)
CREATE TABLE IF NOT EXISTS customer_billing_request_detail (
    id                  VARCHAR(64) PRIMARY KEY,
    billing_id          VARCHAR(64) NOT NULL,
    customer_code       VARCHAR(50) NOT NULL,
    customer_branch     VARCHAR(50),
    item_code           VARCHAR(50) NOT NULL,
    uom                 VARCHAR(20) NOT NULL,
    sales_qty           NUMERIC(18,4) NOT NULL DEFAULT 0,
    return_qty          NUMERIC(18,4) NOT NULL DEFAULT 0,
    billing_qty         NUMERIC(18,4) NOT NULL DEFAULT 0,   -- sales_qty - return_qty
    unit_price          NUMERIC(18,4),
    line_amount         NUMERIC(18,4),
    actual_return_qty   NUMERIC(18,4),                      -- editable after release
    CONSTRAINT fk_cbr_detail_header FOREIGN KEY (billing_id)
        REFERENCES customer_billing_request(id) ON DELETE CASCADE
);

CREATE INDEX IF NOT EXISTS idx_cbr_filter ON customer_billing_request(store, customer_code, status, from_date, to_date);
CREATE INDEX IF NOT EXISTS idx_cbr_detail ON customer_billing_request_detail(billing_id);
