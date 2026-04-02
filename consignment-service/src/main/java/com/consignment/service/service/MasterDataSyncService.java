package com.consignment.service.service;

import com.consignment.service.model.master.MasterSyncRecordRequest;
import com.consignment.service.model.master.MasterSyncResponse;
import com.consignment.service.persistence.mapper.ItemPriceMapper;
import com.consignment.service.persistence.mapper.ItemSetupMapper;
import com.consignment.service.persistence.model.ItemPriceEntity;
import com.consignment.service.persistence.model.ItemSetupEntity;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class MasterDataSyncService {

    private final ItemSetupMapper itemSetupMapper;
    private final ItemPriceMapper itemPriceMapper;
    private final Map<String, Map<String, Map<String, Object>>> storage = new ConcurrentHashMap<>();

    public MasterDataSyncService(ItemSetupMapper itemSetupMapper, ItemPriceMapper itemPriceMapper) {
        this.itemSetupMapper = itemSetupMapper;
        this.itemPriceMapper = itemPriceMapper;
    }

    public MasterSyncResponse sync(String entity, java.util.List<MasterSyncRecordRequest> records) {
        Map<String, Map<String, Object>> entityStore = storage.computeIfAbsent(entity, key -> new ConcurrentHashMap<>());
        for (MasterSyncRecordRequest record : records) {
            Map<String, Object> payload = new HashMap<>();
            if (record.attributes() != null) {
                payload.putAll(record.attributes());
            }
            payload.put("code", record.code());
            payload.put("syncedAt", Instant.now().toString());
            entityStore.put(record.code(), payload);

            if ("items".equalsIgnoreCase(entity)) {
                ItemSetupEntity itemSetupEntity = new ItemSetupEntity();
                itemSetupEntity.setItemCode(record.code());
                itemSetupEntity.setHierarchy(readStringAttribute(record, "hierarchy", "CONSIGNMENT"));
                itemSetupEntity.setItemModel(readStringAttribute(record, "itemModel", null));
                itemSetupEntity.setSyncFlag(true);
                itemSetupEntity.setDeletedFlag(false);
                itemSetupMapper.upsert(itemSetupEntity);
            } else if ("item-prices".equalsIgnoreCase(entity)) {
                ItemPriceEntity itemPrice = new ItemPriceEntity();
                itemPrice.setId(UUID.randomUUID().toString());
                itemPrice.setItemCode(record.code());
                itemPrice.setCompany(normalizeScope(readStringAttribute(record, "company", null)));
                itemPrice.setStore(normalizeScope(readStringAttribute(record, "store", null)));
                itemPrice.setSupplierCode(normalizeScope(readStringAttribute(record, "supplierCode", null)));
                itemPrice.setSupplierContract(normalizeScope(readStringAttribute(record, "supplierContract", null)));
                itemPrice.setCustomerCode(normalizeScope(readStringAttribute(record, "customerCode", null)));
                itemPrice.setCurrency(readStringAttribute(record, "currency", "IDR"));
                itemPrice.setUnitPrice(readBigDecimalAttribute(record, "unitPrice", BigDecimal.ZERO));
                itemPrice.setEffectiveFrom(readLocalDateAttribute(record, "effectiveFrom", LocalDate.now()));
                itemPriceMapper.upsert(itemPrice);
            }
        }

        return new MasterSyncResponse(entity, records.size(), Instant.now());
    }

    private String readStringAttribute(MasterSyncRecordRequest record, String attributeName, String defaultValue) {
        if (record.attributes() == null) {
            return defaultValue;
        }
        Object value = record.attributes().get(attributeName);
        return value == null ? defaultValue : String.valueOf(value);
    }

    private BigDecimal readBigDecimalAttribute(MasterSyncRecordRequest record, String attributeName, BigDecimal defaultValue) {
        if (record.attributes() == null) {
            return defaultValue;
        }
        Object value = record.attributes().get(attributeName);
        if (value == null) {
            return defaultValue;
        }
        try {
            return new BigDecimal(String.valueOf(value));
        } catch (NumberFormatException ex) {
            return defaultValue;
        }
    }

    private LocalDate readLocalDateAttribute(MasterSyncRecordRequest record, String attributeName, LocalDate defaultValue) {
        String value = readStringAttribute(record, attributeName, null);
        if (value == null || value.isBlank()) {
            return defaultValue;
        }
        try {
            return LocalDate.parse(value);
        } catch (Exception ex) {
            return defaultValue;
        }
    }

    private String normalizeScope(String value) {
        return value == null || value.isBlank() ? "" : value;
    }
}
