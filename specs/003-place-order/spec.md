# 003 — Place Order

> **Status:** ✅ Agreed (2026-09-23)
> **Depends on:** 000, 002
> **Last updated:** 2026-09-23

A guest turns their basket into an **order** for collection, directly from the **basket page** (002). They give a **collection name** and a **secret code**. The **clamp rule** (002 §3) runs first. If the order is placed, stock is taken, the order is given an **order number**, the basket is cleared and a **confirmation page** is shown. There is no payment in v1.

Terms in **bold** are defined in the [000 glossary](../000-overview/spec.md#3-glossary), the [002 terms](../002-shopping-basket/spec.md#1-terms-used-in-this-spec), or §1 below.

---

## 1. Terms used in this spec

| Term | Meaning |
|---|---|
| **Order form** | The part of the basket page with the collection name field, secret code field and **Place order** button. |
| **Order number** | A unique reference given to each order when it is placed. Shown to the guest and used by admins (005). |
| **Order line** | One product in an order: its name, unit, quantity and **price at the moment the order was placed** (a snapshot, see §3). |
| **Order total** | The sum of the order lines' quantity × snapshot price. |
| **Order status** | Where an order is in its life: **Placed → Ready for collection → Collected**. Changes after "Placed" belong to 005. |

---

## 2. User stories

| ID | Story |
|---|---|
| **US-1** | As a guest, I want to place an order straight from my basket, so that checking out is quick. |
| **US-2** | As a guest, I want to give a name and secret code, so that I can prove the order is mine at collection. |
| **US-3** | As a guest, I want to be told if stock changed before my order went through, so that I know exactly what I'm getting. |
| **US-4** | As a guest, I want a confirmation with everything I need for collection, so that I can pick up my order. |
| **US-5** | As the store, I want an order to take stock only if it succeeds, so that stock is never lost or oversold. |

---

## 3. Acceptance criteria

### The order form (US-1, US-2)
| ID | Given | When | Then |
|---|---|---|---|
| **AC-1** | a basket with at least one line | the basket page is shown | it shows the **order form** below the basket total |
| **AC-2** | an empty basket | the basket page is shown | there is **no** order form, or **Place order** is disabled |
| **AC-3** | the collection name is empty | the guest presses Place order | the order isn't placed, and a message says a name is required |
| **AC-4** | a collection name containing anything other than **letters** (accented letters count, e.g. "José"), **spaces**, **hyphens** or **apostrophes** (e.g. "Sam2", "Sam!") | the guest presses Place order | the order isn't placed, and a message says which characters are allowed |
| **AC-4a** | the collection names "Mary Jane", "Anne-Marie" and "O'Brien" | each is used to place an order | each is **accepted** |
| **AC-4b** | a collection name with no letters at all (e.g. "- '") | the guest presses Place order | the order isn't placed, and a message says the name must contain letters |
| **AC-5** | a collection name longer than **50** characters | the guest types it | it can't go past 50, or a message says the maximum is 50 |
| **AC-6** | the secret code is empty | the guest presses Place order | the order isn't placed, and a message says a secret code is required |
| **AC-7** | a secret code containing anything other than letters and numbers (e.g. "ban ana!") | the guest presses Place order | the order isn't placed, and a message says the code may only contain letters and numbers |
| **AC-7a** | a secret code shorter than **3** or longer than **20** characters | the guest presses Place order | the order isn't placed, and a message says the code must be 3–20 characters |
| **AC-7b** | the guest types a secret code | it is typed | it is **visible** (not hidden like a password) |
| **AC-8** | the guest enters secret code "BaNaNa42" | the order is placed | it is stored, and shown on the confirmation page, as **"banana42"** |
| **AC-9** | the order isn't placed for any reason (validation, clamp, error) | the basket page is shown again | the name and code the guest typed are **kept** |

### Stock check at order time (US-3, US-5)
*(Moved here from 002 §5.)*
| ID | Given | When | Then |
|---|---|---|---|
| **AC-10** | a valid order form | the guest presses Place order | the **clamp rule** (002 §3) is applied to every basket line **before** anything else happens |
| **AC-11** | the basket has 5 eggs and stock is now 3 | the guest presses Place order | the order is **not placed**, the line is set to **3**, the message *"Eggs: max is 3"* is shown, and the guest stays on the basket page |
| **AC-12** | the basket has eggs and stock is now 0 | the guest presses Place order | the order is **not placed**, the line is **removed** with a message, and the guest stays on the basket page |
| **AC-13** | the clamp rule removed **every** line | the basket page is shown | the basket is empty (002 AC-16) and there's no order form (AC-2) |
| **AC-14** | the clamp rule changed nothing | the check completes | the order goes ahead |
| **AC-15** | two guests each have the **last** loaf in their basket | they both press Place order at almost the same moment | **exactly one** order gets the loaf. The other guest gets the clamp result (AC-12) |

### Placing the order (US-4, US-5)
| ID | Given | When | Then |
|---|---|---|---|
| **AC-16** | the clamp rule changed nothing | the order is placed | each product's **stock level decreases** by the line quantity |
| **AC-17** | the order is placed | it is stored | each order line records the product's name, unit, quantity and **current price** at that moment (a snapshot) |
| **AC-18** | an order was placed at £1.20 for milk | milk's price later changes to £1.50 | the order still says **£1.20** |
| **AC-19** | a price changed between loading the basket page and placing the order | the order is placed | the order uses the **new** price, with **no warning** *(warning is in the backlog)* |
| **AC-20** | the order is placed | it is stored | it gets a **unique order number**, and its status is **Placed** |
| **AC-21** | the order is placed | it completes | the basket is **cleared** (002 AC-32) and the basket count shows 0 |
| **AC-22** | the guest double-clicks Place order | the requests arrive | **only one** order is created |

### Confirmation (US-4)
| ID | Given | When | Then |
|---|---|---|---|
| **AC-23** | an order was placed | the confirmation page is shown | it shows the **order number**, **collection name**, **secret code**, every **order line** (name, unit, quantity, price, line total) and the **order total** |
| **AC-24** | the confirmation page | it is shown | it tells the guest to **collect from Glenda's Groceries**, giving their name and secret code |
| **AC-25** | the guest leaves the confirmation page | they try to get back to it (back button, reload) | they **can't** see the order again. It isn't an error, they just see the store (or empty basket) |

### When something goes wrong (US-5)
| ID | Given | When | Then |
|---|---|---|---|
| **AC-26** | any failure while placing the order (e.g. part of the system is unavailable) | the order can't be completed | **no order** is created, **no stock** is taken, the basket is **unchanged**, and the guest sees a message asking them to try again |
| **AC-27** | stock was taken but the order then failed to save | the failure is handled | the stock is **put back**, so that AC-26 still holds |

---

## 4. Edge cases

- **Leading or trailing spaces** in the name or code are ignored ("  Sam " → "Sam").
- **Name case:** the name is stored as typed (e.g. "Sam"). Lookup in 005 ignores case.
- **Order number** is shown so it's easy to read out loud. Its exact format is a plan decision.

---

## 5. Out of scope

- A separate checkout page *(backlog, nice to have)*.
- Warning the guest about price changes *(backlog, nice to have)*.
- Viewing an order again after leaving the confirmation page.
- Payment of any kind *(fake payment is in the backlog)*.
- Delivery *(backlog)*.
- Cancelling or editing an order after it's placed.
- Changing an order's status after "Placed" (**005**).

---

## 6. Open questions

*None at present.*
