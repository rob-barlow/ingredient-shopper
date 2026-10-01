package com.glendas.shopper.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

/**
 * A row in {@code categories}: the fixed list from V1 (000 §5.5). Read-only in the app, so it
 * has no setters. Admins choose categories, they never edit them.
 *
 * <p><b>JPA basics</b> (like EF Core entities): {@code @Entity} marks a class Hibernate maps to a
 * table, {@code @Table} names the table, and {@code @Id} marks the primary key. Fields map to
 * columns by name, with camelCase becoming snake_case automatically.
 */
@Entity
@Table(name = "categories")
public class CategoryEntity {

    @Id
    private Integer id;

    private String name;

    /** JPA needs a no-argument constructor to create objects from rows. {@code protected} keeps app code from using it. */
    protected CategoryEntity() {
    }

    public Integer getId() {
        return id;
    }

    public String getName() {
        return name;
    }
}
