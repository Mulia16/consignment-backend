package com.consignment.service.model.billing;

import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;

public record CustomerBillingActualReturnRequest(
        @NotNull BigDecimal actualReturnQty
) {}
