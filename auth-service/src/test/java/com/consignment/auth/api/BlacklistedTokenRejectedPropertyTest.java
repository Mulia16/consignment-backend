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

import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Property-based test for Blacklisted Token Rejected on /auth/me.
 *
 * // Feature: consignee-role, Property 6: Blacklisted Token Rejected
 *
 * Validates: Requirements 4.2
 */
class BlacklistedTokenRejectedPropertyTest {

    /**
     * Property 6: Blacklisted Token Rejected on /auth/me
     *
     * For any token that has been blacklisted (via logout), a subsequent
     * GET /auth/me request using that token SHALL return HTTP 401 with
     * message "Token has been revoked".
     *
     * **Validates: Requirements 4.2**
     */
    @Property(tries = 100)
    void blacklistedTokenRejectedOnGetMe(
            @ForAll @AlphaChars @StringLength(min = 1, max = 50) String username) {

        // Feature: consignee-role, Property 6: Blacklisted Token Rejected

        // --- Setup mocks ---
        AuthenticationManager authManager = mock(AuthenticationManager.class);
        UserRepository userRepository = mock(UserRepository.class);
        UserProfileRepository userProfileRepository = mock(UserProfileRepository.class);
        TokenBlacklistRepository blacklist = mock(TokenBlacklistRepository.class);
        PasswordEncoder passwordEncoder = mock(PasswordEncoder.class);
        MenuService menuService = mock(MenuService.class);

        JwtUtil jwtUtil = createJwtUtil();

        // Generate a valid JWT token for the given username
        String token = jwtUtil.generateToken(username, Set.of("ROLE_USER"), null);

        // Mock: blacklist always returns true (token is blacklisted)
        when(blacklist.isBlacklisted(any())).thenReturn(true);

        // Instantiate AuthController directly (unit test, no Spring context)
        AuthController controller = new AuthController(
                authManager, userRepository, userProfileRepository,
                passwordEncoder, jwtUtil, blacklist, menuService
        );

        // --- Invoke GET /auth/me with the blacklisted token ---
        @SuppressWarnings("unchecked")
        ResponseEntity<ApiResponse<?>> response =
                (ResponseEntity<ApiResponse<?>>) controller.getMe("Bearer " + token);

        // --- Assert: HTTP 401 with correct message ---
        assertThat(response.getStatusCode())
                .as("Blacklisted token on GET /auth/me should return HTTP 401")
                .isEqualTo(HttpStatus.UNAUTHORIZED);

        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().message())
                .as("Error message should be 'Token has been revoked'")
                .isEqualTo("Token has been revoked");
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
