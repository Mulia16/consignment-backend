package com.consignment.service.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@ConfigurationProperties(prefix = "app.settlement.batch")
public class SettlementBatchProperties {

    private boolean enabled = false;
    private String timezone = "UTC";
    private String weeklyCron = "0 0 2 * * MON";
    private String monthlyCron = "0 0 3 1 * *";
    private List<Profile> profiles = new ArrayList<>();

    public boolean isEnabled() { return enabled; }
    public void setEnabled(boolean enabled) { this.enabled = enabled; }
    public String getTimezone() { return timezone; }
    public void setTimezone(String timezone) { this.timezone = timezone; }
    public String getWeeklyCron() { return weeklyCron; }
    public void setWeeklyCron(String weeklyCron) { this.weeklyCron = weeklyCron; }
    public String getMonthlyCron() { return monthlyCron; }
    public void setMonthlyCron(String monthlyCron) { this.monthlyCron = monthlyCron; }
    public List<Profile> getProfiles() { return profiles; }
    public void setProfiles(List<Profile> profiles) { this.profiles = profiles; }

    public static class Profile {
        private String name;
        private String company;
        private String store;
        private String settlementType;
        private String customerCode;
        private String supplierCode;
        private String supplierContract;
        private String currency = "IDR";
        private String createdBy = "batch-settlement";
        private BigDecimal defaultUnitPrice = BigDecimal.ZERO;

        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
        public String getCompany() { return company; }
        public void setCompany(String company) { this.company = company; }
        public String getStore() { return store; }
        public void setStore(String store) { this.store = store; }
        public String getSettlementType() { return settlementType; }
        public void setSettlementType(String settlementType) { this.settlementType = settlementType; }
        public String getCustomerCode() { return customerCode; }
        public void setCustomerCode(String customerCode) { this.customerCode = customerCode; }
        public String getSupplierCode() { return supplierCode; }
        public void setSupplierCode(String supplierCode) { this.supplierCode = supplierCode; }
        public String getSupplierContract() { return supplierContract; }
        public void setSupplierContract(String supplierContract) { this.supplierContract = supplierContract; }
        public String getCurrency() { return currency; }
        public void setCurrency(String currency) { this.currency = currency; }
        public String getCreatedBy() { return createdBy; }
        public void setCreatedBy(String createdBy) { this.createdBy = createdBy; }
        public BigDecimal getDefaultUnitPrice() { return defaultUnitPrice; }
        public void setDefaultUnitPrice(BigDecimal defaultUnitPrice) { this.defaultUnitPrice = defaultUnitPrice; }
    }
}