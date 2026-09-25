# Constitution: Ingredient Shopper

> **Status:** ✅ Agreed (2026-09-23), amendments listed below
> **Last updated:** 2026-09-25

Project-wide **engineering** rules. Every `plan.md` must follow them. If a plan needs to break one, change this document first, deliberately.

Product rules (users, behaviour, principles) live in [`specs/000-overview/spec.md`](specs/000-overview/spec.md), not here.

---

## Architecture roadmap

The architecture evolves in **stages**. Each stage is a learning exercise. The product (specs 000–006) is the **same** in every stage.

| Stage | Architecture | Built by | Timebox |
|---|---|---|---|
| **1** | **Layered monolith**: one backend, organised by technical layer, with one database | **AI** | about a month (Article VIII) |
| **2** | **Modular monolith**: the same backend, reorganised by business area, each module owning its data | **Rob** | none |
| **3** | **Services**: modules extracted one at a time into separate services, in the **same language** as the monolith | **Rob** | none |
| **4a** | **Local infrastructure**: containers, Compose, reverse proxy / load balancer, queues | **Rob** | none |
| **4b** | **Cloud deployment**: provider to be decided (e.g. AWS, Azure, Cloudflare) | **Rob** | none |
| **5** | **Polyglot rewrite**: services rewritten one at a time in **new languages**, against the same contracts | **Rob** | none |

Each stage after 1 gets its own plan and tasks, and its decisions supersede earlier ones through decision records (ADRs).

**Stages 4a and 4b don't have to wait for Stages 2 and 3.** The Stage 1 monolith can be containerised and deployed as soon as it works, and the architecture can then evolve while it runs. 4a comes before 4b, because most cloud platforms run containers.

---

## Articles

### I. Architecture by stage *(amended)*
The system follows the roadmap above. **Stage 1** is a **single backend application**, organised by **technical layer** (e.g. controllers, services, repositories), using **one database**. This layered structure is **deliberate**: reorganising it is Stage 2's learning exercise. The code must still be clean, readable and well tested. It just shouldn't be pre-organised into business modules.

### II. Polyglot by design *(amended)*
This is a learning project. In Stage 1, the **backend** and **frontend** use different languages. In Stage 5, **each service** is rewritten in a **new** language, picked to teach something new. Stage 3 deliberately keeps the monolith's language, so that extracting services and changing languages are learned separately. Each plan records its choices and what they're meant to teach.

### III. Contract-first *(amended)*
The backend's HTTP API is described by a **language-neutral contract** (OpenAPI), written and agreed **before** it is implemented. The frontend relies only on the contract. It shares no code with the backend, although generating types *from* the contract is allowed. In Stage 3, each extracted service gets its own contract in the same way.

### IIIa. API versioning
Every contract is versioned, and changes follow these rules:
- **Non-breaking changes** (adding an optional field or a new endpoint) stay within the current version.
- **Breaking changes** (removing or renaming a field, changing a type or meaning) create a **new major version** (e.g. `v1` → `v2`), exposed alongside the old one.
- An old version is removed only once nothing depends on it.
- Each contract keeps a short **changelog**.

### IV. Runnable from the command line
Every runnable part (in Stage 1, the backend and the frontend) starts with a single documented command on a developer machine. No containers, orchestration, reverse proxies or message brokers are **required**.

### V. Infrastructure-ready, not infrastructure-built
The owner will add infrastructure by hand in Stages 4a and 4b. To make that possible without code changes, or changes for a particular cloud provider:
- Addresses, ports and credentials come from **configuration** (e.g. environment variables), never hard-coded.
- Every runnable part is **stateless** apart from its database, so it could run as multiple copies.
- Every runnable part exposes a **health check**.
- Parts communicate over **standard network protocols** only, never shared memory or shared files.

### VI. Networking is invisible to the product
Nothing a customer or admin sees may depend on how the system is deployed or connected. Architecture and deployment changes must never require a spec change.

### VII. One store frontend
There is a **single web frontend** for customers and admins. It talks to the backend only through the contract (Article III). A future external identity provider (OIDC) may host its own login pages. That does not count as a second store frontend.

### VIII. Respect the timebox
**Stage 1** (the AI-built MVP) should fit into **about a month** (000 §1). So:
- Learning happens in the *language and framework* choices, not in extra moving parts.
- Don't build infrastructure the MVP doesn't need (see V).

Stages 2–5 are the owner's own learning and have no timebox.

### IX. Testing *(amended)*
- **Unit tests are mandatory.** Every task that adds or changes behaviour includes unit tests for it, and a task isn't done until they pass.
- **API-level tests are mandatory.** Every acceptance criterion that can be observed through the API has a test that calls the **real HTTP API** and checks the response. These tests **must not depend on internal structure**, so they keep passing through Stages 2, 3 and 5. They are the refactoring safety net.
- Tests are traceable: each test names the acceptance criterion it proves (e.g. `004 AC-28`).
- **Contract tests (recommended):** the backend checks that its real responses match the published contract.
- **Browser-driven end-to-end tests are out of scope for now** *(backlog)*.

### X. Spec first
No code is written until the relevant `spec.md` is **Agreed** and a `plan.md` and `tasks.md` exist. This applies to the owner's Stage 2–5 work too. When the implementation reveals a spec is wrong, update the spec, then the code.

### XI. Task ownership *(new)*
Every task in a `tasks.md` has an **owner**: `AI` or `Rob`.
- **AI-owned tasks:** the AI writes the code and tests, following the plan.
- **Rob-owned tasks:** the AI **explains, reviews and answers questions**, and helps write the plan and tasks. It **doesn't write the code**, unless Rob explicitly asks for a specific piece.
- In GitHub, ownership is shown with the labels `owner:ai` and `owner:rob`.

### XII. One task, one branch, one PR *(new)*
- Every task is done on its **own branch**, named `t-NNN-short-name` (e.g. `t-021-product-list`), and delivered as **one pull request**.
- The PR says which task and acceptance criteria it implements, and closes its issue (`Closes #12`).
- **CI must pass** before a PR can be merged. `main` is protected: no direct pushes.
- **Only Rob merges.** The AI may create branches, commit, push its task branches and open PRs, but it **never merges** and **never pushes to `main`**.
- Rob reviews AI PRs, and the AI reviews Rob's PRs.
- Spec, plan and constitution changes go through PRs too.

---

## Amendments

| # | Date | Change |
|---|---|---|
| 1 | 2026-09-24 | Staged architecture (layered monolith first). Articles I, II, III, IV, V, VIII, IX amended; Article XI added; roadmap added. |
| 2 | 2026-09-24 | Article XII added: branch and PR per task, CI required, only Rob merges. |
| 3 | 2026-09-25 | Stage 4 split into 4a (local infrastructure) and 4b (cloud deployment, provider undecided). Stages 4a/4b may start before Stages 2 and 3. Article V wording updated. |

---

## Open questions
*None at present.*
