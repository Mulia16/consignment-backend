CREATE INDEX IF NOT EXISTS idx_external_supplier_code_contract
    ON consignment_external_supplier (supplier_code, supplier_contract);

CREATE INDEX IF NOT EXISTS idx_item_price_effective_from
    ON consignment_item_price (effective_from);

CREATE INDEX IF NOT EXISTS idx_settlement_detail_doc_type_no
    ON settlement_request_detail (document_type, document_no);
