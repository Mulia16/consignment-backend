package com.consignment.auth.api;

import com.consignment.auth.model.User;
import com.consignment.auth.model.UserProfile;
import com.consignment.auth.repository.TokenBlacklistRepository;
import com.consignment.auth.repository.UserProfileRepository;
import com.consignment.auth.repository.UserRepository;
import com.consignment.auth.security.JwtUtil;
import com.consignment.auth.service.MenuService;
import jakarta.validation.constraints.Email;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

@RestController
@RequestMapping("/auth")
public class AuthController {

    private final AuthenticationManager authManager;
    private final UserRepository userRepository;
    private final UserProfileRepository userProfileRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;
    private final TokenBlacklistRepository blacklist;
    private final MenuService menuService;

    public AuthController(AuthenticationManager authManager, UserRepository userRepository,
                          UserProfileRepository userProfileRepository,
                          PasswordEncoder passwordEncoder, JwtUtil jwtUtil,
                          TokenBlacklistRepository blacklist, MenuService menuService) {
        this.authManager = authManager;
        this.userRepository = userRepository;
        this.userProfileRepository = userProfileRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtUtil = jwtUtil;
        this.blacklist = blacklist;
        this.menuService = menuService;
    }

    @PostMapping("/login")
    public ResponseEntity<ApiResponse<?>> login(@Valid @RequestBody LoginRequest request) {
        try {
            Authentication auth = authManager.authenticate(
                    new UsernamePasswordAuthenticationToken(request.username(), request.password()));
            UserDetails userDetails = (UserDetails) auth.getPrincipal();
            Set<String> roles = userDetails.getAuthorities().stream()
                    .map(a -> a.getAuthority())
                    .collect(java.util.stream.Collectors.toSet());

            String store = null;
            if (roles.contains("ROLE_CONSIGNEE")) {
                User user = userRepository.findByUsername(userDetails.getUsername())
                        .orElseThrow(() -> new RuntimeException("User not found"));
                Optional<UserProfile> profileOpt = userProfileRepository.findByUserId(user.getId());
                if (profileOpt.isEmpty()) {
                    return ResponseEntity.status(HttpStatus.FORBIDDEN)
                            .body(ApiResponse.error(403, "Consignee store mapping not found"));
                }
                store = profileOpt.get().getConsigneeStore();
            }

            String token = jwtUtil.generateToken(userDetails.getUsername(), roles, store);
            Map<String, Object> data = Map.of(
                    "token", token,
                    "token_type", "Bearer",
                    "expires_in", jwtUtil.getExpirationMs() / 1000,
                    "username", userDetails.getUsername(),
                    "roles", roles
            );
            return ResponseEntity.ok(ApiResponse.ok("Login successful", data));
        } catch (DisabledException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ApiResponse.error(401, "Account is disabled"));
        } catch (AuthenticationException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ApiResponse.error(401, "Invalid username or password"));
        }
    }

    @PostMapping("/logout")
    public ResponseEntity<ApiResponse<?>> logout(@RequestHeader("Authorization") String bearerToken) {
        String token = bearerToken.replace("Bearer ", "").trim();
        if (!jwtUtil.validateToken(token)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ApiResponse.error(401, "Token is invalid or already expired"));
        }
        blacklist.blacklist(jwtUtil.extractJti(token), jwtUtil.extractExpiration(token));
        return ResponseEntity.ok(ApiResponse.ok("Logged out successfully", null));
    }

    @PostMapping("/register")
    public ResponseEntity<ApiResponse<?>> register(@Valid @RequestBody RegisterRequest request) {
        if (userRepository.existsByUsername(request.username())) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(ApiResponse.error(409, "Validation failed", java.util.List.of(Map.of(
                    "field", "username",
                    "message", "Username already exists",
                    "value", request.username()
            ))));
        }
        if (userRepository.existsByEmail(request.email())) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(ApiResponse.error(409, "Validation failed", java.util.List.of(Map.of(
                    "field", "email",
                    "message", "Email already exists",
                    "value", request.email()
            ))));
        }
        User user = new User();
        user.setUsername(request.username());
        user.setEmail(request.email());
        user.setPassword(passwordEncoder.encode(request.password()));
        user.setRoles(Set.of("ROLE_USER"));
        userRepository.save(user);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("User registered successfully", null));
    }

    @PostMapping("/validate")
    public ResponseEntity<ApiResponse<?>> validate(@RequestHeader("Authorization") String bearerToken) {
        String token = bearerToken.replace("Bearer ", "").trim();
        if (!jwtUtil.validateToken(token)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ApiResponse.error(401, "Token is invalid or expired"));
        }
        String jti = jwtUtil.extractJti(token);
        if (blacklist.isBlacklisted(jti)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ApiResponse.error(401, "Token has been revoked"));
        }
        Map<String, Object> data = Map.of(
                "valid", true,
                "username", jwtUtil.extractUsername(token)
        );
        return ResponseEntity.ok(ApiResponse.success(data));
    }

        record LoginRequest(
            @NotBlank(message = "username is required") String username,
            @NotBlank(message = "password is required") String password
        ) {}

        record RegisterRequest(
            @NotBlank(message = "username is required") @Size(max = 100, message = "username max length is 100") String username,
            @NotBlank(message = "email is required") @Email(message = "email format is invalid") @Size(max = 150, message = "email max length is 150") String email,
            @NotBlank(message = "password is required") @Size(min = 8, max = 100, message = "password length must be between 8 and 100") String password
        ) {}

    @GetMapping("/me")
    public ResponseEntity<ApiResponse<?>> getMe(
            @RequestHeader(value = "Authorization", required = false) String bearerToken) {
        if (bearerToken == null || !bearerToken.startsWith("Bearer ")) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ApiResponse.error(401, "Token is invalid or expired"));
        }
        String token = bearerToken.substring(7).trim();
        
        // Validate token
        if (!jwtUtil.validateToken(token)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ApiResponse.error(401, "Token is invalid or expired"));
        }
        
        // Check blacklist
        String jti = jwtUtil.extractJti(token);
        if (blacklist.isBlacklisted(jti)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ApiResponse.error(401, "Token has been revoked"));
        }
        
        // Extract username and get user from database
        String username = jwtUtil.extractUsername(token);
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));
        
        // Get roles
        Set<String> roles = user.getRoles();
        
        // Get store if user has ROLE_CONSIGNEE
        String store = null;
        if (roles.contains("ROLE_CONSIGNEE")) {
            Optional<UserProfile> profileOpt = userProfileRepository.findByUserId(user.getId());
            if (profileOpt.isPresent()) {
                store = profileOpt.get().getConsigneeStore();
            }
        }
        
        // Create response
        MeResponse meResponse = new MeResponse(username, user.getEmail(), roles, store);
        return ResponseEntity.ok(ApiResponse.ok("User info retrieved successfully", meResponse));
    }

    record MeResponse(
        String username,
        String email,
        Set<String> roles,
        String store
    ) {}

    @GetMapping("/me/menus")
    public ResponseEntity<ApiResponse<?>> getMenus(
            @RequestHeader(value = "Authorization", required = false) String bearerToken) {
        if (bearerToken == null || !bearerToken.startsWith("Bearer ")) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ApiResponse.error(401, "Token is invalid or expired"));
        }
        String token = bearerToken.substring(7).trim();

        if (!jwtUtil.validateToken(token)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ApiResponse.error(401, "Token is invalid or expired"));
        }

        String jti = jwtUtil.extractJti(token);
        if (blacklist.isBlacklisted(jti)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ApiResponse.error(401, "Token has been revoked"));
        }

        @SuppressWarnings("unchecked")
        List<?> rawRoles = jwtUtil.parseClaims(token).get("roles", List.class);
        Set<String> roles = rawRoles == null ? Set.of()
                : rawRoles.stream().map(Object::toString)
                          .collect(java.util.stream.Collectors.toSet());

        List<String> menus = menuService.getMenusForRoles(roles);
        return ResponseEntity.ok(ApiResponse.ok("Menus retrieved successfully", menus));
    }
}
