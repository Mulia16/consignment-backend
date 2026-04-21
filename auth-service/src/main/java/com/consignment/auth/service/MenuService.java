package com.consignment.auth.service;

import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Service
public class MenuService {

    // Feature: consignee-role — role-to-menus mapping (hardcoded, not stored in DB)
    private static final Map<String, List<String>> ROLE_MENUS = Map.of(
        "ROLE_CONSIGNEE", List.of("PRODUCTS", "PURCHASE_ORDERS", "DASHBOARD"),
        "ROLE_ADMIN",     List.of(
                                   "CONSIGNMENT_RECEIVING",
                                   "CONSIGNMENT_STOCK_OUT",
                                   "CONSIGNMENT_STOCK_REQUEST",
                                   "CONSIGNMENT_DELIVERY_ORDER",
                                   "CONSIGNMENT_STOCK_RETURN",
                                   "CONSIGNMENT_STOCK_RETURN_COLLECT",
                                   "CONSIGNMENT_STOCK_ADJUSTMENT",
                                   "SETTLEMENT_CUSTOMER_COMPUTE",
                                   "SETTLEMENT_CUSTOMER_BILLING",
                                   "SETTLEMENT_FAILURE_CUSTOMER",
                                   "SETTLEMENT_SUPPLIER_COMPUTE",
                                   "SETTLEMENT_SUPPLIER_BILLING",
                                   "SETTLEMENT_FAILURE_SUPPLIER",
                                   "MASTER_DATA_COMPANIES",
                                   "MASTER_DATA_STORES",
                                   "MASTER_DATA_SUPPLIERS",
                                   "MASTER_DATA_CONTRACTS",
                                   "MASTER_DATA_ITEMS",
                                   "SETUP_CONSIGNMENT_ITEMS",
                                   "REPORT_CENTER",
                                   "REPORT_CSRQ",
                                   "REPORT_CSRV",
                                   "REPORT_CSO",
                                   "REPORT_CSDO",
                                   "REPORT_CSR",
                                   "REPORT_CSA",
                                   "REPORT_SETTLEMENT_SUMMARY",
                                   "REPORT_SETTLEMENT_DETAIL",
                                   "REPORT_SUPPLIER_BOOK_VALUE",
                                   "REPORT_CUSTOMER_INVENTORY",
                                   "REPORT_RESERVATIONS",
                                   "REPORT_CONSIGNMENT_SETUP",
                                   "SYSTEM_AUDIT_LOG"
                                   ),
        "ROLE_USER",      List.of("REPORTS", "SETTLEMENTS")
    );

    /**
     * Returns the union of all menus accessible by the given set of roles.
     * If a user has multiple roles, menus from all roles are merged (no duplicates).
     *
     * @param roles set of role strings (e.g. "ROLE_CONSIGNEE", "ROLE_ADMIN")
     * @return ordered list of unique menu identifiers
     */
    public List<String> getMenusForRoles(Set<String> roles) {
        Set<String> menus = new LinkedHashSet<>();
        for (String role : roles) {
            List<String> roleMenus = ROLE_MENUS.get(role);
            if (roleMenus != null) {
                menus.addAll(roleMenus);
            }
        }
        return new ArrayList<>(menus);
    }
}
