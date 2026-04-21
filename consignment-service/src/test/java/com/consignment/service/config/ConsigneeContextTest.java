package com.consignment.service.config;

import com.consignment.service.exception.MissingStoreClaimException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ConsigneeContextTest {

    private final ConsigneeContext context = new ConsigneeContext();

    @AfterEach
    void clearSecurityContext() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void requireStoreThrowsWhenAuthenticationIsMissing() {
        SecurityContextHolder.clearContext();

        assertThatThrownBy(context::requireStore)
                .isInstanceOf(MissingStoreClaimException.class)
                .hasMessage("Store claim missing in token");
    }

    @Test
    void getRolesReturnsEmptyWhenAuthenticationIsMissing() {
        SecurityContextHolder.clearContext();

        assertThat(context.getRoles()).isEqualTo(Set.of());
    }

    @Test
    void requireStoreReturnsStoreFromAuthenticationDetails() {
        UsernamePasswordAuthenticationToken authentication =
                new UsernamePasswordAuthenticationToken(
                        "consignee-user",
                        null,
                        List.of(new SimpleGrantedAuthority("ROLE_CONSIGNEE"))
                );
        authentication.setDetails(Map.of("store", "STORE_A", "roles", List.of("ROLE_CONSIGNEE")));
        SecurityContextHolder.getContext().setAuthentication(authentication);

        assertThat(context.requireStore()).isEqualTo("STORE_A");
    }
}
