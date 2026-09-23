# Constitution: Ingredient Shopper

> **Status:** ✅ Agreed (2026-09-23)
> **Last updated:** 2026-09-23

Project-wide **engineering** rules. Every feature's `plan.md` must follow them. If a plan needs to break one, change this document first, deliberately.

Product rules (users, behaviour, principles) live in [`specs/000-overview/spec.md`](specs/000-overview/spec.md), not here.

---

## Articles

### I. Microservices
The system is built as a set of **independent services**, each owning one business area and **its own data**. A service never reads another service's data directly. It asks that service through its public interface.

### II. Polyglot by design
This is a learning project. **Each service uses a different language and framework**, picked to teach something new. Each service's `plan.md` records the choice and what it is meant to teach.

### III. Contract-first
Because services are written in different languages, they agree through **language-neutral API contracts** (e.g. OpenAPI), not shared code. A service's contract is written and agreed **before** its implementation. No shared libraries between services.

### IIIa. API versioning
Every contract is versioned, and changes follow these rules:
- **Non-breaking changes** (adding an optional field or a new endpoint) stay within the current version.
- **Breaking changes** (removing or renaming a field, changing a type or meaning) create a **new major version** (e.g. `v1` → `v2`), exposed alongside the old one.
- An old version is removed only once nothing depends on it.
- Each contract keeps a short **changelog**.

### IV. Runnable from the command line
Every service starts with a single documented command on a developer machine. No containers, orchestration, reverse proxies or message brokers are **required** to run the MVP.

### V. Infrastructure-ready, not infrastructure-built
The owner will add Docker, Kubernetes, queues, nginx and similar later, **by hand, as a separate learning exercise**. To make that possible without code changes:
- Service addresses, ports and credentials come from **configuration** (e.g. environment variables), never hard-coded.
- Each service is **stateless** apart from its own data store, so it could run as multiple copies.
- Each service exposes a **health check**.
- Services communicate over **standard network protocols** only, never shared memory or shared files.

### VI. Networking is invisible to the product
Nothing a customer or admin sees may depend on how services are deployed or connected. Deployment changes must never require a spec change.

### VII. One store frontend
There is a **single web frontend** for customers and admins. It talks to the services only through their contracts (Article III). A future external identity provider (OIDC) may host its own login pages. That does not count as a second store frontend.

### VIII. Respect the timebox
The MVP should fit into **about a month** (see 000 §1). So:
- Keep the **number of services small**. Split by business area, not by technical layer.
- Learning happens in the *language and framework* choices, not in extra moving parts.
- Don't build infrastructure the MVP doesn't need (see V).

### IX. Testing
- **Unit tests are mandatory.** Every task that adds or changes behaviour includes unit tests for it, and a task isn't done until they pass.
- Tests are traceable: acceptance criteria in a `spec.md` should map to tests that prove them.
- **Contract tests (recommended):** each service checks that its real responses match its published contract, to catch drift between contract and code.
- **End-to-end tests are out of scope for now.**

### X. Spec first
No code is written for a feature until its `spec.md` is **Agreed** and its `plan.md` and `tasks.md` exist. When the implementation reveals the spec is wrong, update the spec, then the code.

---

## Open questions
*None at present.*
