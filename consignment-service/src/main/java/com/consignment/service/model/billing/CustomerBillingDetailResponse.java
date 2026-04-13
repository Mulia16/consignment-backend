package com.consignment.service.model.billing;

import java.math.BigDecimal;

public record CustomerBillingDetailResponse(
        String id,
        String customerCode,
        String customerBranch,
        String itemCode,
        String uom,
        BigDecimal salesQty,
        BigDecimal returnQty,
        BigDecimal billingQty,
        BigDecimal unitPrice,
        BigDecimal lineAmount,
        BigDecimal actualReturnQty
) {}
