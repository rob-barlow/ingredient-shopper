# 004 — Admin: Inventory & Stock · Feature plan

> **Status:** Draft
> **Implements:** [spec.md](spec.md) (agreed)
> **Follows:** [architecture plan](../000-overview/plan.md) and the [constitution](../../constitution.md)
> **Last updated:** 2026-10-01

The *how* for 004. Anything cross-cutting (money in pence, Problem errors, the layered packages, tests) is already decided in the architecture plan, so this plan only covers what's specific to 004.

---

## 1. Build-order consequences

004 is built **first**, before 001–003, because API tests create their data through 004's admin endpoints (setup tasks). So:

**004 also builds two small pieces that 001 would otherwise own,** because the admin controls live on them:

| Piece | Spec it belongs to | Why it's built in 004 |
|---|---|---|
| `GET /v1/categories` | 001 AC-10 | The product form needs the category list (004 AC-18) |
| `GET /v1/products/{id}` and the **product detail page** | 001 AC-23–27 | Admin controls live on the detail page (004 AC-10), and API tests need it to check a created product "appears in the store" (004 AC-19) |

001 later **reuses** these and adds the product list, search and pagination.

**Some 004 criteria can only be fully verified later:**

| AC | What's missing in 004 | Verified when | Until then |
|---|---|---|---|
| AC-9 (Add product on the list) | The product list (001) | 001 | **Add product** sits on the Home page placeholder |
| AC-11 (admin uses basket/orders) | Basket and orders (002, 003) | 002, 003 | n/a |
| AC-19 "appears in the store" | The list (001) | 001 | Checked via `GET /v1/products/{id}` and the detail page |
| AC-23 (price change in baskets/orders) | 002, 003 | 002, 003 | The price update itself is tested in 004 |
| AC-28 (adjustment vs a guest's order) | Orders (003) | 003 | The **same guarantee** is tested with two **concurrent adjustments**, which must both count |
| AC-31 "updates everywhere" | The list (001) | 001 | The detail page shows the new level |

These are listed again in the relevant later feature's tasks, so they can't be forgotten.

---

## 2. Contract additions (`contracts/openapi.yaml`, v1)

New tags: `auth`, `catalogue`, `admin`, `stock`. All error responses use the existing `Problem` responses.

*`stock` is its own tag (decided while writing tasks.md):* it generates a separate `StockApi` interface. That lets the stock adjustment's contract and test-first API tests be merged **before** the endpoint exists. An unimplemented interface is fine, but adding the operation to `AdminApi` would break the build of the existing `AdminProductsController` (`skipDefaultInterface`).

### Endpoints

| Operation | `operationId` | Security | Responses | ACs |
|---|---|---|---|---|
| `POST /v1/auth/login` | `login` | public | 200 `LoginResult` · 401 · 422 | AC-2–4 |
| `POST /v1/auth/logout` | `logout` | bearer | 204 · 401 | AC-7 |
| `GET /v1/auth/me` | `getCurrentAdmin` | bearer | 200 `AdminInfo` · 401 | AC-5, AC-6 |
| `GET /v1/categories` | `listCategories` | public | 200 `Category[]` | (001 AC-10) |
| `GET /v1/products/{id}` | `getProduct` | public | 200 `ProductDetail` · 404 | AC-19, AC-22 (001 AC-23–27) |
| `POST /v1/admin/products` | `createProduct` | bearer | 201 `ProductDetail` · 401 · 422 | AC-13–20 |
| `PUT /v1/admin/products/{id}` | `updateProduct` | bearer | 200 `ProductDetail` · 401 · 404 · 422 | AC-13–17, AC-21–22 |
| `POST /v1/admin/products/{id}/stock-adjustments` | `adjustStock` | bearer | 200 `StockLevel` · 401 · 404 · **409** · 422 | AC-25–31 |

### Schemas

| Schema | Fields |
|---|---|
| `LoginRequest` | `username`, `password` (both required strings) |
| `LoginResult` | `token`: the opaque session token (ADR-006) |
| `AdminInfo` | `username` |
| `Category` | `id` (integer), `name` |
| `ProductDetail` | `id` (uuid), `name`, `description`, `category` (`Category`), `unitAmount` (number), `unitMeasure` (enum `g kg ml l each`), `pricePence` (integer), `imageUrl`, `stockLevel` (integer) |
| `ProductInput` | `name`, `description`, `categoryId`, `unitAmount`, `unitMeasure`, `pricePence`, `imageUrl`. Used by **create and edit** |
| `NewProduct` | `ProductInput` **plus** `startingStock` (integer ≥ 0). Used by **create only** (AC-21: no stock on edit) |
| `StockAdjustment` | `delta`: a non-zero integer |
| `StockLevel` | `stockLevel` (integer) |
| `InsufficientStockProblem` | `Problem` **plus** `currentStockLevel` (integer). Returned as the 409 for AC-30/30a |

**Validation is written into the schemas** wherever OpenAPI can express it: `maxLength: 50` / `1000`, a printable-ASCII `pattern`, `pricePence` `minimum: 1` and `maximum: 999999`, `unitAmount` `exclusiveMinimum: 0`, the `^https?://` pattern for `imageUrl`, and so on. The generated Java then validates these automatically (`useBeanValidation`). Rules OpenAPI **can't** express are checked in the service: name uniqueness (AC-14/15), at most 2 decimal places on `unitAmount` (AC-16a), `categoryId` existing, and `delta ≠ 0` (AC-29).

**Duplicate names** come back as **422** with a field error on `name` ("Already in use"), not 409, so the form shows it next to the Name field like any other validation error (AC-13, AC-14).

---

## 3. Database (Flyway)

### `V2__products.sql`

```sql
CREATE TABLE products (
    id            uuid          PRIMARY KEY,
    name          text          NOT NULL CHECK (length(name) BETWEEN 1 AND 50),
    name_key      text          GENERATED ALWAYS AS (lower(trim(name))) STORED UNIQUE,  -- AC-14
    description   text          NOT NULL CHECK (length(description) BETWEEN 1 AND 1000),
    category_id   integer       NOT NULL REFERENCES categories(id),
    unit_amount   numeric(9,2)  NOT NULL CHECK (unit_amount > 0),
    unit_measure  text          NOT NULL CHECK (unit_measure IN ('g','kg','ml','l','each')),
    price_pence   integer       NOT NULL CHECK (price_pence BETWEEN 1 AND 999999),
    image_url     text          NOT NULL,
    stock_level   integer       NOT NULL CHECK (stock_level >= 0)                       -- AC-30
);
```

- **`name_key` is generated by the database** from `name`, so the "unique ignoring case and spaces" rule (AC-14) is enforced **by PostgreSQL itself**, even if a bug skipped the service check, or two saves raced. The service still checks first, to give a friendly field error.
- **`stock_level >= 0`** is the last line of defence for AC-30. The conditional update (§4) means it's never actually hit.

### `V3__admin_sessions.sql`

```sql
CREATE TABLE admin_sessions (
    token_hash  text         PRIMARY KEY,     -- SHA-256 of the token, never the token itself
    created_at  timestamptz  NOT NULL DEFAULT now()
);
```

---

## 4. Backend design (layered, Article I)

| Layer | Classes | Notes |
|---|---|---|
| `controller/` | `AuthController implements AuthApi` · `CatalogueController implements CatalogueApi` · `AdminProductsController implements AdminApi` · `StockController implements StockApi` *(Rob)* | Thin: they map generated models ↔ service calls. No rules here |
| `service/` | `AuthService` · `ProductService` · `StockService` | All the business rules and `@Transactional` boundaries |
| `repository/` | `ProductRepository` · `CategoryRepository` · `AdminSessionRepository` | Spring Data JPA. `ProductRepository` gets the conditional stock update |
| `entity/` | `ProductEntity` · `CategoryEntity` · `AdminSessionEntity` | `name_key` is mapped **read-only** (the database generates it) |
| `security/` | `AdminSessionFilter` · `ProblemAuthenticationEntryPoint` | See below |
| `config/` | `SecurityConfig` (updated) · `ShopperProperties` (+ `admin`) | |

### Admin login (ADR-006)
1. `POST /v1/auth/login`: `AuthService` checks the username, and checks the password with **`BCryptPasswordEncoder`** against `ADMIN_PASSWORD_BCRYPT`.
   - It **always** runs the bcrypt check, even when the username is wrong, so a wrong username and a wrong password take the same time. The response is also identical: `401 "Wrong username or password"` (AC-3). That stops anyone discovering the username by timing the responses.
2. On success, it creates a token: **32 random bytes** from `SecureRandom`, Base64URL-encoded. It stores **SHA-256(token)** in `admin_sessions`, and returns the token once.
3. **`AdminSessionFilter`** runs on every request. If there's an `Authorization: Bearer …` header, it hashes the token and looks it up. If found, the request is authenticated as `ROLE_ADMIN`.
4. **`SecurityConfig`**: `/v1/admin/**`, `/v1/auth/logout` and `/v1/auth/me` require `ROLE_ADMIN`. Everything else is public. A failure returns **401 Problem JSON**, via `ProblemAuthenticationEntryPoint` (AC-8).
5. `POST /v1/auth/logout` deletes the row, so the token is dead immediately (AC-7).

New configuration: `shopper.admin.username` / `shopper.admin.password-bcrypt`, from `ADMIN_USERNAME` / `ADMIN_PASSWORD_BCRYPT` (already in `.env.example`).

### Products
- **Create:** validate → check `categoryId` exists and the name is free (case and space insensitive) → insert with a new random UUID and `stockLevel = startingStock`.
- **Edit:** the same checks, but the name check **excludes the product itself** (AC-15). `stockLevel` is **never** touched (AC-21).
- **422 field errors** go through the existing `GlobalExceptionHandler`. Service-level rule failures throw a small `FieldValidationException(field, message)` that the handler turns into the same 422 shape.

### Stock adjustment *(Rob's task, following the product endpoints as the example)*
The relative update that makes AC-28 true (ADR-008):
```sql
UPDATE products SET stock_level = stock_level + :delta
WHERE id = :id AND stock_level + :delta >= 0
```
- **1 row updated:** success, so return the new level.
- **0 rows:** either the product doesn't exist (**404**), or the result would go below zero (**409** `InsufficientStockProblem` with `currentStockLevel`, AC-30/30a). The service tells them apart with one lookup.
- `delta == 0` → **422** (AC-29). A non-integer is rejected by the contract's `type: integer`.

Because the database applies `+ delta` to *whatever the current value is*, a concurrent sale or adjustment can never be overwritten. That's AC-28, with no locks needed.

---

## 5. Frontend design (Blazor)

| Page / piece | Route | ACs |
|---|---|---|
| **Header**: Log in / Log out | (every page) | AC-1, AC-6, AC-7, AC-12 |
| **Login page** | `/login` | AC-2–4 |
| **Product detail page**: image (with placeholder), name, unit, price, description, stock, plus **Edit product** for the admin | `/products/{id}` | AC-10, AC-19 (001 AC-22–27) |
| **Product form**: shared by create and edit | `/admin/products/new`, `/admin/products/{id}/edit` | AC-13–24 |
| **Add product** button: on the Home placeholder until 001 | `/` | AC-9 (interim), AC-18 |
| **Adjust stock** control: on the detail page | `/products/{id}` | AC-25–31 *(built after Rob's endpoint)* |

- **`AdminSession` service:** keeps the token in `localStorage` (via JavaScript interop), so it survives a reload (AC-5). On startup, it checks the token with `GET /v1/auth/me`, and forgets it if the backend says 401 (e.g. after a logout in another tab).
- **A `DelegatingHandler`** adds `Authorization: Bearer …` to the generated client's requests when logged in.
- **Showing errors:** the form maps `Problem.errors[]` to fields by name, and shows each message next to its field (AC-13). The form keeps whatever was typed (AC-17).
- **Money input:** the form takes **pounds** ("1.20") and converts to **pence** for the API, with a `Money.TryParse` helper next to `Money.Format`.
- **Hidden admin controls are convenience only.** Security is enforced on the server (AC-8).

---

## 6. Tests (Article IX)

| Level | What |
|---|---|
| **Unit (backend)** | `AuthService` (right/wrong username/password, same failure for both, token hashing), `ProductService` (validation, the duplicate-name rule including "itself" on edit), `StockService` (success, 404 vs 409, delta 0) with mocked repositories |
| **Unit (frontend)** | `Money.TryParse`; bUnit: the header shows Log in/Log out by session state, the product form shows field errors from a 422 and keeps input, the detail page shows the admin controls only for the admin |
| **API (black-box)** | One test per API-visible AC, named after it, e.g. `ac004_3_wrongUsernameOrPasswordGiveSameMessage`, `ac004_8_guestCannotCreateProduct`, `ac004_14_duplicateNameIgnoringCaseAndSpaces`, `ac004_28_concurrentAdjustmentsBothCount`, `ac004_30a_rejectionShowsCurrentLevel`. A shared `AdminLogin` helper logs in for tests that need it |

**CI's admin account:** `ADMIN_USERNAME` / `ADMIN_PASSWORD_BCRYPT` are added to the `api-tests` job, with a CI-only password. The API tests receive the matching plain password as `-DADMIN_PASSWORD`. As with the database password, it's not a secret: it only exists in a throwaway CI run.

---

## 7. Task outline (detailed in `tasks.md`)

1. Contract: auth and catalogue endpoints, then admin product endpoints *(AI)*
2. Migrations V2 and V3, entities and repositories *(AI)*
3. Admin login: `AuthService`, the session filter, security config, plus unit and API tests *(AI)*
4. Categories and product read endpoints, plus tests *(AI)*
5. Create and edit product endpoints, plus tests *(AI)*. **This is Rob's worked example**
6. Contract for stock adjustment *(AI)*, with its **API tests written first and `@Disabled`** *(AI)*
7. **Stock adjustment endpoint and unit tests *(Rob)*.** Rob's PR removes `@Disabled`, and the API tests must pass
8. Frontend: session, login page, header *(AI)*
9. Frontend: product detail page, product form, Add product *(AI)*
10. Frontend: Adjust stock control *(AI, after task 7)*

---

## 8. Decisions

1. **Test-first for Rob's task** *(decided 2026-10-01)*: the stock adjustment's API tests (AC-25 to AC-31) are written **before** the endpoint, and committed with `@Disabled("Enabled by T-0NN")`. Rob's PR removes `@Disabled`, and CI must pass. That's test-driven development: the tests are the finish line.

## 9. Open questions

*None at present.*
