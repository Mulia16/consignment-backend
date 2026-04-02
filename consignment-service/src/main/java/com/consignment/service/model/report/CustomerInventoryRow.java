package com.consignment.service.model.report;

import java.math.BigDecimal;

public record CustomerInventoryRow(
        String issueFromStore,
        String customerCode,
        String branchCode,
        String itemCode,
        BigDecimal qty
) {}
