package com.consignment.auth.api;

import com.consignment.auth.model.User;
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

    public AuthController(AuthenticationManager authManager, UserRepository userRepository,
                          PasswordEncoder passwordEncoder, JwtUtil jwtUtil) {
        this.authManager = authManager;
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtUtil = jwtUtil;
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@Valid @RequestBody LoginRequest request) {
        try {
            Authentication auth = authManager.authenticate(
                    new UsernamePasswordAuthenticationToken(request.username(), request.password()));
            UserDetails userDetails = (UserDetails) auth.getPrincipal();
            Set<String> roles = userDetails.getAuthorities().stream()
                    .map(a -> a.getAuthority()).collect(java.util.stream.Collectors.toSet());
            String token = jwtUtil.generateToken(userDetails.getUsername(), roles);
            return ResponseEntity.ok(Map.of(
                    "token", token,
                    "token_type", "Bearer",
                    "expires_in", jwtUtil.getExpirationMs() / 1000,
                    "username", userDetails.getUsername(),
                    "roles", roles
            ));
        } catch (DisabledException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("error", "Account is disabled"));
        } catch (AuthenticationException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("error", "Invalid username or password"));
        }
    }

    @PostMapping("/register")
    public ResponseEntity<?> register(@Valid @RequestBody RegisterRequest request) {
        if (userRepository.existsByUsername(request.username())) {
            return ResponseEntity.badRequest().body(Map.of("error", "Username already taken"));
        }
        User user = new User();
        user.setUsername(request.username());
        user.setEmail(request.email());
        user.setPassword(passwordEncoder.encode(request.password()));
        user.setRoles(Set.of("ROLE_USER"));
        userRepository.save(user);
        return ResponseEntity.ok(Map.of("message", "User registered successfully"));
    }

    @PostMapping("/validate")
    public ResponseEntity<?> validate(@RequestHeader("Authorization") String bearerToken) {
        String token = bearerToken.replace("Bearer ", "");
        if (!jwtUtil.validateToken(token)) {
            return ResponseEntity.status(401).body(Map.of("valid", false));
        }
        return ResponseEntity.ok(Map.of("valid", true, "username", jwtUtil.extractUsername(token)));
    }

    record LoginRequest(@NotBlank String username, @NotBlank String password) {}
    record RegisterRequest(@NotBlank String username, @NotBlank String email, @NotBlank String password) {}
}
