package com.consignment.auth.api;

import com.consignment.auth.model.User;
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
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Property-based test for Missing User Profile Blocks Login.
 *
 * // Feature: consignee-role, Property 4: Missing User Profile Blocks Login
 *
 * Validates: Requirements 2.3
 */
class MissingUserProfileBlocksLoginPropertyTest {

    /**
     * Property 4: Missing User Profile Blocks Login
     *
     * For any user with ROLE_CONSIGNEE that has no entry in user_profiles,
     * the login attempt SHALL be rejected with HTTP 403 and message
     * "Consignee store mapping not found".
     *
     * Validates: Requirements 2.3
     */
    @Property(tries = 100)
    void missingUserProfileBlocksLogin(
            @ForAll @AlphaChars @StringLength(min = 1, max = 50) String username,
            @ForAll @AlphaChars @StringLength(min = 8, max = 50) String password) {

        // Feature: consignee-role, Property 4: Missing User Profile Blocks Login

        // --- Setup mocks ---
        AuthenticationManager authManager = mock(AuthenticationManager.class);
        UserRepository userRepository = mock(UserRepository.class);
        UserProfileRepository userProfileRepository = mock(UserProfileRepository.class);
        TokenBlacklistRepository blacklist = mock(TokenBlacklistRepository.class);
        JwtUtil jwtUtil = createJwtUtil();

        // Mock: authentication succeeds, principal has ROLE_CONSIGNEE
        UserDetails userDetails = new org.springframework.security.core.userdetails.User(
                username,
                password,
                List.of(new SimpleGrantedAuthority("ROLE_CONSIGNEE"))
        );
        Authentication auth = new UsernamePasswordAuthenticationToken(
                userDetails, null, userDetails.getAuthorities());
        when(authManager.authenticate(any())).thenReturn(auth);

        // Mock: user exists in users table
        User user = new User();
        user.setUsername(username);
        ReflectionTestUtils.setField(user, "id", 1L);
        when(userRepository.findByUsername(username)).thenReturn(Optional.of(user));

        // Mock: NO user_profiles entry for this user
        when(userProfileRepository.findByUserId(any())).thenReturn(Optional.empty());

        // Instantiate AuthController directly (unit test, no Spring context)
        AuthController controller = new AuthController(
                authManager, userRepository, userProfileRepository,
                mock(org.springframework.security.crypto.password.PasswordEncoder.class),
                jwtUtil, blacklist, mock(com.consignment.auth.service.MenuService.class)
        );

        // Build login request via reflection (LoginRequest is a private record)
        Object loginRequest = buildLoginRequest(username, password);

        // --- Invoke login ---
        @SuppressWarnings("unchecked")
        ResponseEntity<ApiResponse<?>> response =
                (ResponseEntity<ApiResponse<?>>) invokeLogin(controller, loginRequest);

        // --- Assert: HTTP 403 with correct message ---
        assertThat(response.getStatusCode())
                .as("Login with ROLE_CONSIGNEE and no user_profiles entry should return HTTP 403")
                .isEqualTo(HttpStatus.FORBIDDEN);

        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().message())
                .as("Error message should be 'Consignee store mapping not found'")
                .isEqualTo("Consignee store mapping not found");

        assertThat(response.getBody().status())
                .as("Response status code in body should be 403")
                .isEqualTo(403);
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

    /**
     * Build a LoginRequest (private record inside AuthController) via reflection.
     */
    private Object buildLoginRequest(String username, String password) {
        try {
            Class<?>[] declaredClasses = AuthController.class.getDeclaredClasses();
            Class<?> loginRequestClass = null;
            for (Class<?> c : declaredClasses) {
                if (c.getSimpleName().equals("LoginRequest")) {
                    loginRequestClass = c;
                    break;
                }
            }
            if (loginRequestClass == null) {
                throw new IllegalStateException("LoginRequest record not found in AuthController");
            }
            loginRequestClass.getDeclaredConstructors()[0].setAccessible(true);
            return loginRequestClass.getDeclaredConstructors()[0].newInstance(username, password);
        } catch (Exception e) {
            throw new RuntimeException("Failed to build LoginRequest", e);
        }
    }

    /**
     * Invoke AuthController.login() via reflection.
     */
    private Object invokeLogin(AuthController controller, Object loginRequest) {
        try {
            java.lang.reflect.Method loginMethod = AuthController.class.getDeclaredMethod(
                    "login", loginRequest.getClass());
            loginMethod.setAccessible(true);
            return loginMethod.invoke(controller, loginRequest);
        } catch (java.lang.reflect.InvocationTargetException e) {
            throw new RuntimeException("login() threw an exception", e.getCause());
        } catch (Exception e) {
            throw new RuntimeException("Failed to invoke login()", e);
        }
    }
}
