package com.glendas.shopper.config;

import java.util.List;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Typed view of the {@code shopper.*} settings in application.yaml, which come from
 * environment variables (plan §9). Features add their own groups here as they need them.
 */
@ConfigurationProperties(prefix = "shopper")
public record ShopperProperties(Cors cors) {

    /** @param allowedOrigins where the frontend is served from (CORS_ALLOWED_ORIGINS, comma-separated) */
    public record Cors(List<String> allowedOrigins) {
    }
}
