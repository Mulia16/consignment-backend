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
        @SuppressWarnings("unchecked")
        Map<String, Object> details = (Map<String, Object>) auth.getDetails();
        String store = (String) details.get("store");
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
        return auth.getAuthorities().stream()
                .map(a -> a.getAuthority())
                .collect(Collectors.toSet());
    }
}
