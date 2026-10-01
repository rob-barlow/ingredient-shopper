package com.glendas.shopper.service;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.Clock;
import java.util.Base64;
import java.util.HexFormat;
import java.util.Optional;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.glendas.shopper.config.ShopperProperties;
import com.glendas.shopper.entity.AdminSessionEntity;
import com.glendas.shopper.repository.AdminSessionRepository;

/**
 * Admin login, logout and session checks (004 AC-2–8, ADR-006).
 *
 * <p>Tokens are 32 random bytes. Only their SHA-256 hash is stored, so the database never
 * holds anything that could be used to log in.
 */
@Service
public class AuthService {

    private static final SecureRandom RANDOM = new SecureRandom();
    private static final Base64.Encoder TOKEN_ENCODING = Base64.getUrlEncoder().withoutPadding();

    private final AdminSessionRepository sessions;
    private final PasswordEncoder passwordEncoder;
    private final ShopperProperties.Admin admin;
    private final Clock clock;

    public AuthService(AdminSessionRepository sessions, PasswordEncoder passwordEncoder,
                       ShopperProperties properties, Clock clock) {
        this.sessions = sessions;
        this.passwordEncoder = passwordEncoder;
        this.admin = properties.admin();
        this.clock = clock;
    }

    /**
     * Logs in, returning a new token, or empty if the username <b>or</b> password is wrong.
     * Callers can't tell which (004 AC-3).
     *
     * <p>Both checks <b>always</b> run (note {@code &}, not {@code &&}), and bcrypt is the slow
     * one. So a wrong username takes as long as a wrong password, and an attacker can't discover
     * the username by timing responses. {@link MessageDigest#isEqual} compares in constant time too.
     */
    @Transactional
    public Optional<String> login(String username, String password) {
        boolean passwordMatches = passwordEncoder.matches(password, admin.passwordBcrypt());
        boolean usernameMatches = MessageDigest.isEqual(
            username.getBytes(StandardCharsets.UTF_8), admin.username().getBytes(StandardCharsets.UTF_8));

        if (!(usernameMatches & passwordMatches)) {
            return Optional.empty();
        }

        byte[] randomBytes = new byte[32];
        RANDOM.nextBytes(randomBytes);
        String token = TOKEN_ENCODING.encodeToString(randomBytes);

        sessions.save(new AdminSessionEntity(hash(token), clock.instant()));
        return Optional.of(token);
    }

    /** Is this token a current admin session? Used by the security filter on every request. */
    @Transactional(readOnly = true)
    public boolean isValidSession(String token) {
        return sessions.existsById(hash(token));
    }

    /** Ends the session: the token stops working immediately (004 AC-7). */
    @Transactional
    public void logout(String token) {
        sessions.deleteById(hash(token));
    }

    /** The admin's username, for {@code GET /v1/auth/me}. */
    public String adminUsername() {
        return admin.username();
    }

    /** SHA-256 of the token, as lowercase hex: what's stored in {@code admin_sessions.token_hash}. */
    static String hash(String token) {
        try {
            byte[] digest = MessageDigest.getInstance("SHA-256").digest(token.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(digest);
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 is always available in Java", e);
        }
    }
}
