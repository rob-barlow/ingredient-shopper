package com.glendas.shopper.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

import com.glendas.shopper.config.ShopperProperties;
import com.glendas.shopper.entity.AdminSessionEntity;
import com.glendas.shopper.repository.AdminSessionRepository;

/**
 * Unit tests for login, logout and session checks (004 AC-2, AC-3, AC-7, ADR-006).
 *
 * <p><b>Mockito</b> fakes the repository, so there's no database: {@code mock(...)} makes a
 * stand-in, {@code when(...).thenReturn(...)} programs it, and {@code verify(...)} checks how it
 * was called. Similar to Moq in .NET.
 */
class AuthServiceTest {

    private static final String USERNAME = "glenda";
    private static final String PASSWORD = "correct horse battery staple";
    private static final Instant NOW = Instant.parse("2026-10-01T12:00:00Z");

    // Strength 4 is the fastest bcrypt allows: fine for tests, too weak for real use.
    private final BCryptPasswordEncoder encoder = new BCryptPasswordEncoder(4);
    private AdminSessionRepository sessions;
    private AuthService auth;

    @BeforeEach
    void setUp() {
        sessions = mock(AdminSessionRepository.class);
        auth = newAuthService(encoder);
    }

    private AuthService newAuthService(PasswordEncoder passwordEncoder) {
        var properties = new ShopperProperties(null, false,
            new ShopperProperties.Admin(USERNAME, encoder.encode(PASSWORD)));
        return new AuthService(sessions, passwordEncoder, properties, Clock.fixed(NOW, ZoneOffset.UTC));
    }

    @Test
    void correctCredentials_returnAToken_andStoreOnlyItsHash() {
        var token = auth.login(USERNAME, PASSWORD);

        assertThat(token).isPresent();
        var saved = ArgumentCaptor.forClass(AdminSessionEntity.class);
        verify(sessions).save(saved.capture());
        assertThat(saved.getValue().getTokenHash())
            .isEqualTo(AuthService.hash(token.get()))
            .isNotEqualTo(token.get());                 // never the raw token
        assertThat(saved.getValue().getCreatedAt()).isEqualTo(NOW);
    }

    @Test
    void eachLogin_getsADifferentToken() {
        assertThat(auth.login(USERNAME, PASSWORD)).isNotEqualTo(auth.login(USERNAME, PASSWORD));
    }

    @Test
    void wrongPassword_givesNothing_andStoresNothing() {
        assertThat(auth.login(USERNAME, "wrong")).isEmpty();
        verify(sessions, never()).save(any());
    }

    @Test
    void wrongUsername_givesNothing_andStoresNothing() {
        assertThat(auth.login("not-glenda", PASSWORD)).isEmpty();
        verify(sessions, never()).save(any());
    }

    @Test
    void wrongUsername_stillRunsThePasswordCheck_soTimingDoesntRevealTheUsername() {
        PasswordEncoder spyEncoder = mock(PasswordEncoder.class);
        when(spyEncoder.matches(any(), anyString())).thenReturn(false);
        var authWithSpy = newAuthService(spyEncoder);

        authWithSpy.login("not-glenda", "anything");

        verify(spyEncoder).matches(any(), anyString());   // bcrypt ran even though the username was wrong
    }

    @Test
    void sessionCheck_andLogout_useTheTokensHash() {
        String token = "some-token";
        when(sessions.existsById(AuthService.hash(token))).thenReturn(true);

        assertThat(auth.isValidSession(token)).isTrue();
        assertThat(auth.isValidSession("another-token")).isFalse();

        auth.logout(token);
        verify(sessions).deleteById(AuthService.hash(token));
    }

    @Test
    void hash_isSha256Hex() {
        // Known SHA-256 of "abc", from the standard test vectors
        assertThat(AuthService.hash("abc"))
            .isEqualTo("ba7816bf8f01cfea414140de5dae2223b00361a396177a9cb410ff61f20015ad");
    }
}
