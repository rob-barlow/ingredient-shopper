# 005 — Admin: Order Management

> **Status:** ✅ Agreed (2026-09-24)
> **Depends on:** 000, 003, 004
> **Last updated:** 2026-09-23

The **admin** sees every order in an **orders list**, filters it by status, looks orders up by **collection name + secret code** at the counter, and sets each order's **status**. Order details are shown by **expanding** a row. There's no separate order page. Changing status **never** affects stock.

Terms in **bold** are defined in the [000 glossary](../000-overview/spec.md#3-glossary), the [003 terms](../003-place-order/spec.md#1-terms-used-in-this-spec), the [004 terms](../004-admin-inventory/spec.md#1-terms-used-in-this-spec), or §1 below.

---

## 1. Terms used in this spec

| Term | Meaning |
|---|---|
| **Orders list** | The admin-only page listing orders. |
| **Order row** | One order in the orders list. It can be **collapsed** (summary only) or **expanded** (showing its order lines too). |
| **Status filter** | Restricts the orders list to one **order status**, or **All**. |
| **Collection lookup** | Finding orders by exact **collection name** + **secret code** when a customer arrives to collect. |
| **Status order** | The order statuses in workflow order: **Placed**, then **Ready for collection**, then **Collected**. |

---

## 2. User stories

| ID | Story |
|---|---|
| **US-1** | As the admin, I want to see all orders with the outstanding ones first, so that I know what needs doing. |
| **US-2** | As the admin, I want to filter orders by status, so that I can focus on one stage of work. |
| **US-3** | As the admin, I want to see what's in an order without leaving the list, so that I can pick it quickly. |
| **US-4** | As the admin, I want to look up orders by name and secret code, so that I hand the right order to the right person. |
| **US-5** | As the admin, I want to set an order's status, so that the list reflects what's happened, and I can fix mistakes. |

---

## 3. Acceptance criteria

### Access
| ID | Given | When | Then |
|---|---|---|---|
| **AC-1** | logged in as admin | any page is shown | the header shows an **Orders** link, which opens the orders list |
| **AC-2** | a guest | any page is shown | there is **no** Orders link |
| **AC-3** | a guest | they try to view orders, look them up or change a status by any means | it's **refused** and nothing is revealed or changed (as 004 AC-8) |

### The orders list (US-1)
| ID | Given | When | Then |
|---|---|---|---|
| **AC-4** | orders exist | the orders list is shown | it shows **all** orders, with status filter **All** |
| **AC-5** | the orders list | it is shown | each collapsed row shows the **order number**, **collection name**, **status**, **order total** and **time placed** |
| **AC-6** | the orders list | it is shown | rows are sorted by **status order** (Placed first, then Ready for collection, then Collected), then **newest first** within each status |
| **AC-7** | more than 10 orders pass the filter | the list is shown | it shows **10 per page** with page controls (as 001 AC-5 to AC-7) |
| **AC-8** | no orders pass the filter | the list is shown | a clear "no orders found" message is shown |

### Status filter (US-2)
| ID | Given | When | Then |
|---|---|---|---|
| **AC-9** | the orders list | the admin chooses a status in the filter | only orders with that status are shown, still newest first |
| **AC-10** | a status filter is set | the admin chooses **All** | all orders are shown again (AC-6 ordering) |
| **AC-11** | the filter changes | the list updates | it returns to page 1 |

### Expanding an order (US-3)
| ID | Given | When | Then |
|---|---|---|---|
| **AC-12** | a collapsed row | the admin expands it | it shows every **order line**: name, unit, quantity, snapshot price and line total. It does **not** show the secret code |
| **AC-12a** | any admin view (list, expanded row, lookup results) | it is shown | the **secret code is never revealed**. The store only ever confirms whether an entered code **matches** (AC-16) |
| **AC-13** | an expanded row | the admin collapses it | only the summary (AC-5) is shown again |
| **AC-14** | several rows | the admin expands more than one | they can all be expanded at once |

### Collection lookup (US-4)
| ID | Given | When | Then |
|---|---|---|---|
| **AC-15** | the orders list | it is shown | it has a **collection lookup** with a name field and a secret code field |
| **AC-16** | an uncollected order for "Sam" with code "banana42" | the admin looks up "sam" + "BANANA42" | that order is shown (**exact** match on both, ignoring case and leading/trailing spaces) |
| **AC-17** | the same order | the admin looks up "Sa" + "banana42", or "Sam" + "banana" | it is **not** shown (no partial matches) |
| **AC-18** | "Sam" / "banana42" has **two** uncollected orders | the admin looks them up | **both** are shown (000 §5.2) |
| **AC-19** | "Sam" / "banana42" also has a **Collected** order | the admin looks them up | the collected order is **not** shown (lookup shows **uncollected** orders only, 000 §5.2) |
| **AC-20** | no uncollected order matches | the admin looks up | a clear "no matching orders" message is shown |
| **AC-21** | lookup results | they are shown | they appear as normal order rows (collapsed, expandable, status can be set) |
| **AC-22** | lookup results are shown | the admin clears the lookup | the normal orders list returns, with its previous filter |

### Setting status (US-5)
| ID | Given | When | Then |
|---|---|---|---|
| **AC-23** | any order row | it is shown | its status can be changed to **any** of the three statuses, including going **back** (e.g. Collected → Placed) and **skipping** (Placed → Collected) |
| **AC-24** | the admin changes a status | it is saved | the new status is shown straight away in that row |
| **AC-25** | the admin changed a status | the row is shown | it **stays where it is** until the list is next loaded (e.g. page change, filter change, reload), and then it's re-sorted (AC-6) |
| **AC-26** | any status change | it is saved | **stock doesn't change**, and the order's lines and prices don't change |
| **AC-27** | the status is set to the value it already has | it is saved | nothing changes, and there's no error |

---

## 4. Edge cases

- **Very many orders:** orders are kept forever (§6), so pagination (AC-7) is what keeps the list usable.
- **Time placed** is shown in the store's local time, in a readable format (e.g. "23 Sep 2026, 14:05").
- **An order placed while the admin is viewing the list:** it appears the next time the list is loaded. No live updates.

---

## 5. Out of scope

- A separate order detail page.
- Cancelling orders *(backlog)*.
- Editing an order's lines, name or code.
- Searching orders other than by collection lookup (e.g. by order number).
- Notifying the customer when an order is ready.
- Deleting orders.
- A history of status changes.

---

## 6. Known limitations

- Orders are **kept forever**. That's fine at portfolio scale.
- Any status can be set to any other. This is safe **only** because status changes have no side effects (AC-26). If cancelling is added later with a stock rule, allowed transitions must be revisited.

---

## 7. Open questions

*None at present.*
