package com.glendas.shopper.config;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Arrays;
import java.util.List;

import org.flywaydb.core.api.Location;
import org.flywaydb.core.api.configuration.FluentConfiguration;
import org.junit.jupiter.api.Test;

/** Unit test: the seed location is added only when SEED_DEMO_DATA is true (ADR-017). No database. */
class FlywayConfigTest {

    private static List<String> locationsAfterCustomizing(boolean seedDemoData) {
        var properties = new ShopperProperties(new ShopperProperties.Cors(List.of()), seedDemoData);
        var flyway = new FluentConfiguration().locations("classpath:db/migration");

        new FlywayConfig().seedDataLocation(properties).customize(flyway);

        return Arrays.stream(flyway.getLocations()).map(Location::toString).toList();
    }

    @Test
    void seedDisabled_onlySchemaMigrationsRun() {
        assertThat(locationsAfterCustomizing(false))
            .containsExactly("classpath:db/migration");
    }

    @Test
    void seedEnabled_seedLocationIsAddedAfterSchemaMigrations() {
        assertThat(locationsAfterCustomizing(true))
            .containsExactly("classpath:db/migration", "classpath:db/seed");
    }
}
