-- V3: admin sessions (ADR-006, 004 plan §3, T-012).
--
-- One row per logged-in admin browser. The token itself is never stored: only its SHA-256
-- hash. Someone who could read this table still couldn't log in with what they found.
-- Logging out deletes the row (004 AC-7). Rows don't expire (004 §6, known limitation).

CREATE TABLE admin_sessions (
    token_hash  text         PRIMARY KEY,
    created_at  timestamptz  NOT NULL DEFAULT now()
);
