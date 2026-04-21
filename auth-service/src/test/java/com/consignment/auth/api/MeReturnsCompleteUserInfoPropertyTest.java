package com.consignment.auth.api;

import com.consignment.auth.model.User;
import com.consignment.auth.model.UserProfile;
import com.consignment.auth.repository.TokenBlacklistRepository;
import com.consignment.auth.repository.UserProfileRepository;
import com.consignment.auth.repository.UserRepository;
import com.consignment.auth.security.JwtUtil;
import net.jqwik.api.*;
import net.jqwik.api.constraints.AlphaChars;
import net.jqwik.api.constraints.StringLength;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Property-based test for /auth/me Returns Complete User Info.
 *
 * // Feature: consignee-role, Property 5: /auth/me Returns Complete User Info
 *
 * Validates: Requirements 4.1
 */
class MeReturnsCompleteUserInfoPropertyTest {

    private static final String TEST_SECRET =
            "test-secret-key-for-property-tests-minimum-32-chars!!";

    /**
     * Property 5: /auth/me Returns Complete User Info (ROLE_CONSIGNEE user with store)
     *
     * For any valid, non-blacklisted JWT for a consignee user, GET /auth/me SHALL return
     * HTTP 200 with a body containing all four fields: username, email, roles, and store.
     *
     * Validates: Requirements 4.1
     */
    @Property(tries = 100)
    void meReturnsCompleteUserInfoForConsignee(
            @ForAll @AlphaChars @StringLength(min = 1, max = 30) String username,
            @ForAll @AlphaChars @StringLength(min = 1, max = 30) String emailLocal,
            @ForAll @AlphaChars @StringLength(min = 1, max = 20) String store) {

        // Feature: consignee-role, Property 5: /auth/me Returns Complete User Info

        String email = emailLocal + "@example.com";
        JwtUtil jwtUtil = createJwtUtil();

        // Generate a valid JWT for a ROLE_CONSIGNEE user with a store claim
        String token = jwtUtil.generateToken(username, Set.of("ROLE_CONSIGNEE"), store);

        // Setup mocks
        UserRepository userRepository = mock(UserRepository.class);
        UserProfileRepository userProfileRepository = mock(UserProfileRepository.class);
        TokenBlacklistRepository blacklist = mock(TokenBlacklistRepository.class);

        User user = new User();
        user.setUsername(username);
        user.setEmail(email);
        user.setRoles(Set.of("ROLE_CONSIGNEE"));
        ReflectionTestUtils.setField(user, "id", 1L);

        UserProfile profile = new UserProfile();
        profile.setConsigneeStore(store);
        profile.setUser(user);

        when(userRepository.findByUsername(username)).thenReturn(Optional.of(user));
        when(userProfileRepository.findByUserId(1L)).thenReturn(Optional.of(profile));
        when(blacklist.isBlacklisted(any())).thenReturn(false);

        AuthController controller = new AuthController(
                mock(AuthenticationManager.class),
                userRepository,
                userProfileRepository,
                mock(org.springframework.security.crypto.password.PasswordEncoder.class),
                jwtUtil,
                blacklist,
                mock(com.consignment.auth.service.MenuService.class)
        );

        // Invoke GET /auth/me
        @SuppressWarnings("unchecked")
        ResponseEntity<ApiResponse<?>> response =
                (ResponseEntity<ApiResponse<?>>) controller.getMe("Bearer " + token);

        // Assert HTTP 200
        assertThat(response.getStatusCode())
                .as("GET /auth/me with valid consignee token should return HTTP 200")
                .isEqualTo(HttpStatus.OK);

        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().data()).isNotNull();

        // Assert all four fields are present in the response body
        Object data = response.getBody().data();
        assertThat(data).isNotNull();
        @SuppressWarnings("unchecked")
        Map<String, Object> dataMap = toMap(data);

        assertThat(dataMap).containsKey("username");
        assertThat(dataMap).containsKey("email");
        assertThat(dataMap).containsKey("roles");
        assertThat(dataMap).containsKey("store");

