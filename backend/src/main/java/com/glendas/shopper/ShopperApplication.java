package com.glendas.shopper;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;

/**
 * Entry point for the Ingredient Shopper backend: a Stage 1 layered monolith (constitution Article I).
 */
@SpringBootApplication
@ConfigurationPropertiesScan
public class ShopperApplication {

    public static void main(String[] args) {
        SpringApplication.run(ShopperApplication.class, args);
    }
}
