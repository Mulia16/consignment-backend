ALTER TABLE cso_header
    ADD COLUMN IF NOT EXISTS shipping_term         VARCHAR(100),
    ADD COLUMN IF NOT EXISTS delivery_date         DATE,
    ADD COLUMN IF NOT EXISTS shipping_mode         VARCHAR(50),
    ADD COLUMN IF NOT EXISTS transporter           VARCHAR(100),
    ADD COLUMN IF NOT EXISTS shipping_to           VARCHAR(200),
    ADD COLUMN IF NOT EXISTS shipping_address      TEXT,
    ADD COLUMN IF NOT EXISTS customer_reference    VARCHAR(100),
    ADD COLUMN IF NOT EXISTS transport_information TEXT;
