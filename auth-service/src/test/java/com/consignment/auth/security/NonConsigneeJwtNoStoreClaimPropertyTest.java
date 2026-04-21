package com.consignment.auth.security;

import io.jsonwebtoken.Claims;
import net.jqwik.api.*;
import net.jqwik.api.constraints.AlphaChars;
import net.jqwik.api.constraints.StringLength;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Property-based tests for non-consignee JWT store claim absence.
 *
 * // Feature: consignee-role, Property 3: Non-Consignee JWT Has No Store Claim
 *
 * Validates: Requirements 3.2
 */
class NonConsigneeJwtNoStoreClaimPropertyTest {

    private JwtUtil createJwtUtil() {
        JwtUtil jwtUtil = new JwtUtil();
        // Secret must be at least 256 bits (32 chars) for HMAC-SHA256
        ReflectionTestUtils.setField(jwtUtil, "secret",
                "test-secret-key-for-property-tests-minimum-32-chars!!");
        ReflectionTestUtils.setField(jwtUtil, "expirationMs", 86400000L);
        return jwtUtil;
    }

    /**
     * Property 3: Non-Consignee JWT Has No Store Claim
     *
     * For any user that does NOT have ROLE_CONSIGNEE, the JWT generated upon login
     * SHALL NOT contain a non-null store claim.
     *
     * Validates: Requirements 3.2
     */
    @Property(tries = 100)
    void nonConsigneeJwtHasNoStoreClaim(
            @ForAll("nonConsigneeRoles") String role,
            @ForAll @AlphaChars @StringLength(min = 1, max = 30) String username) {

        // Feature: consignee-role, Property 3: Non-Consignee JWT Has No Store Claim
        JwtUtil jwtUtil = createJwtUtil();

        String token = jwtUtil.generateToken(username, Set.of(role), null);
        Claims claims = jwtUtil.parseClaims(token);

        assertThat(claims.get("store", String.class)).isNull();
    }

    @Provide
    Arbitrary<String> nonConsigneeRoles() {
        return Arbitraries.of("ROLE_ADMIN", "ROLE_USER");
    }
}
