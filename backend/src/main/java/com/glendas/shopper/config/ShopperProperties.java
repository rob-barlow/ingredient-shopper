package com.glendas.shopper.config;

import java.util.List;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Typed view of the {@code shopper.*} settings in application.yaml, which come from
 * environment variables (plan §9). Features add their own groups here as they need them.
 *
 * @param cors         cross-origin settings for the frontend
 * @param seedDemoData load Glenda's demo catalogue (SEED_DEMO_DATA, ADR-017)
 */
@ConfigurationProperties(prefix = "shopper")
public record ShopperProperties(Cors cors, boolean seedDemoData) {

    /** @param allowedOrigins where the frontend is served from (CORS_ALLOWED_ORIGINS, comma-separated) */
    public record Cors(List<String> allowedOrigins) {
    }
}
