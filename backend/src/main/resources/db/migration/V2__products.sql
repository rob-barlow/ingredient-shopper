-- V2: products (004 plan §3, T-012).
--
-- The database enforces the rules a bug must never break, even if the Java checks were skipped
-- or two saves raced each other:
--   * unique names, ignoring case and leading/trailing spaces (004 AC-14): name_key + UNIQUE
--   * stock never below zero (004 AC-30, 000 §4.3): CHECK (stock_level >= 0)
--   * the field rules from 004 spec §3: the other CHECKs
-- The service still checks first, to give friendly field errors (422).

CREATE TABLE products (
    id            uuid          PRIMARY KEY,
    name          text          NOT NULL,
    -- Computed by PostgreSQL from name, so it can never disagree with it.
    name_key      text          GENERATED ALWAYS AS (lower(trim(name))) STORED,
    description   text          NOT NULL,
    category_id   integer       NOT NULL REFERENCES categories (id),
    unit_amount   numeric(9,2)  NOT NULL,
    unit_measure  text          NOT NULL,
    price_pence   integer       NOT NULL,
    image_url     text          NOT NULL,
    stock_level   integer       NOT NULL,

    CONSTRAINT products_name_key_unique   UNIQUE (name_key),
    CONSTRAINT products_name_length       CHECK (length(name) BETWEEN 1 AND 50),
    CONSTRAINT products_description_length CHECK (length(description) BETWEEN 1 AND 1000),
    CONSTRAINT products_unit_amount_positive CHECK (unit_amount > 0),
    CONSTRAINT products_unit_measure_known CHECK (unit_measure IN ('g', 'kg', 'ml', 'l', 'each')),
    CONSTRAINT products_price_range       CHECK (price_pence BETWEEN 1 AND 999999),
    CONSTRAINT products_stock_not_negative CHECK (stock_level >= 0)
);

-- Products are browsed by category (001), so index the foreign key.
CREATE INDEX products_category_id_idx ON products (category_id);
