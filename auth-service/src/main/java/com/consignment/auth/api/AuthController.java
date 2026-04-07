package com.consignment.auth.api;

import com.consignment.auth.model.User;
import com.consignment.auth.repository.TokenBlacklistRepository;
import com.consignment.auth.repository.UserRepository;
import com.consignment.auth.security.JwtUtil;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
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

import java.util.Map;
import java.util.Set;

@RestController
@RequestMapping("/auth")
public class AuthController {

    private final AuthenticationManager authManager;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;
    private final TokenBlacklistRepository blacklist;

    public AuthController(AuthenticationManager authManager, UserRepository userRepository,
                          PasswordEncoder passwordEncoder, JwtUtil jwtUtil,
                          TokenBlacklistRepository blacklist) {
        this.authManager = authManager;
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtUtil = jwtUtil;
        this.blacklist = blacklist;
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
            String token = jwtUtil.generateToken(userDetails.getUsername(), roles);
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
            return ResponseEntity.badRequest().body(ApiResponse.error(400, "Username already taken"));
        }
        if (userRepository.existsByEmail(request.email())) {
            return ResponseEntity.badRequest().body(ApiResponse.error(400, "Email already registered"));
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

    record LoginRequest(@NotBlank String username, @NotBlank String password) {}
    record RegisterRequest(@NotBlank String username, @NotBlank String email, @NotBlank String password) {}
}
