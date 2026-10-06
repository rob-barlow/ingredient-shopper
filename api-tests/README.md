# API tests

Black-box tests of the backend's HTTP API (constitution Article IX, plan §10, ADR-016). They're the **refactoring safety net**: they pass or fail purely on HTTP behaviour, so they keep working through Stage 2 (modularise), Stage 3 (split into services) and Stage 5 (rewrite in other languages).

## Running

The backend must be running first (see [docs/local-setup.md](../docs/local-setup.md)). Then, from `api-tests/`:

```powershell
.\mvnw.cmd test "-DADMIN_USERNAME=glenda" "-DADMIN_PASSWORD=..."     # against http://localhost:8081
.\mvnw.cmd test "-DBASE_URL=http://localhost:9000" ...                # somewhere else
.\mvnw.cmd test "-Dtest=HealthApiTest"                                # one class
```

- **`ADMIN_USERNAME` / `ADMIN_PASSWORD`**: the admin account the backend was started with. The password is the **plain** one, whose bcrypt hash is in the backend's config. CI passes CI-only values.
- *`ContractValidationSelfTest` needs no backend. It checks the safety net itself.*

## Rules for writing tests

1. **Extend `ApiTestBase` and start requests with one of its helpers**, never with REST Assured's `given()`:
   - **`api()`**: the base URL, JSON headers, and **contract validation of every request and response**
   - **`asAdmin()`**: `api()` logged in as the admin (one shared token, logged in once)
   - **`withToken(token)`**: `api()` with a specific bearer token, e.g. a fresh session from `AdminLogin.freshToken()`, or a made-up one
   - **`apiSendingInvalidRequest()`**: **only** for tests that deliberately break the contract to prove the backend rejects it (empty required fields, a missing token). The **response** is still checked. Anywhere else it would hide mistakes in the test

   *Set tokens with these helpers, not REST Assured's `auth().oauth2()`, which adds the header too late for the contract check to see it. For endpoints that return no body (e.g. logout's 204), use `.accept(ContentType.ANY)`, as real clients do.*
2. **HTTP only.** No imports from `backend/`, no database access, no knowledge of classes or tables.
3. **Set up data through the API**, e.g. log in as the admin and create products with `POST /v1/admin/products`. Never insert rows directly: the database gets restructured in Stages 2 and 3.
4. **Independent and repeatable.** Each test creates what it needs, with **unique names** (e.g. `"Test flour " + UUID.randomUUID()`), so it never relies on another test or on a previous run. The database keeps data between runs.
5. **Name tests after the acceptance criterion they prove:** `acNNN_MM_whatItShows`, e.g.
   ```java
   @Test
   void ac004_28_adjustmentDoesNotUndoConcurrentSale() { ... }
   ```
   The name is the traceability link from spec to test (Article IX). Setup tests that aren't about a criterion (like `HealthApiTest`) just describe what they check.

## What "contract validation" catches

Every response is checked against `contracts/openapi.yaml`. A test **fails**, even if its own assertions pass, when the backend:

- returns a **status code** the contract doesn't list for that operation
- leaves out a **required** field, or sends a **wrong type** or a value outside an **enum**
- answers on a **path** the contract doesn't describe
- uses a **content type** the contract doesn't list

`ContractValidationSelfTest` proves each of these is really caught.
