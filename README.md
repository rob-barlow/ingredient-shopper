# Ingredient Shopper

An online grocery store for the fictional **Glenda's Groceries**, where you can paste a **recipe link** and get its ingredients proposed for your basket, with substitutes for anything out of stock.

It's a learning and portfolio project, built with **spec-driven development (SDD)**: every feature is specified, planned and broken into tasks **before** any code is written.

## Start here

| Document | What it is |
|---|---|
| [constitution.md](constitution.md) | Project-wide engineering rules, and the staged architecture roadmap |
| [specs/000-overview/spec.md](specs/000-overview/spec.md) | The product: users, glossary, principles, shared rules |
| [specs/000-overview/plan.md](specs/000-overview/plan.md) | The architecture: stack, data model, API, decisions (ADRs) |
| [specs/](specs/) | One folder per feature (`spec.md`, `plan.md`, `tasks.md`) |
| [backlog.md](backlog.md) | Future ideas, not yet specified |
| [Project board](https://github.com/users/rob-barlow/projects/1) | Live task status |

## Features (v1)

| # | Feature |
|---|---|
| 001 | Browse the catalogue by category and name search |
| 002 | Shopping basket that stays within stock |
| 003 | Place an order for collection, with a name and secret code |
| 004 | Admin: products and stock adjustments |
| 005 | Admin: orders and collection lookup |
| 006 | **Recipe import**: recipe URL → reviewed basket items, using the Claude API |

## Stack (Stage 1)

**Java + Spring Boot** backend (layered monolith) · **Blazor WebAssembly** frontend · **PostgreSQL** · **OpenAPI** contract-first · **Claude API**

## Running locally

Follow **[docs/local-setup.md](docs/local-setup.md)** to install Java 25, .NET 10 and PostgreSQL 18, create the databases and fill in `.env`. Then:

```powershell
cd backend; .\mvnw.cmd spring-boot:run          # backend  → http://localhost:8081/actuator/health
cd frontend/Shopper.Web; dotnet run              # frontend → http://localhost:5000
```

## How work happens

One task → one branch → one pull request → CI → merge. See constitution Article XII.
