-- CSR: add cso_doc_no column
ALTER TABLE csr_header
    ADD COLUMN IF NOT EXISTS cso_doc_no VARCHAR(50);

-- CSRN header
CREATE TABLE IF NOT EXISTS csrn_header (
    id                      VARCHAR(64) PRIMARY KEY,
    doc_no                  VARCHAR(50) NOT NULL UNIQUE,
    cso_doc_no              VARCHAR(50) NOT NULL,
    company                 VARCHAR(50) NOT NULL,
    store                   VARCHAR(50) NOT NULL,
    supplier_code           VARCHAR(50) NOT NULL,
    supplier_contract       VARCHAR(100) NOT NULL,
    internal_supplier_store VARCHAR(50),
    reason_code             VARCHAR(50),
    remark                  TEXT,
    status                  VARCHAR(20) NOT NULL DEFAULT 'HELD',
    created_by              VARCHAR(50) NOT NULL,
    reference_no            VARCHAR(100),
    updated_at              TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_at              TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- CSRN detail
CREATE TABLE IF NOT EXISTS csrn_detail (
    id          VARCHAR(64) PRIMARY KEY,
    csrn_id     VARCHAR(64) NOT NULL,
    item_code   VARCHAR(50) NOT NULL,
    uom         VARCHAR(20) NOT NULL,
    qty         NUMERIC(18,4) NOT NULL,
    CONSTRAINT fk_csrn_detail_header FOREIGN KEY (csrn_id) REFERENCES csrn_header(id) ON DELETE CASCADE
);

-- CSRN-C header (auto-created when CSRN updated)
CREATE TABLE IF NOT EXISTS csrn_c_header (
    id          VARCHAR(64) PRIMARY KEY,
    doc_no      VARCHAR(50) NOT NULL UNIQUE,
    csrn_id     VARCHAR(64) NOT NULL,
    csrn_doc_no VARCHAR(50) NOT NULL,
    cso_doc_no  VARCHAR(50) NOT NULL,
    company     VARCHAR(50) NOT NULL,
    store       VARCHAR(50) NOT NULL,
    supplier_code     VARCHAR(50) NOT NULL,
    supplier_contract VARCHAR(100) NOT NULL,
    reason_code VARCHAR(50),
    remark      TEXT,
    created_by  VARCHAR(50) NOT NULL,
    created_at  TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_csrn_c_csrn FOREIGN KEY (csrn_id) REFERENCES csrn_header(id)
);

-- CSRN-C detail (snapshot of CSRN detail at time of update)
CREATE TABLE IF NOT EXISTS csrn_c_detail (
    id          VARCHAR(64) PRIMARY KEY,
    csrn_c_id   VARCHAR(64) NOT NULL,
    item_code   VARCHAR(50) NOT NULL,
    uom         VARCHAR(20) NOT NULL,
    qty         NUMERIC(18,4) NOT NULL,
    CONSTRAINT fk_csrn_c_detail_header FOREIGN KEY (csrn_c_id) REFERENCES csrn_c_header(id) ON DELETE CASCADE
);

CREATE INDEX IF NOT EXISTS idx_csrn_header_filter ON csrn_header(company, store, cso_doc_no, status, created_at, updated_at);
CREATE INDEX IF NOT EXISTS idx_csrn_detail_header ON csrn_detail(csrn_id);
CREATE INDEX IF NOT EXISTS idx_csrn_c_header_filter ON csrn_c_header(csrn_id, cso_doc_no, created_at);
CREATE INDEX IF NOT EXISTS idx_csrn_c_detail_header ON csrn_c_detail(csrn_c_id);
