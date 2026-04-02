package com.consignment.service.model.report;

import java.math.BigDecimal;

public record StockSummaryRow(
        String store,
        String supplierCode,
        String supplierContract,
        String itemCode,
        BigDecimal purchaseQty,
        BigDecimal closingQty,
        BigDecimal unbillQty
) {}
