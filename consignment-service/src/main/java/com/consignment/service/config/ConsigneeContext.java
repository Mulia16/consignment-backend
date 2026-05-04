package com.consignment.service.config;

import com.consignment.service.exception.MissingStoreClaimException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Helper component to extract JWT claims from the SecurityContextHolder.
 * The JwtAuthFilter sets authentication.setDetails(details) where details is a
 * Map<String, Object> containing "store" and "roles".
 */
@Component
public class ConsigneeContext {

    /**
     * Extracts the store claim from the current authentication details.
     *
     * @return the store string from the JWT claim
     * @throws MissingStoreClaimException if store is null or blank
     */
    public String requireStore() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null) {
            throw new MissingStoreClaimException("Store claim missing in token");
        }
        Object detailsObj = auth.getDetails();
        String store = null;
        if (detailsObj instanceof Map) {
            @SuppressWarnings("unchecked")
            Map<String, Object> details = (Map<String, Object>) detailsObj;
            store = (String) details.get("store");
        }
        if (store == null || store.isBlank()) {
            throw new MissingStoreClaimException("Store claim missing in token");
        }
        return store;
    }

    /**
     * Extracts the granted authorities from the current authentication.
     *
     * @return set of authority strings (e.g. "ROLE_CONSIGNEE")
     */
    public Set<String> getRoles() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null) {
            return Set.of();
        }
        return auth.getAuthorities().stream()
                .map(a -> a.getAuthority())
                .collect(Collectors.toSet());
    }

    public String getCurrentUsername() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || auth.getName() == null || auth.getName().isBlank()) {
            return null;
        }
        return auth.getName();
    }
}
