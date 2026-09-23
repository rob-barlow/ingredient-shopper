# 004 — Admin: Inventory & Stock

> **Status:** ✅ Agreed (2026-09-23)
> **Depends on:** 000, 001
> **Last updated:** 2026-09-23

The single **admin** logs in and uses the **same store** as guests, with extra admin-only controls. They can create products, edit any product field and **adjust** stock levels. There is no separate admin area. Admins can't delete or hide products (000 §8, 002). Setting stock to 0 is how a product stops being sold.

Terms in **bold** are defined in the [000 glossary](../000-overview/spec.md#3-glossary), the [001 terms](../001-browse-catalogue/spec.md#1-terms-used-in-this-spec), or §1 below.

---

## 1. Terms used in this spec

| Term | Meaning |
|---|---|
| **Logged in as admin** | The browser has signed in with the admin account. Lasts until **Log out**. |
| **Admin controls** | Buttons and forms only shown when logged in as admin (e.g. Add product, Edit, Adjust stock). |
| **Product form** | The form for creating or editing a product's fields. |
| **Stock adjustment** | A **relative** change to a stock level, e.g. **+24** (delivery arrived) or **−3** (damaged). The admin never types an absolute stock level for an existing product. |
| **Starting stock** | The stock level a **new** product is created with. |

---

## 2. User stories

| ID | Story |
|---|---|
| **US-1** | As the admin, I want to log in and out, so that only I can change the catalogue. |
| **US-2** | As the admin, I want to use the normal store with extra controls, so that I see exactly what customers see. |
| **US-3** | As the admin, I want to add new products, so that the store can sell them. |
| **US-4** | As the admin, I want to edit any product's details, so that I can fix mistakes and change prices. |
| **US-5** | As the admin, I want to adjust stock up or down, so that stock levels match what's on the shelves, without accidentally undoing sales. |

---

## 3. Acceptance criteria

### Login and logout (US-1)
| ID | Given | When | Then |
|---|---|---|---|
| **AC-1** | any visitor | any page is shown | a **Log in** option is visible (000 §5.1) |
| **AC-2** | the login form | a username and password are entered and submitted | if they match the admin account, the browser is **logged in as admin** and returns to the store |
| **AC-3** | the login form | the username **or** password is wrong | a message says **"Wrong username or password"**, without saying which one. There's no lockout, however many attempts |
| **AC-4** | the login form | either field is empty | the form isn't submitted, and a message says both are required |
| **AC-5** | logged in as admin | the browser is reloaded, or closed and reopened | they are **still** logged in as admin |
| **AC-6** | logged in as admin | any page is shown | a **Log out** option is visible instead of Log in |
| **AC-7** | logged in as admin | they press Log out | they are treated as a **guest** again. No admin controls are shown |
| **AC-8** | a guest | they try to perform any admin action by any means (e.g. going to a URL directly, or calling the system directly) | the action is **refused** and nothing changes |

### Admin view of the store (US-2)
| ID | Given | When | Then |
|---|---|---|---|
| **AC-9** | logged in as admin | the product list is shown | it looks and behaves **exactly** as in 001 (filters, search, pagination), plus an **Add product** button |
| **AC-10** | logged in as admin | a product detail page is shown | it shows everything from 001 and 002, plus **Edit product** and **Adjust stock** controls |
| **AC-11** | logged in as admin | they use the basket or place an order | it works exactly as for a guest (002, 003) |
| **AC-12** | a guest | any page is shown | **no** admin controls are visible |

### Product fields and validation (US-3, US-4)
These rules apply whenever the **product form** is saved, for both create and edit. **Every field is required.**

| Field | Rule |
|---|---|
| **Name** | 1–50 characters of printable ASCII. **Unique**, ignoring case and leading/trailing spaces. |
| **Description** | 1–1000 characters of printable ASCII. |
| **Category** | Chosen from the fixed category list (000 §5.5). |
| **Unit amount** | A **positive** number, with at most 2 decimal places (e.g. 500, 1.5, 6). |
| **Unit measure** | Chosen from the fixed list: **g, kg, ml, l, each** (000 glossary). |
| **Price** | A **positive** amount in £ with **at most 2 decimal places** (e.g. 1.20, 0.5, 3), no more than **£9,999.99**. Zero and negative prices are rejected. |
| **Image URL** | A web address beginning `http://` or `https://`. The store doesn't check that it loads, since a placeholder covers broken images (000 §5.4). |
| **Starting stock** | *Create only.* A whole number, **0 or more**. |

| ID | Given | When | Then |
|---|---|---|---|
| **AC-13** | any field breaks its rule | the form is saved | nothing is saved, and **each** invalid field shows its own message |
| **AC-14** | a product named "Plain flour" exists | the admin creates or renames a product to "plain FLOUR" or " Plain flour " | it is rejected with a message that the name is already in use |
| **AC-15** | a product is being edited | its name is saved **unchanged** | this doesn't count as a duplicate of itself |
| **AC-16** | a price of 1.234, −1, 0 or 10000 | the form is saved | it's rejected with a message about the price rule |
| **AC-16a** | a unit amount of 0, −1 or 1.234, or no measure chosen | the form is saved | it's rejected with a message about the unit rule |
| **AC-17** | any rejected save | the form is shown again | everything the admin typed is **kept** |

### Creating a product (US-3)
| ID | Given | When | Then |
|---|---|---|---|
| **AC-18** | logged in as admin | they press **Add product** | an empty **product form** opens, including **starting stock** |
| **AC-19** | a valid form | the admin saves it | the product is created and immediately appears in the store (001), and its detail page is shown |
| **AC-20** | a new product with starting stock 0 | it is created | it appears as **"Out of stock"** (001 AC-3) |

### Editing a product (US-4)
| ID | Given | When | Then |
|---|---|---|---|
| **AC-21** | a product detail page | the admin presses **Edit product** | the **product form** opens, filled in with the current values. It has **no** stock field (stock is only changed by adjustment) |
| **AC-22** | a valid edit | the admin saves it | the changes appear immediately everywhere the product is shown |
| **AC-23** | a product's price is changed | guests view their baskets | they see the new price (002 AC-15). Existing orders keep their old price (003 AC-18) |
| **AC-24** | the admin is editing | they cancel | nothing changes |

### Adjusting stock (US-5)
| ID | Given | When | Then |
|---|---|---|---|
| **AC-25** | a product detail page | the admin presses **Adjust stock** | they can enter a **whole number** adjustment, positive or negative (e.g. +24, −3) |
| **AC-26** | stock is 10 | the admin adjusts by **+24** | stock becomes **34** |
| **AC-27** | stock is 10 | the admin adjusts by **−3** | stock becomes **7** |
| **AC-28** | stock is 10 when the admin opens Adjust stock, and a guest orders 2 before they save | the admin saves **+5** | stock becomes **13** (10 − 2 + 5). The guest's order is **not** undone |
| **AC-29** | an adjustment of 0, or one that isn't a whole number (e.g. 2.5, "abc") | the admin saves | it's rejected with a message |
| **AC-30** | stock is **3** | the admin saves an adjustment of **−5** | it's **rejected**, stock is unchanged, and a message shows the **current** level, e.g. *"Only 3 in stock. Can't remove 5."* |
| **AC-30a** | stock was 10 when the admin opened Adjust stock, and guests have since bought 8 | the admin saves **−5** | it's rejected as in AC-30, and the message shows the **current** level (**2**), not the level when the form was opened |
| **AC-31** | a successful adjustment | it completes | the new stock level is shown, and the stock status updates everywhere (001) |

---

## 4. Edge cases

- **Two admin tabs editing the same product:** the last save wins for product fields. Stock adjustments never conflict (AC-28). Accepted, since there's only one admin.
- **A category with no products:** still shown in the category list (001).
- **A product whose image URL is broken:** shows the placeholder (000 §5.4), and the admin can fix it by editing.

---

## 5. Out of scope

- Deleting or hiding products *(deleting is in the backlog; hiding is out of scope)*.
- Adding, renaming or deleting categories (000 §5.5).
- Setting an absolute stock level for an existing product (adjust only).
- Uploading image files (URL only).
- More than one admin account *(backlog, with customer accounts)*.
- Login lockout, password reset, or changing the admin password from the store.
- A history of stock adjustments.
- Prefilling the product form from its name (**008**, backlog).

---

## 6. Known limitations

- The admin login lasts **until logout**, even across browser restarts. That's fine for a portfolio piece, but a real store would expire it.
- The admin's username and password are set **outside** the store (in configuration). They can't be changed from within it.

---

## 7. Open questions

*None at present.*
