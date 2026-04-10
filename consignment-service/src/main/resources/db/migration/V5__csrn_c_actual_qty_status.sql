-- Add status and updated_at to CSRN-C header
ALTER TABLE csrn_c_header
    ADD COLUMN IF NOT EXISTS internal_supplier_store VARCHAR(50),
    ADD COLUMN IF NOT EXISTS status     VARCHAR(20) NOT NULL DEFAULT 'HELD',
    ADD COLUMN IF NOT EXISTS updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP;

-- Add actual_qty to CSRN-C detail
ALTER TABLE csrn_c_detail
    ADD COLUMN IF NOT EXISTS actual_qty NUMERIC(18,4);
