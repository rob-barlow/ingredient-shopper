package com.glendas.shopper.config;

import java.util.Arrays;
import java.util.stream.Stream;

import org.flywaydb.core.api.Location;
import org.springframework.boot.flyway.autoconfigure.FlywayConfigurationCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Database migrations (ADR-004, ADR-017).
 *
 * <ul>
 *   <li>{@code db/migration}: the schema and reference data. Always runs.</li>
 *   <li>{@code db/seed}: Glenda's demo catalogue. Added <b>only</b> when {@code SEED_DEMO_DATA=true}.</li>
 * </ul>
 */
@Configuration
public class FlywayConfig {

    static final String SEED_LOCATION = "classpath:db/seed";

    @Bean
    FlywayConfigurationCustomizer seedDataLocation(ShopperProperties properties) {
        return flyway -> {
            if (properties.seedDemoData()) {
                Location[] withSeed = Stream.concat(
                        Arrays.stream(flyway.getLocations()),
                        Stream.of(new Location(SEED_LOCATION)))
                    .toArray(Location[]::new);
                flyway.locations(withSeed);
            }
        };
    }
}