        assertThat(dataMap.get("username")).isEqualTo(username);
        assertThat(dataMap.get("email")).isEqualTo(email);
        assertThat(dataMap.get("store")).isEqualTo(store);
    }

    /**
     * Property 5: /auth/me Returns Complete User Info (non-consignee user, store is null)
     *
     * For any valid, non-blacklisted JWT for a non-consignee user, GET /auth/me SHALL return
     * HTTP 200 with a body containing all four fields: username, email, roles, and store
     * (store is nullable for non-consignee).
     *
     * Validates: Requirements 4.1
     */
    @Property(tries = 100)
    void meReturnsCompleteUserInfoForNonConsignee(
            @ForAll @AlphaChars @StringLength(min = 1, max = 30) String username,
            @ForAll @AlphaChars @StringLength(min = 1, max = 30) String emailLocal,
            @ForAll("nonConsigneeRoles") Set<String> roles) {

        // Feature: consignee-role, Property 5: /auth/me Returns Complete User Info

        String email = emailLocal + "@example.com";
        JwtUtil jwtUtil = createJwtUtil();

        // Generate a valid JWT for a non-consignee user (no store claim)
        String token = jwtUtil.generateToken(username, roles, null);

        // Setup mocks
        UserRepository userRepository = mock(UserRepository.class);
        UserProfileRepository userProfileRepository = mock(UserProfileRepository.class);
        TokenBlacklistRepository blacklist = mock(TokenBlacklistRepository.class);

        User user = new User();
        user.setUsername(username);
        user.setEmail(email);
        user.setRoles(roles);
        ReflectionTestUtils.setField(user, "id", 2L);

        when(userRepository.findByUsername(username)).thenReturn(Optional.of(user));
        when(blacklist.isBlacklisted(any())).thenReturn(false);

        AuthController controller = new AuthController(
                mock(AuthenticationManager.class),
                userRepository,
                userProfileRepository,
                mock(org.springframework.security.crypto.password.PasswordEncoder.class),
                jwtUtil,
                blacklist,
                mock(com.consignment.auth.service.MenuService.class)
        );

        // Invoke GET /auth/me
        @SuppressWarnings("unchecked")
        ResponseEntity<ApiResponse<?>> response =
                (ResponseEntity<ApiResponse<?>>) controller.getMe("Bearer " + token);

        // Assert HTTP 200
        assertThat(response.getStatusCode())
                .as("GET /auth/me with valid non-consignee token should return HTTP 200")
                .isEqualTo(HttpStatus.OK);

        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().data()).isNotNull();

        // Assert all four fields are present (store may be null for non-consignee)
        Object data = response.getBody().data();
        assertThat(data).isNotNull();
        @SuppressWarnings("unchecked")
        Map<String, Object> dataMap = toMap(data);

        assertThat(dataMap).containsKey("username");
        assertThat(dataMap).containsKey("email");
        assertThat(dataMap).containsKey("roles");
        assertThat(dataMap).containsKey("store");

        assertThat(dataMap.get("username")).isEqualTo(username);
        assertThat(dataMap.get("email")).isEqualTo(email);
        // store should be null for non-consignee
        assertThat(dataMap.get("store")).isNull();
    }

    // -------------------------------------------------------------------------
    // Providers
    // -------------------------------------------------------------------------

    @Provide
    Arbitrary<Set<String>> nonConsigneeRoles() {
        return Arbitraries.of("ROLE_ADMIN", "ROLE_USER")
                .set().ofMinSize(1).ofMaxSize(1);
    }

    // -------------------------------------------------------------------------
    // Helpers
    // -------------------------------------------------------------------------

    private JwtUtil createJwtUtil() {
        JwtUtil jwtUtil = new JwtUtil();
        ReflectionTestUtils.setField(jwtUtil, "secret", TEST_SECRET);
        ReflectionTestUtils.setField(jwtUtil, "expirationMs", 86400000L);
        return jwtUtil;
    }

    @SuppressWarnings("unchecked")
    private Map<String, Object> toMap(Object obj) {
        // MeResponse is a record — convert via its accessor methods using reflection
        if (obj instanceof Map) {
            return (Map<String, Object>) obj;
        }
        // It's a MeResponse record — extract fields via reflection
        try {
            Class<?> clazz = obj.getClass();
            Object usernameVal = clazz.getMethod("username").invoke(obj);
            Object emailVal = clazz.getMethod("email").invoke(obj);
            Object rolesVal = clazz.getMethod("roles").invoke(obj);
            Object storeVal = clazz.getMethod("store").invoke(obj);
            Map<String, Object> result = new HashMap<>();
            result.put("username", usernameVal);
            result.put("email", emailVal);
            result.put("roles", rolesVal);
            result.put("store", storeVal);  // may be null
            return result;
        } catch (Exception e) {
            throw new RuntimeException("Failed to convert response data to map", e);
        }
    }
}
