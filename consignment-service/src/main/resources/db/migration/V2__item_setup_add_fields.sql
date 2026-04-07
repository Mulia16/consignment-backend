ALTER TABLE consignment_item_setup
    ADD COLUMN IF NOT EXISTS item_name        VARCHAR(200),
    ADD COLUMN IF NOT EXISTS variant          VARCHAR(100),
    ADD COLUMN IF NOT EXISTS unit_retail      NUMERIC(18,4),
    ADD COLUMN IF NOT EXISTS mvc              NUMERIC(18,4),
    ADD COLUMN IF NOT EXISTS category_l1      VARCHAR(100),
    ADD COLUMN IF NOT EXISTS category_l2      VARCHAR(100),
    ADD COLUMN IF NOT EXISTS category_l3      VARCHAR(100);
