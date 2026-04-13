-- Drop old CSRN-C (was child of old CSRN, will be recreated as child of new CSRN)
DROP TABLE IF EXISTS csrn_c_detail CASCADE;
DROP TABLE IF EXISTS csrn_c_header CASCADE;

-- Drop old CSRN tables (the one that was auto-created from CSR update)
DROP TABLE IF EXISTS csrn_detail CASCADE;
DROP TABLE IF EXISTS csrn_header CASCADE;

-- Rename CSR tables to CSRN (CSR is the real CSRN per requirement)
ALTER TABLE csr_header RENAME TO csrn_header;
ALTER TABLE csr_detail RENAME TO csrn_detail;

-- Rename FK and constraint columns in csrn_detail
ALTER TABLE csrn_detail RENAME COLUMN csr_id TO csrn_id;

-- Rename doc_no prefix will be handled at application level (new docs get CSRN- prefix)
-- Existing docs keep their CSR- prefix for historical data

-- Rename indexes
ALTER INDEX IF EXISTS idx_csr_header_filter RENAME TO idx_csrn_header_filter;
ALTER INDEX IF EXISTS idx_csr_detail_header RENAME TO idx_csrn_detail_header;

-- Recreate CSRN-C tables (now child of new csrn_header)
CREATE TABLE IF NOT EXISTS csrn_c_header (
    id                      VARCHAR(64) PRIMARY KEY,
    doc_no                  VARCHAR(50) NOT NULL UNIQUE,
    csrn_id                 VARCHAR(64) NOT NULL,
    csrn_doc_no             VARCHAR(50) NOT NULL,
    cso_doc_no              VARCHAR(50),
    company                 VARCHAR(50) NOT NULL,
    store                   VARCHAR(50) NOT NULL,
    supplier_code           VARCHAR(50) NOT NULL,
    supplier_contract       VARCHAR(100) NOT NULL,
    internal_supplier_store VARCHAR(50),
    reason_code             VARCHAR(50),
    remark                  TEXT,
    created_by              VARCHAR(50) NOT NULL,
    status                  VARCHAR(20) NOT NULL DEFAULT 'HELD',
    created_at              TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at              TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_csrn_c_csrn FOREIGN KEY (csrn_id) REFERENCES csrn_header(id)
);

CREATE TABLE IF NOT EXISTS csrn_c_detail (
    id          VARCHAR(64) PRIMARY KEY,
    csrn_c_id   VARCHAR(64) NOT NULL,
    item_code   VARCHAR(50) NOT NULL,
    uom         VARCHAR(20) NOT NULL,
    qty         NUMERIC(18,4) NOT NULL,
    actual_qty  NUMERIC(18,4),
    CONSTRAINT fk_csrn_c_detail_header FOREIGN KEY (csrn_c_id) REFERENCES csrn_c_header(id) ON DELETE CASCADE
);

CREATE INDEX IF NOT EXISTS idx_csrn_c_header_filter ON csrn_c_header(csrn_id, company, store, status, created_at);
CREATE INDEX IF NOT EXISTS idx_csrn_c_detail_header ON csrn_c_detail(csrn_c_id);
