CREATE TABLE IF NOT EXISTS consignment_item_setup (
    id BIGSERIAL PRIMARY KEY,
    item_code VARCHAR(50) NOT NULL UNIQUE,
    hierarchy VARCHAR(30) NOT NULL,
    item_model VARCHAR(100),
    sync_flag BOOLEAN NOT NULL DEFAULT TRUE,
    deleted_flag BOOLEAN NOT NULL DEFAULT FALSE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS consignment_item_price (
    id VARCHAR(64) PRIMARY KEY,
    item_code VARCHAR(50) NOT NULL,
    company VARCHAR(50) NOT NULL DEFAULT '',
    store VARCHAR(50) NOT NULL DEFAULT '',
    supplier_code VARCHAR(50) NOT NULL DEFAULT '',
    supplier_contract VARCHAR(100) NOT NULL DEFAULT '',
    customer_code VARCHAR(50) NOT NULL DEFAULT '',
    unit_price NUMERIC(18,4) NOT NULL,
    currency VARCHAR(10) NOT NULL DEFAULT 'IDR',
    effective_from DATE NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS consignment_external_supplier (
    id VARCHAR(64) PRIMARY KEY,
    item_code VARCHAR(50) NOT NULL,
    supplier_code VARCHAR(50) NOT NULL,
    supplier_type VARCHAR(30) NOT NULL,
    supplier_contract VARCHAR(100) NOT NULL,
    consignee_company VARCHAR(50) NOT NULL,
    consignee_store VARCHAR(50) NOT NULL,
    current_inventory_qty INTEGER NOT NULL DEFAULT 0,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_external_item_setup FOREIGN KEY (item_code) REFERENCES consignment_item_setup(item_code)
);

CREATE TABLE IF NOT EXISTS consignment_internal_supplier (
    id VARCHAR(64) PRIMARY KEY,
    item_code VARCHAR(50) NOT NULL,
    supplier_code VARCHAR(50) NOT NULL,
    supplier_store VARCHAR(50) NOT NULL,
    consignee_company VARCHAR(50) NOT NULL,
    consignee_store VARCHAR(50) NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_internal_item_setup FOREIGN KEY (item_code) REFERENCES consignment_item_setup(item_code)
);

CREATE TABLE IF NOT EXISTS supplier_book_value_inventory (
    id BIGSERIAL PRIMARY KEY,
    store VARCHAR(50) NOT NULL,
    sku VARCHAR(50) NOT NULL,
    supplier_code VARCHAR(50),
    supplier_contract VARCHAR(100),
    purchase_qty NUMERIC(18, 4) NOT NULL DEFAULT 0,
    closing_qty NUMERIC(18, 4) NOT NULL DEFAULT 0,
    unbill_qty NUMERIC(18, 4) NOT NULL DEFAULT 0,
    period_date DATE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uq_supplier_bv_inventory UNIQUE (store, sku, supplier_code, supplier_contract)
);

CREATE TABLE IF NOT EXISTS customer_consignment_inventory (
    id BIGSERIAL PRIMARY KEY,
    issue_from_store VARCHAR(50) NOT NULL,
    customer_code VARCHAR(50),
    branch_code VARCHAR(50),
    sku VARCHAR(50) NOT NULL,
    qty NUMERIC(18, 4) NOT NULL DEFAULT 0,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uq_customer_consignment_inventory UNIQUE (issue_from_store, customer_code, branch_code, sku)
);

CREATE TABLE IF NOT EXISTS consignment_reservation (
    id BIGSERIAL PRIMARY KEY,
    doc_no VARCHAR(50) NOT NULL,
    doc_type VARCHAR(50),
    store VARCHAR(50) NOT NULL,
    sku VARCHAR(50) NOT NULL,
    qty NUMERIC(18, 4) NOT NULL DEFAULT 0,
    reservation_type VARCHAR(20) NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS unpost_staging_inventory (
    id BIGSERIAL PRIMARY KEY,
    sku VARCHAR(50) NOT NULL,
    location VARCHAR(50) NOT NULL,
    sales_qty NUMERIC(18, 4) NOT NULL DEFAULT 0,
    return_qty NUMERIC(18, 4) NOT NULL DEFAULT 0,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uq_unpost_staging_inventory UNIQUE (sku, location)
);

CREATE TABLE IF NOT EXISTS csrq_header (
    id VARCHAR(64) PRIMARY KEY,
    doc_no VARCHAR(50) NOT NULL UNIQUE,
    company VARCHAR(50) NOT NULL,
    store VARCHAR(50) NOT NULL,
    supplier_code VARCHAR(50) NOT NULL,
    supplier_contract VARCHAR(100) NOT NULL,
    branch VARCHAR(50),
    internal_supplier_store VARCHAR(50),
    notes TEXT,
    status VARCHAR(20) NOT NULL,
    created_by VARCHAR(50) NOT NULL,
    created_method VARCHAR(20) NOT NULL,
    reference_no VARCHAR(100),
    released_at TIMESTAMP,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS csrq_detail (
    id VARCHAR(64) PRIMARY KEY,
    csrq_id VARCHAR(64) NOT NULL,
    item_code VARCHAR(50) NOT NULL,
    request_qty NUMERIC(18, 4) NOT NULL,
    request_uom VARCHAR(20) NOT NULL,
    CONSTRAINT fk_csrq_detail_header FOREIGN KEY (csrq_id) REFERENCES csrq_header(id) ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS csrv_header (
    id VARCHAR(64) PRIMARY KEY,
    doc_no VARCHAR(50) NOT NULL UNIQUE,
    company VARCHAR(50) NOT NULL,
    receiving_store VARCHAR(50) NOT NULL,
    supplier_code VARCHAR(50) NOT NULL,
    supplier_contract VARCHAR(100) NOT NULL,
    branch VARCHAR(50),
    supplier_do_no VARCHAR(100),
    delivery_date DATE,
    remark TEXT,
    status VARCHAR(20) NOT NULL,
    created_by VARCHAR(50) NOT NULL,
    created_method VARCHAR(20) NOT NULL,
    reference_no VARCHAR(100),
    released_at TIMESTAMP,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS csrv_detail (
    id VARCHAR(64) PRIMARY KEY,
    csrv_id VARCHAR(64) NOT NULL,
    item_code VARCHAR(50) NOT NULL,
    available_qty NUMERIC(18, 4) NOT NULL DEFAULT 0,
    request_qty NUMERIC(18, 4) NOT NULL,
    receiving_qty NUMERIC(18, 4) NOT NULL,
    CONSTRAINT fk_csrv_detail_header FOREIGN KEY (csrv_id) REFERENCES csrv_header(id) ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS cso_header (
    id VARCHAR(64) PRIMARY KEY,
    doc_no VARCHAR(50) NOT NULL UNIQUE,
    company VARCHAR(50) NOT NULL,
    store VARCHAR(50) NOT NULL,
    customer_code VARCHAR(50) NOT NULL,
    customer_branch VARCHAR(50),
    customer_email VARCHAR(150),
    supplier_code VARCHAR(50) NOT NULL,
    supplier_contract VARCHAR(100) NOT NULL,
    auto_generate_csdo BOOLEAN NOT NULL DEFAULT FALSE,
    note TEXT,
    status VARCHAR(20) NOT NULL,
    created_by VARCHAR(50) NOT NULL,
    created_method VARCHAR(20) NOT NULL,
    reference_no VARCHAR(100),
    released_at TIMESTAMP,
    released_by VARCHAR(50),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS cso_detail (
    id VARCHAR(64) PRIMARY KEY,
    cso_id VARCHAR(64) NOT NULL,
    item_code VARCHAR(50) NOT NULL,
    qty NUMERIC(18, 4) NOT NULL,
    uom VARCHAR(20) NOT NULL,
    CONSTRAINT fk_cso_detail_header FOREIGN KEY (cso_id) REFERENCES cso_header(id) ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS csdo_header (
    id VARCHAR(64) PRIMARY KEY,
    doc_no VARCHAR(50) NOT NULL UNIQUE,
    cso_id VARCHAR(64) NOT NULL,
    cso_doc_no VARCHAR(50) NOT NULL,
    company VARCHAR(50) NOT NULL,
    store VARCHAR(50) NOT NULL,
    customer_code VARCHAR(50) NOT NULL,
    customer_branch VARCHAR(50),
    customer_email VARCHAR(150),
    require_generate_cdo BOOLEAN NOT NULL DEFAULT FALSE,
    shipping_mode VARCHAR(50),
    transporter VARCHAR(100),
    status VARCHAR(20) NOT NULL,
    created_by VARCHAR(50) NOT NULL,
    released_at TIMESTAMP,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_csdo_cso FOREIGN KEY (cso_id) REFERENCES cso_header(id)
);

CREATE TABLE IF NOT EXISTS csdo_detail (
    id VARCHAR(64) PRIMARY KEY,
    csdo_id VARCHAR(64) NOT NULL,
    item_code VARCHAR(50) NOT NULL,
    qty NUMERIC(18, 4) NOT NULL,
    uom VARCHAR(20) NOT NULL,
    CONSTRAINT fk_csdo_detail_header FOREIGN KEY (csdo_id) REFERENCES csdo_header(id) ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS csr_header (
    id VARCHAR(64) PRIMARY KEY,
    doc_no VARCHAR(50) NOT NULL UNIQUE,
    company VARCHAR(50) NOT NULL,
    store VARCHAR(50) NOT NULL,
    supplier_code VARCHAR(50) NOT NULL,
    supplier_contract VARCHAR(100) NOT NULL,
    internal_supplier_store VARCHAR(50),
    supplier_confirm_note VARCHAR(100),
    reason_code VARCHAR(50),
    remark TEXT,
    status VARCHAR(20) NOT NULL,
    created_by VARCHAR(50) NOT NULL,
    reference_no VARCHAR(100),
    released_at TIMESTAMP,
    completed_at TIMESTAMP,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS csr_detail (
    id VARCHAR(64) PRIMARY KEY,
    csr_id VARCHAR(64) NOT NULL,
    item_code VARCHAR(50) NOT NULL,
    uom VARCHAR(20) NOT NULL,
    qty NUMERIC(18, 4) NOT NULL,
    actual_qty NUMERIC(18, 4),
    CONSTRAINT fk_csr_detail_header FOREIGN KEY (csr_id) REFERENCES csr_header(id) ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS csa_header (
    id VARCHAR(64) PRIMARY KEY,
    doc_no VARCHAR(50) NOT NULL UNIQUE,
    company VARCHAR(50) NOT NULL,
    store VARCHAR(50) NOT NULL,
    supplier_code VARCHAR(50),
    supplier_contract VARCHAR(100),
    transaction_type VARCHAR(20) NOT NULL,
    reference_no VARCHAR(100),
    reason_code VARCHAR(50),
    remark TEXT,
    create_from VARCHAR(50),
    status VARCHAR(20) NOT NULL,
    created_by VARCHAR(50) NOT NULL,
    released_at TIMESTAMP,
    released_by VARCHAR(50),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS csa_detail (
    id VARCHAR(64) PRIMARY KEY,
    csa_id VARCHAR(64) NOT NULL,
    item_code VARCHAR(50) NOT NULL,
    item_name VARCHAR(150),
    qty NUMERIC(18, 4) NOT NULL,
    uom VARCHAR(20) NOT NULL,
    settlement_decision VARCHAR(50) NOT NULL,
    CONSTRAINT fk_csa_detail_header FOREIGN KEY (csa_id) REFERENCES csa_header(id) ON DELETE CASCADE
);

CREATE INDEX IF NOT EXISTS idx_item_setup_item_code ON consignment_item_setup(item_code);
CREATE UNIQUE INDEX IF NOT EXISTS ux_item_price_scope
    ON consignment_item_price(item_code, company, store, supplier_code, supplier_contract, customer_code, effective_from);
CREATE INDEX IF NOT EXISTS idx_item_price_lookup
    ON consignment_item_price(item_code, company, store, supplier_code, supplier_contract, customer_code, effective_from);
CREATE INDEX IF NOT EXISTS idx_external_supplier_item_code ON consignment_external_supplier(item_code);
CREATE INDEX IF NOT EXISTS idx_internal_supplier_item_code ON consignment_internal_supplier(item_code);
CREATE INDEX IF NOT EXISTS idx_supplier_bv_lookup ON supplier_book_value_inventory(store, sku);
CREATE INDEX IF NOT EXISTS idx_customer_consignment_lookup ON customer_consignment_inventory(issue_from_store, sku);
CREATE INDEX IF NOT EXISTS idx_reservation_lookup ON consignment_reservation(store, sku);
CREATE INDEX IF NOT EXISTS idx_unpost_lookup ON unpost_staging_inventory(location, sku);
CREATE INDEX IF NOT EXISTS idx_csrq_header_filter ON csrq_header(company, store, supplier_code, supplier_contract, status, created_at);
CREATE INDEX IF NOT EXISTS idx_csrq_detail_header ON csrq_detail(csrq_id);
CREATE INDEX IF NOT EXISTS idx_csrv_header_filter ON csrv_header(company, receiving_store, supplier_code, supplier_contract, status, created_at);
CREATE INDEX IF NOT EXISTS idx_csrv_detail_header ON csrv_detail(csrv_id);
CREATE INDEX IF NOT EXISTS idx_cso_header_filter ON cso_header(company, store, customer_code, supplier_code, supplier_contract, status, created_at);
CREATE INDEX IF NOT EXISTS idx_cso_detail_header ON cso_detail(cso_id);
CREATE INDEX IF NOT EXISTS idx_csdo_header_filter ON csdo_header(company, store, customer_code, status, created_at);
CREATE INDEX IF NOT EXISTS idx_csdo_detail_header ON csdo_detail(csdo_id);
CREATE INDEX IF NOT EXISTS idx_csr_header_filter ON csr_header(company, store, supplier_code, supplier_contract, status, created_at);
CREATE INDEX IF NOT EXISTS idx_csr_detail_header ON csr_detail(csr_id);
CREATE INDEX IF NOT EXISTS idx_csa_header_filter ON csa_header(company, store, transaction_type, status, created_at);
CREATE INDEX IF NOT EXISTS idx_csa_detail_header ON csa_detail(csa_id);

-- Settlement Request Tables (Sprint 4)
CREATE TABLE IF NOT EXISTS settlement_request_header (
    id VARCHAR(64) PRIMARY KEY,
    doc_no VARCHAR(50) NOT NULL UNIQUE,
    company VARCHAR(50) NOT NULL,
    store VARCHAR(50) NOT NULL,
    settlement_type VARCHAR(30) NOT NULL,  -- CUSTOMER, SUPPLIER
    customer_code VARCHAR(50),
    supplier_code VARCHAR(50),
    supplier_contract VARCHAR(100),
    total_amount NUMERIC(18, 4) NOT NULL DEFAULT 0,
    currency VARCHAR(10) NOT NULL DEFAULT 'IDR',
    status VARCHAR(30) NOT NULL,  -- HELD, READY_FOR_BILLING, BILLED, SETTLED
    created_by VARCHAR(100) NOT NULL,
    reference_no VARCHAR(100),
    remark TEXT,
    ready_for_billing_at TIMESTAMP,
    billed_at TIMESTAMP,
    settled_at TIMESTAMP,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS settlement_request_detail (
    id VARCHAR(64) PRIMARY KEY,
    settlement_id VARCHAR(64) NOT NULL,
    document_type VARCHAR(30) NOT NULL,  -- CSO, CSRV, CSR, CSDO
    document_no VARCHAR(50) NOT NULL,
    item_code VARCHAR(50) NOT NULL,
    qty NUMERIC(18, 4) NOT NULL,
    uom VARCHAR(20) NOT NULL,
    unit_price NUMERIC(18, 4),
    line_amount NUMERIC(18, 4) NOT NULL DEFAULT 0,
    remark TEXT,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_settlement_detail_header FOREIGN KEY (settlement_id) REFERENCES settlement_request_header(id)
);

CREATE INDEX IF NOT EXISTS idx_settlement_header_filter ON settlement_request_header(company, store, settlement_type, status, created_at);
CREATE INDEX IF NOT EXISTS idx_settlement_detail_header ON settlement_request_detail(settlement_id);

