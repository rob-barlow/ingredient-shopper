package com.glendas.shopper.security;

import java.io.IOException;
import java.util.List;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

import com.glendas.shopper.service.AuthService;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/**
 * Runs once on every request (ADR-006). If there's an {@code Authorization: Bearer <token>}
 * header with a valid session token, the request is marked as coming from the admin
 * ({@code ROLE_ADMIN}). Otherwise it carries on as a guest.
 *
 * <p>The filter never <i>rejects</i> anything itself. {@link com.glendas.shopper.config.SecurityConfig}
 * decides which paths need the admin, and {@link ProblemAuthenticationEntryPoint} sends the 401.
 * Keeping "who are you?" separate from "are you allowed?" is the usual Spring Security split.
 *
 * <p>It's created in {@code SecurityConfig}, not as a {@code @Component}: Spring Boot registers
 * filter beans automatically, which would make this run twice.
 */
public class AdminSessionFilter extends OncePerRequestFilter {

    public static final String ROLE_ADMIN = "ROLE_ADMIN";
    private static final String BEARER = "Bearer ";

    private final AuthService authService;

    public AdminSessionFilter(AuthService authService) {
        this.authService = authService;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException {

        String header = request.getHeader("Authorization");
        if (header != null && header.startsWith(BEARER)) {
            String token = header.substring(BEARER.length()).trim();
            if (!token.isEmpty() && authService.isValidSession(token)) {
                // Principal = username, credentials = the token (logout needs it), plus the admin role.
                var authentication = new UsernamePasswordAuthenticationToken(
                    authService.adminUsername(), token, List.of(new SimpleGrantedAuthority(ROLE_ADMIN)));
                SecurityContextHolder.getContext().setAuthentication(authentication);
            }
        }
        chain.doFilter(request, response);
    }
}
