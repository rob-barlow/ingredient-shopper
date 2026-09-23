# 001 — Browse Catalogue

> **Status:** ✅ Agreed (2026-09-23)
> **Depends on:** 000
> **Last updated:** 2026-09-23

Guests browse one **product list**, narrowed by an optional **category filter** and an optional **name search**. Clicking a product opens its full details. This feature is **read-only**: adding to the basket belongs to 002, and editing products belongs to 004.

Terms in **bold** are defined in the [000 glossary](../000-overview/spec.md#3-glossary) or in §1 below.

---

## 1. Terms used in this spec

| Term | Meaning |
|---|---|
| **Product list** | The main browsing view. It shows every product that passes the active filters. |
| **Category filter** | Restricts the list to one category. Either one category is selected, or **All** (no category filter). |
| **Search term** | Text that restricts the list to products whose **name** contains it. Either set, or not set (no search filter). |
| **Active filters** | The current category filter and search term together. A product is shown only if it passes **both**. |

---

## 2. User stories

| ID | Story |
|---|---|
| **US-1** | As a guest, I want to see the store's products when I arrive, so that I can start shopping straight away. |
| **US-2** | As a guest, I want to filter products by **category**, so that I can find things without knowing their exact names. |
| **US-3** | As a guest, I want to search products by name, including within a category, so that I can quickly find something specific. |
| **US-4** | As a guest, I want to see at a glance whether a product is **in stock**, so that I don't waste time on things I can't buy. |
| **US-5** | As a guest, I want to open a product to see its description and how many are left, so that I can decide whether to buy it. |

---

## 3. Acceptance criteria

### Product list display (US-1, US-4)
| ID | Given | When | Then |
|---|---|---|---|
| **AC-1** | the product list | it is shown | each product shows its **image**, **name**, **unit**, **price** and **stock status** |
| **AC-2** | a product with stock level > 0 | it appears in the list | its stock status reads **"In stock"** |
| **AC-3** | a product with stock level 0 | it appears in the list | it is still shown, with stock status **"Out of stock"** |
| **AC-4** | the product list | it is shown | products are sorted **alphabetically by name**, ignoring case |
| **AC-5** | more than 10 products pass the active filters | the list is shown | it shows **10 products per page**, with controls to move between pages |
| **AC-6** | 10 or fewer products pass the active filters | the list is shown | no page controls are shown |
| **AC-7** | a guest on a page other than the first | they go to the next or previous page | the products continue in alphabetical order, with none repeated or skipped, and the active filters are unchanged |
| **AC-8** | no products pass the active filters | the list is shown | a clear **"no products found"** message is shown |

### Arriving (US-1)
| ID | Given | When | Then |
|---|---|---|---|
| **AC-9** | a guest | they open the store | the product list shows **all products** (category **All**, no search term), page 1 |

### Category filter (US-2)
| ID | Given | When | Then |
|---|---|---|---|
| **AC-10** | a guest | they view the product list | they can see every category, in alphabetical order, plus an **All** option |
| **AC-11** | the Dairy category has 6 products and no search term is set | a guest selects Dairy | the list shows exactly those 6 products |
| **AC-12** | a category is selected | a guest selects **All** | the category filter is removed, and any search term still applies |
| **AC-13** | the category filter is changed | the list updates | it returns to page 1 |

### Search (US-3)
| ID | Given | When | Then |
|---|---|---|---|
| **AC-14** | a product named "Semi-skimmed milk" in category **All** | a guest searches "milk" | it appears in the list |
| **AC-15** | the same product | a guest searches "MILK" or "Milk" | it appears (search ignores case) |
| **AC-16** | the same product | a guest searches "mil" | it appears (partial matches count) |
| **AC-17** | a product whose **description** mentions "milk" but whose name doesn't | a guest searches "milk" | it does **not** appear (only names are searched) |
| **AC-18** | category **Dairy** is selected | a guest searches "milk" | only products in Dairy whose names contain "milk" are shown |
| **AC-19** | category **Bakery** is selected, and "Semi-skimmed milk" is in Dairy | a guest searches "milk" | "Semi-skimmed milk" does **not** appear |
| **AC-20** | a search term is set | a guest changes the category | the search term is kept and applies to the new category |
| **AC-21** | a guest with **no** search term set | they submit an empty search, or one containing only spaces | **nothing happens**: the list, filters and page are unchanged |
| **AC-21a** | a search term is set (e.g. "milk") and category Dairy is selected | the guest submits an empty search, or one containing only spaces | the **search filter is removed**, the **category filter stays** (Dairy), and the list returns to page 1 |
| **AC-22** | a new search is submitted | the list updates | it returns to page 1 |

### Product detail (US-4, US-5)
| ID | Given | When | Then |
|---|---|---|---|
| **AC-23** | the product list | a guest clicks a product | its **product detail page** opens |
| **AC-24** | a product detail page | it is shown | it shows the product's **image**, **name**, **unit**, **price**, **description** and exact **stock level** (e.g. "12 in stock") |
| **AC-25** | a product with stock level 0 | its detail page is shown | the stock level reads **"Out of stock"** |
| **AC-26** | a product detail page | the guest goes back | they return to the product list with the same filters and page as before |
| **AC-27** | a link to a product that doesn't exist | a guest opens it | they see a clear "product not found" message and a way back to the store |

### General
| ID | Given | When | Then |
|---|---|---|---|
| **AC-28** | any price | it is shown | it uses the **£** symbol with two decimal places (e.g. £1.20), per 000 §5.3 |
| **AC-29** | a product with no image, or one that fails to load | it is shown anywhere | a **placeholder** image is shown, per 000 §5.4 |

---

## 4. Edge cases

- **Empty category:** a category with no products shows the "no products found" message (AC-8).
- **Very long names:** long product names are shown in full, or shortened neatly, but never break the page layout.
- **Stock changes while browsing:** the list shows stock status as it was when the page loaded. It doesn't need to update live.
- **Search with special characters** (e.g. `%`, `'`, `&`): treated as ordinary text. The search never errors.
- **Leading or trailing spaces** in a search term are ignored ("  milk " is the same as "milk").

---

## 5. Out of scope

- Adding to the basket from the list or detail page. This belongs to **002**, which will extend these pages.
- Sorting options other than alphabetical *(Future, low priority)*.
- Filtering by anything other than category and name (e.g. price, stock status).
- Selecting more than one category at once.
- Searching descriptions.
- Live updates of stock levels.
- Creating, editing or deleting products (**004**). Categories are a fixed list (000 §5.5).

---

## 6. Open questions

*None at present.*
