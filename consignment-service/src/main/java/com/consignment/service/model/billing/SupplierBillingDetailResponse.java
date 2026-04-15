package com.consignment.service.model.billing;

import java.math.BigDecimal;

public record SupplierBillingDetailResponse(
        String id,
        String itemCode,
        String uom,
        BigDecimal salesQty,
        BigDecimal salesReturnQty,
        BigDecimal bfQty,
        BigDecimal billingQty,
        BigDecimal cfQty,
        BigDecimal unitCost,
        BigDecimal totalCost,
        BigDecimal totalSupplierQty
) {}
