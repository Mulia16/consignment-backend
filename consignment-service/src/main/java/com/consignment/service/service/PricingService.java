package com.consignment.service.service;

import com.consignment.service.persistence.mapper.ItemPriceMapper;
import com.consignment.service.persistence.model.ItemPriceEntity;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

@Service
public class PricingService {

    private final ItemPriceMapper itemPriceMapper;

    public PricingService(ItemPriceMapper itemPriceMapper) {
        this.itemPriceMapper = itemPriceMapper;
    }

    public BigDecimal resolveUnitPrice(
            String itemCode,
            String company,
            String store,
            String supplierCode,
            String supplierContract,
            String customerCode
    ) {
        ItemPriceEntity price = itemPriceMapper.findEffectivePrice(
                itemCode,
                normalize(company),
                normalize(store),
                normalize(supplierCode),
                normalize(supplierContract),
                normalize(customerCode)
        );
        return price == null || price.getUnitPrice() == null ? BigDecimal.ZERO : price.getUnitPrice();
    }

    private String normalize(String value) {
        return value == null || value.isBlank() ? "" : value;
    }
}