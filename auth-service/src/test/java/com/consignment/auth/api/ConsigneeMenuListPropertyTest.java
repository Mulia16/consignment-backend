package com.consignment.auth.api;

import com.consignment.auth.repository.TokenBlacklistRepository;
import com.consignment.auth.repository.UserProfileRepository;
import com.consignment.auth.repository.UserRepository;
import com.consignment.auth.security.JwtUtil;
import com.consignment.auth.service.MenuService;
import net.jqwik.api.*;
import net.jqwik.api.constraints.AlphaChars;
import net.jqwik.api.constraints.StringLength;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Property-based test for Consignee Menu List Is Correct.
 *
 * // Feature: consignee-role, Property 7: Consignee Menu List Is Correct
 *
 * Validates: Requirements 5.1
 */
class ConsigneeMenuListPropertyTest {

    /**
     * Property 7: Consignee Menu List Is Correct
     *
     * For any user with ROLE_CONSIGNEE, GET /auth/me/menus SHALL return a list
     * containing exactly ["PRODUCTS", "PURCHASE_ORDERS", "DASHBOARD"] — no more, no fewer.
     *
     * Validates: Requirements 5.1
     */
    @Property(tries = 100)
    void consigneeMenuListIsCorrect(
            @ForAll @AlphaChars @StringLength(min = 1, max = 50) String username) {

        // Feature: consignee-role, Property 7: Consignee Menu List Is Correct

        // --- Setup mocks ---
        AuthenticationManager authManager = mock(AuthenticationManager.class);
        UserRepository userRepository = mock(UserRepository.class);
        UserProfileRepository userProfileRepository = mock(UserProfileRepository.class);
        TokenBlacklistRepository blacklist = mock(TokenBlacklistRepository.class);
        PasswordEncoder passwordEncoder = mock(PasswordEncoder.class);

        // Mock: blacklist always returns false (token not revoked)
        when(blacklist.isBlacklisted(any())).thenReturn(false);

        // Create real JwtUtil with test secret
        JwtUtil jwtUtil = createJwtUtil();

        // Generate a valid JWT with ROLE_CONSIGNEE
        String token = jwtUtil.generateToken(username, Set.of("ROLE_CONSIGNEE"), "STORE_TEST");

        // Use real MenuService — we want to test the actual menu logic
        MenuService menuService = new MenuService();

        // Instantiate AuthController with real MenuService
        AuthController controller = new AuthController(
                authManager, userRepository, userProfileRepository,
                passwordEncoder, jwtUtil, blacklist, menuService
        );

        // --- Invoke getMenus ---
        @SuppressWarnings("unchecked")
        ResponseEntity<ApiResponse<?>> response =
                (ResponseEntity<ApiResponse<?>>) controller.getMenus("Bearer " + token);

        // --- Assert: HTTP 200 ---
        assertThat(response.getStatusCode())
                .as("getMenus for ROLE_CONSIGNEE should return HTTP 200")
                .isEqualTo(HttpStatus.OK);

        assertThat(response.getBody()).isNotNull();

        // --- Assert: data contains exactly ["PRODUCTS", "PURCHASE_ORDERS", "DASHBOARD"] ---
        @SuppressWarnings("unchecked")
        List<String> menus = (List<String>) response.getBody().data();

        assertThat(menus)
                .as("Consignee menus should contain exactly PRODUCTS, PURCHASE_ORDERS, DASHBOARD")
                .isNotNull()
                .containsExactly("PRODUCTS", "PURCHASE_ORDERS", "DASHBOARD");
    }

    // -------------------------------------------------------------------------
    // Helpers
    // -------------------------------------------------------------------------

    private JwtUtil createJwtUtil() {
        JwtUtil jwtUtil = new JwtUtil();
        ReflectionTestUtils.setField(jwtUtil, "secret",
                "test-secret-key-for-property-tests-minimum-32-chars!!");
        ReflectionTestUtils.setField(jwtUtil, "expirationMs", 86400000L);
        return jwtUtil;
    }
}
