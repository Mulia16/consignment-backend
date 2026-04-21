package com.consignment.auth.api;

import com.consignment.auth.repository.TokenBlacklistRepository;
import com.consignment.auth.repository.UserProfileRepository;
import com.consignment.auth.repository.UserRepository;
import com.consignment.auth.security.JwtUtil;
import com.consignment.auth.service.MenuService;
import net.jqwik.api.ForAll;
import net.jqwik.api.Property;
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
 * Property-based test for Admin Menu List Is Correct.
 */
class AdminMenuListPropertyTest {

    @Property(tries = 50)
    void adminMenuListIsCorrect(
            @ForAll @AlphaChars @StringLength(min = 1, max = 50) String username) {

        AuthenticationManager authManager = mock(AuthenticationManager.class);
        UserRepository userRepository = mock(UserRepository.class);
        UserProfileRepository userProfileRepository = mock(UserProfileRepository.class);
        TokenBlacklistRepository blacklist = mock(TokenBlacklistRepository.class);
        PasswordEncoder passwordEncoder = mock(PasswordEncoder.class);

        when(blacklist.isBlacklisted(any())).thenReturn(false);

        JwtUtil jwtUtil = createJwtUtil();
        String token = jwtUtil.generateToken(username, Set.of("ROLE_ADMIN"), null);

        MenuService menuService = new MenuService();
        AuthController controller = new AuthController(
                authManager, userRepository, userProfileRepository,
                passwordEncoder, jwtUtil, blacklist, menuService
        );

        @SuppressWarnings("unchecked")
        ResponseEntity<ApiResponse<?>> response =
                (ResponseEntity<ApiResponse<?>>) controller.getMenus("Bearer " + token);

        assertThat(response.getStatusCode())
                .as("getMenus for ROLE_ADMIN should return HTTP 200")
                .isEqualTo(HttpStatus.OK);

        assertThat(response.getBody()).isNotNull();

        @SuppressWarnings("unchecked")
        List<String> menus = (List<String>) response.getBody().data();

        assertThat(menus)
                .isNotNull()
                .containsExactly(
                        "CONSIGNMENT_RECEIVING",
                        "CONSIGNMENT_STOCK_OUT",
                        "CONSIGNMENT_STOCK_REQUEST",
                        "CONSIGNMENT_DELIVERY_ORDER",
                        "CONSIGNMENT_STOCK_RETURN",
                        "CONSIGNMENT_STOCK_RETURN_COLLECT",
                        "CONSIGNMENT_STOCK_ADJUSTMENT",
                        "SETTLEMENT_CUSTOMER_COMPUTE",
                        "SETTLEMENT_CUSTOMER_BILLING",
                        "SETTLEMENT_FAILURE_CUSTOMER",
                        "SETTLEMENT_SUPPLIER_COMPUTE",
                        "SETTLEMENT_SUPPLIER_BILLING",
                        "SETTLEMENT_FAILURE_SUPPLIER",
                        "MASTER_DATA_COMPANIES",
                        "MASTER_DATA_STORES",
                        "MASTER_DATA_SUPPLIERS",
                        "MASTER_DATA_CONTRACTS",
                        "MASTER_DATA_ITEMS",
                        "SETUP_CONSIGNMENT_ITEMS",
                        "REPORT_CENTER",
                        "REPORT_CSRQ",
                        "REPORT_CSRV",
                        "REPORT_CSO",
                        "REPORT_CSDO",
                        "REPORT_CSR",
                        "REPORT_CSA",
                        "REPORT_SETTLEMENT_SUMMARY",
                        "REPORT_SETTLEMENT_DETAIL",
                        "REPORT_SUPPLIER_BOOK_VALUE",
                        "REPORT_CUSTOMER_INVENTORY",
                        "REPORT_RESERVATIONS",
                        "REPORT_CONSIGNMENT_SETUP",
                        "SYSTEM_AUDIT_LOG"
                );
    }

    private JwtUtil createJwtUtil() {
        JwtUtil jwtUtil = new JwtUtil();
        ReflectionTestUtils.setField(jwtUtil, "secret",
                "test-secret-key-for-property-tests-minimum-32-chars!!");
        ReflectionTestUtils.setField(jwtUtil, "expirationMs", 86400000L);
        return jwtUtil;
    }
}
