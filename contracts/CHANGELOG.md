# Contract changelog

Every change to [`openapi.yaml`](openapi.yaml) is recorded here (constitution Article IIIa).

- **Non-breaking** (new endpoint, new optional field): a **minor** version bump, e.g. 1.0.0 → 1.1.0, staying on `/v1`.
- **Breaking** (removed or renamed field, changed type or meaning): a **major** version bump, e.g. 1.x → 2.0.0, with **new `/v2` paths alongside `/v1`**.
- **Fixes to descriptions or examples only:** a **patch** bump, e.g. 1.0.0 → 1.0.1.

Until v1 is first released (the end of Stage 1), endpoints are added under **Unreleased**.

---

## [1.0.0] - Unreleased

### Added
- Contract skeleton (T-003): info, conventions, local server, `bearerAuth` security scheme.
- Shared `Problem` (RFC 9457) and `FieldError` schemas, and reusable error responses: `BadRequest`, `Unauthorized`, `NotFound`, `Conflict`, `ValidationFailed`.
- `GET /actuator/health` (operational, unversioned).

- **004 (T-011):** admin auth: `POST /v1/auth/login`, `POST /v1/auth/logout` and `GET /v1/auth/me`.
- **004 (T-011):** catalogue reads: `GET /v1/categories` and `GET /v1/products/{id}`.
- **004 (T-011):** admin products: `POST /v1/admin/products` and `PUT /v1/admin/products/{id}`.
- **004 (T-011):** schemas `LoginRequest`, `LoginResult`, `AdminInfo`, `Category`, `UnitMeasure`, `ProductInput`, `NewProduct` and `ProductDetail`, plus the shared `ProductId` path parameter.

### Changed
- Local development server is now `http://localhost:8081` (was 8080, which is often taken on dev machines).
