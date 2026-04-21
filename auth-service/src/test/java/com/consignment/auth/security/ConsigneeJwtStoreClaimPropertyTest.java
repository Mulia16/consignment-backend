package com.consignment.auth.security;

import io.jsonwebtoken.Claims;
import net.jqwik.api.*;
import net.jqwik.api.constraints.AlphaChars;
import net.jqwik.api.constraints.StringLength;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Property-based tests for Consignee JWT store claim correctness.
 *
 * // Feature: consignee-role, Property 2: Consignee JWT Contains Correct Store Claim
 *
 * Validates: Requirements 3.1, 3.3
 */
class ConsigneeJwtStoreClaimPropertyTest {

    private JwtUtil createJwtUtil() {
        JwtUtil jwtUtil = new JwtUtil();
        // Secret must be at least 256 bits (32 chars) for HMAC-SHA256
        ReflectionTestUtils.setField(jwtUtil, "secret",
                "test-secret-key-for-property-tests-minimum-32-chars!!");
        ReflectionTestUtils.setField(jwtUtil, "expirationMs", 86400000L);
        return jwtUtil;
    }

    /**
     * Property 2: Consignee JWT Contains Correct Store Claim
     *
     * For any user with ROLE_CONSIGNEE that has a valid entry in user_profiles,
     * the JWT generated upon login SHALL contain a store claim whose value is
     * identical to consignee_store stored in user_profiles for that user.
     *
     * Simulates the login flow: JwtUtil.generateToken(username, Set.of("ROLE_CONSIGNEE"), consigneeStore)
     * is called with the consigneeStore value from user_profiles, then the JWT is parsed back
     * and the store claim is verified to match the original consigneeStore.
     *
     * Validates: Requirements 3.1, 3.3
     */
    @Property(tries = 100)
    void consigneeJwtContainsCorrectStoreClaim(
            @ForAll @AlphaChars @StringLength(min = 1, max = 50) String username,
            @ForAll @AlphaChars @StringLength(min = 1, max = 50) String consigneeStore) {

        // Feature: consignee-role, Property 2: Consignee JWT Contains Correct Store Claim
        JwtUtil jwtUtil = createJwtUtil();

        // Simulate the login flow: user with ROLE_CONSIGNEE has a UserProfile with consigneeStore
        // AuthController calls: jwtUtil.generateToken(username, roles, store)
        // where store = userProfile.getConsigneeStore()
        String token = jwtUtil.generateToken(username, Set.of("ROLE_CONSIGNEE"), consigneeStore);

        // Parse the JWT back and verify store claim == consigneeStore
        Claims claims = jwtUtil.parseClaims(token);

        // Requirement 3.1: JWT for ROLE_CONSIGNEE SHALL contain store claim
        assertThat(claims.get("store", String.class))
                .as("JWT store claim should be present for ROLE_CONSIGNEE user")
                .isNotNull();

        // Requirement 3.3: store claim value SHALL be identical to consignee_store from user_profiles
        assertThat(claims.get("store", String.class))
                .as("JWT store claim should equal the consigneeStore from user_profiles")
                .isEqualTo(consigneeStore);
    }
}
