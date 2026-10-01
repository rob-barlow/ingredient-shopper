package com.glendas.shopper.security;

import java.io.IOException;

import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/**
 * What a guest gets when they call an admin-only endpoint (004 AC-8): a 401 in the contract's
 * {@code Problem} shape (RFC 9457). Spring Security calls this instead of its default HTML/empty
 * response.
 */
public class ProblemAuthenticationEntryPoint implements AuthenticationEntryPoint {

    static final String BODY = """
        {"type":"about:blank","title":"Unauthorized","status":401,"detail":"Log in as the admin to do this."}""";

    @Override
    public void commence(HttpServletRequest request, HttpServletResponse response,
                         AuthenticationException authException) throws IOException {
        response.setStatus(HttpStatus.UNAUTHORIZED.value());
        response.setContentType(MediaType.APPLICATION_PROBLEM_JSON_VALUE);
        response.getWriter().write(BODY);
    }
}
