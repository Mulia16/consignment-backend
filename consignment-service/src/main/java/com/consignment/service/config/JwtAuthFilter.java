package com.consignment.service.config;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Component
public class JwtAuthFilter extends OncePerRequestFilter {

    private final JwtUtil jwtUtil;
    private final boolean securityEnabled;

    public JwtAuthFilter(JwtUtil jwtUtil,
                         @Value("${app.security.enabled:false}") boolean securityEnabled) {
        this.jwtUtil = jwtUtil;
        this.securityEnabled = securityEnabled;
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        // When security is disabled, skip JWT validation entirely
        return !securityEnabled;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain chain) throws ServletException, IOException {

        String header = request.getHeader("Authorization");
        if (header == null || !header.startsWith("Bearer ")) {
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Unauthorized");
            return;
        }

        String token = header.substring(7);
        try {
            Claims claims = jwtUtil.parseClaims(token);

            String subject = claims.getSubject();

            // Extract roles claim — stored as List by JJWT
            @SuppressWarnings("unchecked")
            List<String> roles = claims.get("roles", List.class);
            if (roles == null) {
                roles = List.of();
            }

            String store = claims.get("store", String.class);

            // Build details map so ConsigneeContext can extract store and roles
            Map<String, Object> details = new HashMap<>();
            details.put("store", store);
            details.put("roles", roles);

            List<SimpleGrantedAuthority> authorities = roles.stream()
                    .map(SimpleGrantedAuthority::new)
                    .collect(Collectors.toList());

            UsernamePasswordAuthenticationToken authentication =
                    new UsernamePasswordAuthenticationToken(subject, null, authorities);
            authentication.setDetails(details);

            SecurityContextHolder.getContext().setAuthentication(authentication);

            chain.doFilter(request, response);

        } catch (JwtException e) {
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Token is invalid or expired");
        }
    }
}
