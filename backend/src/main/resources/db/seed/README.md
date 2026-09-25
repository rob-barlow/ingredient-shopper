# Demo seed data (ADR-017)

Loaded **only** when `SEED_DEMO_DATA=true`: local dev, demos, CI and API tests. Never needed for the store to work.

- Files here are Flyway **repeatable** migrations, named `R__description.sql` (e.g. `R__demo_catalogue.sql`).
- Flyway runs them **after** every versioned migration in `db/migration`, so the tables they fill always exist.
- Flyway **re-runs** a repeatable migration whenever its contents change. So each file must be **safe to run again**, e.g. `INSERT … ON CONFLICT … DO UPDATE`.
- The demo catalogue (000 §9, at least 50 products) arrives with feature 001, once the `products` table exists (004).

*This README is here so the folder exists in git. Flyway ignores non-`.sql` files.*
