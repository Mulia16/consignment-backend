package com.consignment.service.model.csrn;

import java.time.Instant;
import java.util.List;

public record CsrnCResponse(
        String id,
        String docNo,
        String csrnId,
        String csrnDocNo,
        String csoDocNo,
        String company,
        String store,
        String supplierCode,
        String supplierContract,
        String reasonCode,
        String remark,
        String createdBy,
        Instant createdAt,
        List<CsrnResponseDetail> items
) {
}
