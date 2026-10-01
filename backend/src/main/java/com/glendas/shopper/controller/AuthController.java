package com.glendas.shopper.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.RestController;

import com.glendas.shopper.api.AuthApi;
import com.glendas.shopper.api.model.AdminInfo;
import com.glendas.shopper.api.model.LoginRequest;
import com.glendas.shopper.api.model.LoginResult;
import com.glendas.shopper.service.AuthService;
import com.glendas.shopper.service.WrongCredentialsException;

/**
 * Admin login and logout (004 AC-2–8). Implements the interface generated from the contract, so
 * the URLs, methods and validation all come from {@code openapi.yaml} (ADR-013). This class
 * only says what to <i>do</i>.
 */
@RestController
public class AuthController implements AuthApi {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    /** {@code @Valid} on the interface has already rejected empty fields with a 422 (004 AC-4). */
    @Override
    public ResponseEntity<LoginResult> login(LoginRequest request) {
        String token = authService.login(request.getUsername(), request.getPassword())
            .orElseThrow(WrongCredentialsException::new);
        return ResponseEntity.ok(new LoginResult(token));
    }

    /** Only reachable with a valid token (SecurityConfig). The filter put the token in the credentials. */
    @Override
    public ResponseEntity<Void> logout() {
        String token = (String) SecurityContextHolder.getContext().getAuthentication().getCredentials();
        authService.logout(token);
        return ResponseEntity.noContent().build();
    }

    @Override
    public ResponseEntity<AdminInfo> getCurrentAdmin() {
        return ResponseEntity.ok(new AdminInfo(authService.adminUsername()));
    }
}
