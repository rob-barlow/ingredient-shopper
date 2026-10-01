package com.glendas.shopper.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.glendas.shopper.entity.CategoryEntity;

/**
 * Data access for categories.
 *
 * <p><b>Spring Data in one paragraph:</b> you write only this interface, and Spring generates the
 * implementation at startup. {@code JpaRepository<CategoryEntity, Integer>} (entity type, id
 * type) gives you {@code findById}, {@code findAll}, {@code save}, {@code existsById} and more
 * for free. Extra queries are worked out from the <b>method name</b>: {@code findAllByOrderByNameAsc}
 * becomes {@code SELECT … FROM categories ORDER BY name ASC}. It's a bit like EF Core's DbSet with LINQ,
 * but the query is written in the name.
 */
public interface CategoryRepository extends JpaRepository<CategoryEntity, Integer> {

    /** 001 AC-10: categories in alphabetical order. */
    List<CategoryEntity> findAllByOrderByNameAsc();
}
