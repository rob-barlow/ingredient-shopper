# Backlog: Ingredient Shopper

Everything **not** in the v1 MVP (000 §6) that we might want later. This is a list of ideas, **not specs**: nothing here is specified until it gets its own spec folder.

**Priority:** 🔴 High · 🟡 Medium · 🟢 Low
**Kind:** *Feature* (planned, will get a spec) · *Nice to have* (only if time allows) · *Engineering* (how we build, not what)

| Item | Kind | Priority | Source |
|---|---|---|---|
| **007:** Shopping-list image → basket | Feature | 🟡 | Brief (optional) |
| **008:** Admin assist: prefill a new product's details from its name | Feature | 🟡 | Brief (extension) |
| Customer accounts and login (likely an external OIDC provider), with multiple admin accounts | Feature | 🟡 | 000 |
| Secret code linked to a customer account, so all their orders are handed over together | Feature | 🟢 | 000 §5.2 |
| Basket shared across devices (needs customer accounts) | Feature | 🟢 | 002 |
| Simulated (fake) payment at checkout | Feature | 🟢 | 000 |
| Delivery to an address, as an alternative to collection | Feature | 🟡 | 000 |
| Scaling a recipe import to a chosen number of servings | Feature | 🟡 | 000, 006 |
| Recipe import: when partly in stock, offer the available amount **plus** a substitute for the rest | Nice to have | 🟢 | 006 |
| Live progress while a recipe imports (e.g. "found 8 ingredients…") | Nice to have | 🟢 | 006 |
| Sorting options in the product list (price, etc.) | Nice to have | 🟢 | 001 |
| Admins can delete products | Nice to have | 🟢 | 002 |
| Clean up abandoned baskets | Nice to have | 🟢 | 002 §7 |
| Separate checkout page | Nice to have | 🟢 | 003 |
| Warn the guest when prices changed since they loaded the basket | Nice to have | 🟢 | 003 |
| Admin can cancel an order and return its stock. *Revisit 005's "any status to any status" rule when doing this.* | Feature | 🟢 | 005 |
| **Stage 2:** modularise the monolith (owner: Rob) | Engineering | 🟡 | Constitution roadmap |
| **Stage 3:** extract services, same language (owner: Rob) | Engineering | 🟡 | Constitution roadmap |
| **Stage 4a:** local infrastructure: Docker, Compose, nginx as reverse proxy and load balancer, queues (owner: Rob) | Engineering | 🟡 | Constitution roadmap |
| **Stage 4b:** cloud deployment, provider undecided (AWS / Azure / Cloudflare). See *Cloud notes* below (owner: Rob) | Engineering | 🟡 | Constitution roadmap |
| **Stage 5:** rewrite services in new languages (owner: Rob) | Engineering | 🟡 | Constitution roadmap |
| Browser-driven end-to-end tests | Engineering | 🟢 | Constitution IX |

*When an item is picked up, give it a spec folder and move it out of this list.*

---

## Cloud notes (for Stage 4b)

Decisions to make when Stage 4b starts. Nothing here is decided.

- **Provider:** AWS, Azure or Cloudflare. Check each one's *current* free tier and credit terms for your account first (AWS changed its free tier in July 2025).
- **Cost safety, on day one:** budget alerts (e.g. at $1 and $10), free-tier usage alerts, and tearing down after each session. Infrastructure as code (Terraform, or the provider's own tool) makes teardown one command.
- **Common surprise costs:** managed load balancers, NAT gateways, public IPv4 addresses, resources forgotten in another region, and outbound data.
- **Cheap learning shape:** frontend as static files on a CDN; one small VM running Docker Compose with **nginx as the load balancer** in front of two backend copies (proving Article V's statelessness); database in a **private** subnet; backend in a public subnet with a tight firewall, so no NAT is needed at all. A self-managed NAT instance is an optional extra exercise.
- **Security:** keep plan §7's SSRF protection. Cloud metadata endpoints (e.g. `169.254.169.254`) are the classic target. Secrets go in the provider's secret store, never in images or the repo.
- **Optional:** on AWS, Claude is also available through **Amazon Bedrock**, which would be a small self-contained exercise.
