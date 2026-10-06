package com.glendas.shopper.security;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockFilterChain;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;

import com.glendas.shopper.service.AuthService;

/** Unit test: the filter marks requests with a valid bearer token as the admin, and no others (ADR-006). */
class AdminSessionFilterTest {

    private final AuthService authService = mock(AuthService.class);
    private final AdminSessionFilter filter = new AdminSessionFilter(authService);

    @AfterEach
    void clearSecurityContext() {
        SecurityContextHolder.clearContext();
    }

    private Authentication filterRequestWithAuthorization(String headerValue) throws Exception {
        var request = new MockHttpServletRequest();
        if (headerValue != null) {
            request.addHeader("Authorization", headerValue);
        }
        var chain = new MockFilterChain();

        filter.doFilter(request, new MockHttpServletResponse(), chain);

        assertThat(chain.getRequest()).as("the request always carries on down the chain").isNotNull();
        return SecurityContextHolder.getContext().getAuthentication();
    }

    @Test
    void validToken_isTheAdmin() throws Exception {
        when(authService.isValidSession("good-token")).thenReturn(true);
        when(authService.adminUsername()).thenReturn("glenda");

        Authentication auth = filterRequestWithAuthorization("Bearer good-token");

        assertThat(auth).isNotNull();
        assertThat(auth.getName()).isEqualTo("glenda");
        assertThat(auth.getCredentials()).isEqualTo("good-token");
        assertThat(auth.getAuthorities()).extracting(GrantedAuthority::getAuthority)
            .containsExactly(AdminSessionFilter.ROLE_ADMIN);
    }

    @Test
    void unknownToken_isAGuest() throws Exception {
        when(authService.isValidSession("bad-token")).thenReturn(false);
        assertThat(filterRequestWithAuthorization("Bearer bad-token")).isNull();
    }

    @Test
    void noHeader_isAGuest() throws Exception {
        assertThat(filterRequestWithAuthorization(null)).isNull();
    }

    @Test
    void otherAuthorizationSchemes_areIgnored() throws Exception {
        assertThat(filterRequestWithAuthorization("Basic Z2xlbmRhOnBhc3M=")).isNull();
        assertThat(filterRequestWithAuthorization("Bearer ")).isNull();
    }
}
