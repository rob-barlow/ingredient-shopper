package com.glendas.shopper.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.glendas.shopper.entity.AdminSessionEntity;

/**
 * Data access for admin sessions (ADR-006). The id is the token's SHA-256 hash, so the inherited
 * {@code existsById(hash)} and {@code deleteById(hash)} are all login and logout need (T-013).
 */
public interface AdminSessionRepository extends JpaRepository<AdminSessionEntity, String> {
}
