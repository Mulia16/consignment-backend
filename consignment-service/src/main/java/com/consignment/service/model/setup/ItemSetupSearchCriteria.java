package com.consignment.service.model.setup;

public record ItemSetupSearchCriteria(
        String itemCode,
        String itemName,
        String variant,
        String hierarchy,
        String categoryL1,
        String categoryL2,
        int page,
        int perPage
) {
    public int offset() {
        return (page - 1) * perPage;
    }
}
