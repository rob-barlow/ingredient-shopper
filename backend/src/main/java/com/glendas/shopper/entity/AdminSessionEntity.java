package com.glendas.shopper.entity;

import java.time.Instant;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

/**
 * A row in {@code admin_sessions} (V3, ADR-006): one logged-in admin browser. The primary key is
 * the token's SHA-256 hash. The raw token is never stored.
 */
@Entity
@Table(name = "admin_sessions")
public class AdminSessionEntity {

    @Id
    private String tokenHash;

    private Instant createdAt;

    protected AdminSessionEntity() {
    }

    public AdminSessionEntity(String tokenHash, Instant createdAt) {
        this.tokenHash = tokenHash;
        this.createdAt = createdAt;
    }

    public String getTokenHash() {
        return tokenHash;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }
}
