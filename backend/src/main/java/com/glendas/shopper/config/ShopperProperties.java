package com.glendas.shopper.config;

import java.util.List;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Typed view of the {@code shopper.*} settings in application.yaml, which come from
 * environment variables (plan §9). Features add their own groups here as they need them.
 *
 * @param cors         cross-origin settings for the frontend
 * @param seedDemoData load Glenda's demo catalogue (SEED_DEMO_DATA, ADR-017)
 * @param admin        the single admin account (000 §5.1, ADR-006)
 */
@ConfigurationProperties(prefix = "shopper")
public record ShopperProperties(Cors cors, boolean seedDemoData, Admin admin) {

    /** @param allowedOrigins where the frontend is served from (CORS_ALLOWED_ORIGINS, comma-separated) */
    public record Cors(List<String> allowedOrigins) {
    }

    /**
     * @param username       ADMIN_USERNAME
     * @param passwordBcrypt ADMIN_PASSWORD_BCRYPT: a bcrypt <b>hash</b>, never the password itself
     */
    public record Admin(String username, String passwordBcrypt) {
    }
}
