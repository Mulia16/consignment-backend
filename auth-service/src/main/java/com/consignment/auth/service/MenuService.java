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
        "ROLE_ADMIN",     List.of("PRODUCTS", "PURCHASE_ORDERS", "DASHBOARD",
                                   "REPORTS", "SETTLEMENTS", "SETUP", "USERS"),
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
