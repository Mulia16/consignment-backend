-- Add company field to customer_billing_request
ALTER TABLE customer_billing_request
    ADD COLUMN IF NOT EXISTS company      VARCHAR(100),
    ADD COLUMN IF NOT EXISTS error_reason TEXT,
    ADD COLUMN IF NOT EXISTS process_date TIMESTAMP;

CREATE INDEX IF NOT EXISTS idx_cbr_company ON customer_billing_request(company);
