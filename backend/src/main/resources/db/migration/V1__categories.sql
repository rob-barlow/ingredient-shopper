-- V1: the fixed category list (000 §5.5, T-005).
--
-- This is REFERENCE data, not demo data: the store can't work without categories (admins
-- must choose one for every product, 004), so it lives in db/migration and is loaded in
-- every environment. Demo products are seed data (db/seed, ADR-017).
--
-- The ids are fixed on purpose, so seed data and tests can refer to them in every database.
-- Never edit this file once applied (Flyway checksums it). Change categories with a new migration.

CREATE TABLE categories (
    id   integer PRIMARY KEY,
    name text    NOT NULL UNIQUE,
    CONSTRAINT categories_name_not_blank CHECK (length(trim(name)) > 0)
);

INSERT INTO categories (id, name) VALUES
    (1,  'Bakery'),
    (2,  'Dairy & Eggs'),
    (3,  'Drinks'),
    (4,  'Food Cupboard'),
    (5,  'Frozen'),
    (6,  'Fruit & Veg'),
    (7,  'Herbs & Spices'),
    (8,  'Household'),
    (9,  'Meat & Fish'),
    (10, 'Snacks & Sweets');
