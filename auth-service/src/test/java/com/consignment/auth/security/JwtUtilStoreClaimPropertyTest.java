package com.consignment.auth.security;

import io.jsonwebtoken.Claims;
import net.jqwik.api.*;
import net.jqwik.api.constraints.AlphaChars;
import net.jqwik.api.constraints.StringLength;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Property-based tests for JwtUtil store claim handling.
 *
 * // Feature: consignee-role, Property 1: JWT Store Claim Round-Trip
 *
 * Validates: Requirements 3.4
 */
class JwtUtilStoreClaimPropertyTest {

    private JwtUtil createJwtUtil() {
        JwtUtil jwtUtil = new JwtUtil();
        // Secret must be at least 256 bits (32 chars) for HMAC-SHA256
        ReflectionTestUtils.setField(jwtUtil, "secret",
                "test-secret-key-for-property-tests-minimum-32-chars!!");
        ReflectionTestUtils.setField(jwtUtil, "expirationMs", 86400000L);
        return jwtUtil;
    }

    /**
     * Property 1: JWT Store Claim Round-Trip
     *
     * For any valid store string, encoding it as a JWT claim and then decoding
     * the resulting token SHALL produce a value identical to the original store string.
     *
     * Validates: Requirements 3.4
     */
    @Property(tries = 100)
    void storeClaimRoundTrip(
            @ForAll @AlphaChars @StringLength(min = 1, max = 50) String store) {

        // Feature: consignee-role, Property 1: JWT Store Claim Round-Trip
        JwtUtil jwtUtil = createJwtUtil();

        String token = jwtUtil.generateToken("testuser", Set.of("ROLE_CONSIGNEE"), store);
        Claims claims = jwtUtil.parseClaims(token);

        assertThat(claims.get("store", String.class)).isEqualTo(store);
    }
}
