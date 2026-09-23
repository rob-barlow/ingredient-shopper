# 000 — Product Overview: Ingredient Shopper

> **Status:** ✅ Agreed (2026-09-23)
> **Type:** Overview (shared context for all feature specs)
> **Last updated:** 2026-09-23

This document contains no technology decisions. It describes *what* the product is and *why*. *How* it is built belongs in [`constitution.md`](../../constitution.md) (project-wide engineering rules) and each feature's `plan.md`.

---

## 1. Purpose

Glenda's Groceries is a fictional online grocery store. **Ingredient Shopper** is its new online store. It has two jobs:

1. **Standard grocery shopping:** customers browse products, fill a basket and place an order for collection.
2. **Recipe-driven shopping:** a customer pastes a link to a recipe and the store fills their basket with the ingredients for it, handling anything out of stock by offering substitutes.

Admins run the store: they manage the product catalogue, stock levels and incoming orders.

**Project context:** This is a learning and portfolio project. It's a demonstration piece, not a store expected to see real, regular use. The priority is a small, well-specified product built with spec-driven development, not feature breadth. A playful tone is fine.

**Timebox:** A working MVP (features 000–006) should be achievable in **about a month**. Every spec and plan should be sized to fit that. If a requirement threatens the timebox, simplify it or move it to Future.

---

## 2. Users

| Role | Description | In v1? |
|---|---|---|
| **Guest customer** | Everyone who opens the store is a guest straight away, with no sign-up step. Can browse, build a basket, import a recipe and place an order. | ✅ |
| **Admin** | Store staff. Signs in through the store's login option. Manages products, stock and orders. v1 has **exactly one admin account**. | ✅ |
| **Registered customer** | A customer who logs in. When this arrives, multiple admin accounts arrive with it. | ❌ Future |

---

## 3. Glossary

These terms have exactly these meanings in every spec.

| Term | Meaning |
|---|---|
| **Product** | One kind of item the store sells, e.g. "Semi-skimmed milk, 2 pints". Products are unbranded and there is one of each kind (no premium/value variants). |
| **Unit** | The amount you get when you buy one of a product, e.g. "2 pints", "500g", "each". Every product has one. *(Amendment 1)* |
| **Category** | A grouping of products, e.g. Dairy, Bakery, Fruit & Veg. Each product belongs to exactly one category. |
| **Catalogue** | All products the store lists. |
| **Stock level** | How many units of a product are available to sell. |
| **In stock** | Stock level is greater than zero. |
| **Basket** | The products and quantities a customer intends to buy, before placing an order. A basket does **not** reserve stock. |
| **Order** | A basket the customer has committed to. Stock is taken when the order is placed. Has a status (see feature 005). |
| **Collection** | The customer picks up the order from the store. The only fulfilment method in v1. |
| **Collection name** | The name a guest gives when placing an order. |
| **Secret code** | A word or phrase the guest chooses when placing an order and says at collection. It doesn't have to be unique. |
| **Dabloons** | The store's currency. Shown with the **£** symbol. Whenever the currency is named in words, it is "dabloons". |
| **Recipe import** | Giving the store a recipe URL so it can propose basket items from the recipe's ingredients. |
| **Ingredient** | One line of a recipe, e.g. "200g plain flour". |
| **Match** | The product the store picks to satisfy an ingredient. |
| **Substitute** | An alternative in-stock product offered when the matched product is out of stock. |
| **Portions** | The number of servings a recipe import is scaled to. Fixed at **2** in v1. |

---

## 4. Product principles

These guide decisions in every feature spec. When a spec leaves a question open, fall back on these.

1. **Bare bones, and done in about a month.** Build only what the features require. When unsure, leave it out.
2. **The customer stays in control.** The store may *propose* (matches, quantities, substitutes), but it never swaps one product for another without the customer choosing to.
3. **Stock is the truth, and it is checked at order time.** A customer can't add more of a product to their basket than is currently in stock, but a basket doesn't hold stock for them. When an order is placed, stock is checked again. If anything is no longer available, the customer is told what changed before the order goes through.
4. **Guest first.** Nothing in v1 needs a customer account.
5. **Leave room for the future.** Future features (section 7) are not built, but v1 decisions shouldn't block them.

---

## 5. Shared rules

Decisions that affect more than one feature.

### 5.1 Access
- Every visitor is a guest. A **login option** is visible to everyone.
- In v1 the only account that can log in is the single **admin** account. Logging in gives access to the admin features (004, 005).
- Guests can never see or use admin features.
- Customers cannot sign up in v1.

### 5.2 Collection and secret codes
- When placing an order, a guest gives a **collection name** and a **secret code**.
- At collection, the admin looks up **name + secret code** and sees **every** uncollected order that matches.
- Codes are not unique. If two people happen to pick the same name and code, the admin sees both orders and picks out the right one.
- *(Future: a registered customer has a secret code linked to their account, and all their orders are handed over together at collection.)*

### 5.3 Money
- All prices are in **dabloons**, shown as **£** (e.g. £1.20).
- No tax is charged or shown.

### 5.4 Product images
- **Every product has an image.**
- Images come from **free-to-use stock image sources**, found by searching the product's plain name (e.g. "milk").
- If an image is missing or fails to load, a **placeholder** is shown instead. A product never appears without some image.

### 5.5 Categories
*(Amendment 1, 2026-09-23, from 001.)*
- Categories are a **fixed list**, defined by the seed data.
- In v1, admins **choose from** the list when creating or editing a product. They can't add, rename or delete categories.

---

## 6. Feature map (v1)

| # | Feature | Summary | Depends on |
|---|---|---|---|
| 000 | Product overview | This document | — |
| 001 | Browse catalogue | View products by category and search for them | — |
| 002 | Shopping basket | Add, change and remove products in a basket | 001 |
| 003 | Place order | Turn a basket into an order for collection (with stock re-check) | 002 |
| 004 | Admin: inventory & stock | Admin login; create and edit products, set stock levels | — |
| 005 | Admin: order management | View orders, look them up by name + code, move them through their statuses | 003, 004 |
| 006 | Recipe import | Recipe URL → proposed basket items, with substitutes for anything out of stock | 001, 002 |

Each feature gets its own folder `specs/NNN-name/` with `spec.md`, then `plan.md` and `tasks.md`.

---

## 7. Future (named, not specified)

*(Amendment 2, 2026-09-23: this list moved to the backlog.)*

Future features and nice-to-haves live in **[`backlog.md`](../../backlog.md)**, each with a priority and its source. **No detail is specified there on purpose.** Each item gets its own spec when it's picked up.

---

## 8. Out of scope

Not planned at all, unless a later spec explicitly brings it in:

- Real payment processing
- Tax
- Reserving stock while items sit in a basket
- Branded products, or several variants of the same product
- Promotions, discounts and loyalty schemes
- Product reviews and ratings
- Email or SMS notifications
- Multiple stores or locations
- Anything not listed in section 6 or 7

---

## 9. Seed data

For development and testing, the store is pre-loaded with a catalogue:

- **At least 50 products** across a spread of common supermarket categories.
- Each product is a generic, **unbranded**, AI-generated item (name, description, price, unit) with an image following §5.4.
- **One product per kind** (e.g. one "Plain flour", not several).
- Stock levels are varied so that some products are **out of stock**. This is needed to test substitutes in feature 006.
- The catalogue should cover the ingredients of several common recipes, so recipe import can be demonstrated end to end.

---

## 10. Open questions

*None at present.*
