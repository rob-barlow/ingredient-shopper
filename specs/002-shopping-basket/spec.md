# 002 — Shopping Basket

> **Status:** ✅ Agreed (2026-09-23)
> **Depends on:** 000, 001
> **Last updated:** 2026-09-23

Guests add products to a **basket** from the product detail page, change quantities, remove lines and clear the basket. The basket stays with the guest's browser until they place an order. A basket does **not** reserve stock (000 §4, principle 3), but it is kept within current stock by the **clamp rule** (§3).

Terms in **bold** are defined in the [000 glossary](../000-overview/spec.md#3-glossary) or in §1 below.

---

## 1. Terms used in this spec

| Term | Meaning |
|---|---|
| **Basket line** | One product in the basket, with its quantity. A basket never has two lines for the same product. |
| **Quantity** | How many units of a product are on a basket line. Always **at least 1**. A line that would drop to 0 is removed. |
| **Line total** | The line's quantity × the product's **current** price. |
| **Basket total** | The sum of all line totals. |
| **Basket count** | The sum of all line quantities, e.g. 5 of A + 2 of B = **7**. |
| **Quantity selector** | The control on the product detail page for choosing how many to add. |
| **Stock limit** | A product's current stock level. The most a basket line can hold. |
| **Clamp rule** | See §3, "Stock limits in the basket". |

---

## 2. User stories

| ID | Story |
|---|---|
| **US-1** | As a guest, I want to add a chosen quantity of a product to my basket, so that I can buy several at once. |
| **US-2** | As a guest, I want to see how many items are in my basket on every page, so that I always know where I stand. |
| **US-3** | As a guest, I want to view my basket with prices and totals, so that I know what I'll pay. |
| **US-4** | As a guest, I want to change quantities, remove lines or clear the basket, so that I can change my mind. |
| **US-5** | As a guest, I want my basket to still be there when I come back, so that I don't have to start again. |
| **US-6** | As a guest, I want my basket to stay within what's actually in stock, so that I'm not surprised at checkout. |

---

## 3. Acceptance criteria

### Adding from the detail page (US-1)
| ID | Given | When | Then |
|---|---|---|---|
| **AC-1** | a product detail page (001) for an **in-stock** product | it is shown | it has a **quantity selector** (starting at 1) and an **Add to basket** button |
| **AC-2** | an **out-of-stock** product | its detail page is shown | the product **cannot be added**: the Add button is disabled and the selector is unavailable |
| **AC-3** | the selector is set to 2 and the product isn't in the basket | the guest presses Add | a basket line is created with quantity **2** |
| **AC-4** | the product is already in the basket with quantity 2 and the selector is set to 2 | the guest presses Add | the **same line** increases to quantity **4** (no second line) |
| **AC-5** | stock level is 3 | the guest tries to set the selector above 3 | the selector cannot go above **3** |
| **AC-6** | stock level is 5, the basket already has 4, and the selector is set to 2 | the guest presses Add | a message says **"Max is 5"** and the line is set to **5** |
| **AC-7** | stock level is 5 and the basket already has 5 **or more** (e.g. 6, after stock fell) | the guest presses Add | a message says **"Max is 5"** and the line is set to **5** |
| **AC-8** | any successful add | it completes | the guest sees confirmation that the item was added, and the basket count updates |
| **AC-9** | the product list (001) | it is shown | it has **no** Add button. Adding is only possible from the detail page |

### Basket count (US-2)
| ID | Given | When | Then |
|---|---|---|---|
| **AC-10** | a basket with 5 of product A and 2 of product B | any page is shown | the header shows a basket count of **7** |
| **AC-11** | an empty basket | any page is shown | the basket count shows **0** |
| **AC-12** | the header basket count | the guest clicks it | the basket page opens |

### Viewing the basket (US-3)
| ID | Given | When | Then |
|---|---|---|---|
| **AC-13** | a basket with lines | the basket page is shown | each line shows the product's **image**, **name**, **unit**, **current price**, **quantity** and **line total** |
| **AC-14** | a basket with lines | the basket page is shown | the **basket total** is shown |
| **AC-15** | a product's price changes after it was added | the basket page is shown | the **new price** is used for the line total and basket total, with no message |
| **AC-16** | an empty basket | the basket page is shown | a clear "your basket is empty" message is shown, with a way back to the product list |
| **AC-17** | a basket with lines | the basket page is shown | lines appear in the **order they were first added** |

### Changing the basket (US-4)
| ID | Given | When | Then |
|---|---|---|---|
| **AC-18** | a line with quantity 3 and stock level ≥ 2 | the guest presses **−** | the quantity becomes 2 |
| **AC-19** | a line with quantity 1 | the guest presses **−** | the line is **removed** |
| **AC-20** | a line with quantity 2 and stock level 5 | the guest presses **+** | the quantity becomes 3 |
| **AC-21** | any line | the guest presses **Remove** | the line is removed |
| **AC-22** | a basket with lines | the guest presses **Clear basket** | all lines are removed and the basket is empty |
| **AC-23** | any change to the basket | it completes | the line total, basket total and basket count update straight away |

### Stock limits in the basket: the clamp rule (US-6)
**Clamp rule:** whenever the basket page **loads**, or a line is **changed** (+, −), current stock is checked for the affected lines. Any line whose quantity would be above its stock limit is **set to the stock limit**, and the guest sees a message such as *"Eggs: max is 4"*. If the stock limit is 0, the line is **removed** with a message such as *"Eggs: now out of stock, removed from basket"*.

| ID | Given | When | Then |
|---|---|---|---|
| **AC-24** | the basket has 6 eggs and stock is now 4 | the basket page loads | the line is set to **4** with the message *"Eggs: max is 4"* |
| **AC-25** | the basket has eggs and stock is now 0 | the basket page loads | the line is **removed**, with a message saying eggs are out of stock |
| **AC-26** | a line whose quantity **equals** its stock limit | the basket page is shown | its **+** button is **disabled** |
| **AC-27** | the basket page is open with 4 eggs (+ enabled, stock was 5), and stock has since dropped to 3 | the guest presses **+** or **−** | stock is re-checked, the line is set to **3**, and the message *"Eggs: max is 3"* is shown |
| **AC-28** | the basket page is open with 2 eggs, and stock has since dropped to 0 | the guest presses **+** or **−** | the line is **removed**, with a message saying eggs are out of stock |
| **AC-29** | several lines exceed their stock limit | the basket page loads | each one is clamped, and each gets its own message |

### Lifetime (US-5)
| ID | Given | When | Then |
|---|---|---|---|
| **AC-30** | a guest with items in their basket | they reload the page, or close and reopen the browser | their basket is **unchanged** (apart from the clamp rule) |
| **AC-31** | a basket | any amount of time passes | it does **not** expire |
| **AC-32** | a guest places an order (003) | the order is placed successfully | their basket is **cleared** |
| **AC-33** | two different browsers | they each add items | they each have their **own** basket |

---

## 4. Edge cases

- **Rapid clicks:** pressing Add, + or − several times quickly gives the same result as pressing it that many times slowly, and never goes over the stock limit.
- **Deleted products:** can't happen in v1. Admins don't delete products, they set stock to 0 (to be confirmed in 004). Deleting products is in the [backlog](../../backlog.md).
- **Stock changes while the basket page is open:** not shown until the guest presses + or −, or reloads (AC-27, AC-28). No live updates.

---

## 5. Handed to 003 (Place order)

*(Moved to [003 §3, "Stock check at order time"](../003-place-order/spec.md), AC-10 to AC-15.)*

---

## 6. Out of scope

- Adding from the product list (AC-9).
- Typing a quantity directly (only + and −).
- Live stock updates while the basket page is open.
- Notifying guests about price changes.
- Reserving stock (000 §8).
- Sharing a basket across devices or browsers *(backlog, with customer accounts)*.
- Saved-for-later lists, wishlists.

---

## 7. Known limitations

- A basket belongs to **one browser**. If the guest clears their browser data or uses a private window, they get a new, empty basket, and the old one can't be recovered.
- Abandoned baskets are **never deleted** (AC-31). This is acceptable at portfolio scale. Cleaning them up is in the [backlog](../../backlog.md).

---

## 8. Open questions

*None at present.*
