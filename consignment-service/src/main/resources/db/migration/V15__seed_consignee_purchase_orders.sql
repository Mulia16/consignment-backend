-- V15__seed_consignee_purchase_orders.sql
-- Seed sample consignee purchase orders for dev/testing.

INSERT INTO consignee_purchase_orders (
    po_number,
    store,
    item_code,
    item_name,
    ordered_qty,
    status,
    po_date,
    synced_at
)
VALUES
    ('PO-STOREA-001', 'STORE_A', 'ITEM001', 'Laptop Pro 15', 5, 'OPEN', CURRENT_DATE - INTERVAL '3 days', CURRENT_TIMESTAMP),
    ('PO-STOREA-002', 'STORE_A', 'ITEM002', 'Laptop Air 13', 7, 'OPEN', CURRENT_DATE - INTERVAL '2 days', CURRENT_TIMESTAMP),
    ('PO-STOREA-003', 'STORE_A', 'ITEM005', 'Monitor 27 inch', 4, 'OPEN', CURRENT_DATE - INTERVAL '1 day', CURRENT_TIMESTAMP),
    ('PO-STOREA-004', 'STORE_A', 'ITEM006', 'Wireless Keyboard', 10, 'OPEN', CURRENT_DATE, CURRENT_TIMESTAMP),
    ('PO-STORE01-001', 'STORE01', 'ITEM001', 'Laptop Pro 15', 3, 'OPEN', CURRENT_DATE - INTERVAL '3 days', CURRENT_TIMESTAMP),
    ('PO-STORE02-001', 'STORE02', 'ITEM002', 'Laptop Air 13', 2, 'OPEN', CURRENT_DATE - INTERVAL '2 days', CURRENT_TIMESTAMP)
ON CONFLICT (po_number) DO NOTHING;