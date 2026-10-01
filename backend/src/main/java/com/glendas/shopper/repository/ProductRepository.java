package com.glendas.shopper.repository;

import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.glendas.shopper.entity.ProductEntity;

/**
 * Data access for products. See {@link CategoryRepository} for how Spring Data works.
 *
 * <p>T-017 adds the conditional stock update here (ADR-008).
 */
public interface ProductRepository extends JpaRepository<ProductEntity, UUID> {

    /**
     * 004 AC-14: is this name already used, ignoring case and spaces? Pass a key made the same way
     * as the database's {@code name_key}, i.e. {@code lower(trim(name))}.
     * Name-derived query: {@code SELECT EXISTS (… WHERE name_key = ?)}.
     */
    boolean existsByNameKey(String nameKey);

    /** 004 AC-15: the same check while editing, ignoring the product being edited. */
    boolean existsByNameKeyAndIdNot(String nameKey, UUID id);
}
