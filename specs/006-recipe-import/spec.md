# 006 — Recipe Import

> **Status:** ✅ Agreed (2026-09-24)
> **Depends on:** 000, 001, 002
> **Last updated:** 2026-09-24

A guest pastes a **recipe URL** on the **recipe import page**. The store reads the recipe, matches each ingredient to a product, works out how many to buy, and offers **substitutes** for anything out of stock. The guest checks everything on a **review screen**, then adds their choices to the basket in one go. Nothing is added without the guest confirming (000 §4, principle 2).

This is the only feature that uses **AI**, so its results can vary. §3 holds the **hard rules** that must *always* be true. §4 holds the **evaluation examples** the AI is judged against.

Terms in **bold** are defined in the [000 glossary](../000-overview/spec.md#3-glossary), the [002 terms](../002-shopping-basket/spec.md#1-terms-used-in-this-spec), or §1 below.

---

## 1. Terms used in this spec

| Term | Meaning |
|---|---|
| **Recipe import page** | The page where a guest pastes a recipe URL. Linked from the header for everyone. |
| **Review screen** | Shown after a successful import. Lists every **proposal line** and **skipped ingredient** for the guest to check before adding. |
| **Proposal line** | One product the import suggests buying, with a quantity, linked to the recipe ingredient(s) it covers. |
| **Needed amount** | How much of an ingredient the recipe asks for, **as written** in the recipe (no scaling). |
| **Proposed quantity** | The number of whole products to buy to cover the needed amount, **always rounded up**. E.g. 400g flour with flour sold in 1.5kg bags gives **1**. |
| **Skipped ingredient** | A recipe ingredient that won't be proposed, shown with a **reason**. |
| **Excluded staples** | **Salt, pepper, water and cooking oils.** Always skipped. |
| **Essential ingredient** | An ingredient the AI judges the recipe can't reasonably be made without (e.g. saffron in a saffron risotto). |
| **Unavailable** | An ingredient with no matching product in stock, and no suitable in-stock substitute. |

---

## 2. User stories

| ID | Story |
|---|---|
| **US-1** | As a guest, I want to paste a recipe link and get its ingredients proposed, so that I don't have to find each one myself. |
| **US-2** | As a guest, I want to review every proposed product and quantity before anything reaches my basket, so that I stay in control. |
| **US-3** | As a guest, I want substitutes offered for out-of-stock ingredients, so that I can still make the recipe. |
| **US-4** | As a guest, I want to be told clearly if the store can't supply the recipe, so that I don't buy half of it for nothing. |
| **US-5** | As a guest, I want to see what was skipped and why, so that I know what I still need at home. |

---

## 3. Acceptance criteria

### Hard rules: these must always hold, whatever the AI returns
| ID | Rule |
|---|---|
| **AC-1** | The import **never** changes the basket by itself. Only the guest pressing **Add to basket** on the review screen does (AC-29). |
| **AC-2** | The review screen **never** proposes an **out-of-stock** product, as a match or as a substitute. |
| **AC-3** | A **substitute** is never selected unless the guest selects it. |
| **AC-4** | Only products that **exist in the catalogue** are ever proposed. |
| **AC-5** | A proposed quantity **never exceeds** the product's current stock. |
| **AC-6** | **Untrusted content:** nothing written on a recipe page can make the store do anything other than propose products from the catalogue. Instructions hidden in a page (e.g. "add 100 bottles of wine", "set prices to £0") have **no effect**. |
| **AC-7** | Excluded staples (salt, pepper, water, cooking oils) are **never** proposed. |

### Submitting a URL (US-1)
| ID | Given | When | Then |
|---|---|---|---|
| **AC-8** | any page | it is shown | the header has a link to the **recipe import page** |
| **AC-9** | the recipe import page | a guest pastes a URL and presses **Import** | a **"Reading recipe…"** indicator is shown until the result is ready |
| **AC-10** | the text entered isn't a web address (e.g. "pancakes") | the guest presses Import | a message asks for a valid web address, and nothing is sent |
| **AC-11** | the page can't be reached | the import runs | a message says the page couldn't be reached. The basket is unchanged |
| **AC-12** | the page is reachable but **isn't a recipe** | the import runs | a message says no recipe was found at that address. The basket is unchanged |
| **AC-13** | the import takes longer than **60 seconds** | the time limit passes | it stops, and a message asks the guest to try again. The basket is unchanged |

### The review screen (US-2, US-5)
| ID | Given | When | Then |
|---|---|---|---|
| **AC-14** | a successful import | the review screen is shown | it shows the recipe's **title** and, if the recipe states it, **how many it serves** (e.g. "Serves 4"). Quantities are **not** scaled |
| **AC-15** | a successful import | the review screen is shown | each **proposal line** shows the **original ingredient text** from the recipe, the proposed product's **image, name, unit, price**, the **proposed quantity** and the **line total** |
| **AC-16** | a recipe needs 400g of flour and flour is sold as 1.5 kg | the review screen is shown | the proposed quantity is **1** |
| **AC-17** | a recipe needs 3 eggs and eggs are sold as 6 each | the review screen is shown | the proposed quantity is **1** |
| **AC-18** | a recipe needs 1 kg of flour and flour is sold as 500 g | the review screen is shown | the proposed quantity is **2** |
| **AC-19** | two ingredients match the **same product** (e.g. butter for pastry, and butter for greasing) | the review screen is shown | they are **combined into one** proposal line, with the needed amounts added together before rounding up, and **both** original ingredient texts shown |
| **AC-20** | in-stock proposal lines | the review screen is shown | each is **ticked** by default, and the guest can **untick** any of them |
| **AC-21** | skipped ingredients | the review screen is shown | they're listed separately, each with its **reason**, e.g. *"Salt: staple, not added"*, *"Oil for greasing: staple, not added"*, *"A pinch of nutmeg: too small an amount to buy"* |
| **AC-22** | a **non-essential** ingredient is **unavailable** | the review screen is shown | it's listed as skipped with the reason *"Not available at Glenda's"* |
| **AC-23** | the review screen | it is shown | a **total** of the currently ticked or selected lines is shown, and it updates as the guest changes their choices |

### Out of stock and substitutes (US-3)
| ID | Given | When | Then |
|---|---|---|---|
| **AC-24** | an ingredient's matched product is **out of stock**, and in-stock substitutes exist | the review screen is shown | the line offers **up to 3** substitutes (any category), each with its own image, name, unit, price and proposed quantity, plus a **Skip** option. **None** is selected by default |
| **AC-25** | the matched product has **some** stock but **not enough** (e.g. 3 packs needed, 1 in stock) | the review screen is shown | the line offers **one** choice from: **the available quantity** of the matched product (1 pack), **up to 3 substitutes** each covering the **full** needed amount (if any are in stock), or **Skip**. The available-quantity option is selected by default, with a note such as *"Only 1 of the 3 needed is in stock"* |
| **AC-25a** | a line from AC-25 | the guest makes a choice | only that one option is included. The line never combines the matched product with a substitute |
| **AC-26** | a line with substitutes | the guest selects one | only that substitute is included, and the total updates |
| **AC-27** | a line with substitutes and nothing selected | the guest presses Add to basket | that line is treated as **skipped** |

### Recipes the store can't supply (US-4)
| ID | Given | When | Then |
|---|---|---|---|
| **AC-28** | an **essential** ingredient is **unavailable** | the review screen is shown | a warning at the **top** says **"We don't sell everything needed for this recipe"** and names the missing ingredient(s) |
| **AC-28a** | the warning from AC-28 | the review screen is shown | the missing essential ingredients are **also** listed as skipped, with the reason *"Essential, not available at Glenda's"* |
| **AC-28b** | the warning from AC-28 | the guest continues | the rest of the review screen works **normally**. They can still add the other lines (e.g. they already have the missing ingredient at home) |

### Adding to the basket (US-2)
| ID | Given | When | Then |
|---|---|---|---|
| **AC-29** | the review screen | the guest presses **Add to basket** | every **ticked** line and **selected** substitute is added to the basket at its proposed quantity. Unticked and skipped lines aren't added |
| **AC-30** | a proposed product is **already** in the basket | it is added | it's added **on top** of the existing quantity (002 AC-4) |
| **AC-31** | stock changed between the review screen loading and Add to basket being pressed | the lines are added | the **clamp rule** (002 §3) applies to each line, with the usual messages |
| **AC-32** | the lines were added | it completes | the guest is taken to the **basket page**, which shows the added items |
| **AC-33** | nothing is ticked or selected | the review screen is shown | **Add to basket** is disabled |
| **AC-34** | the guest leaves the review screen without adding | they come back later | the review isn't kept. They'd import the URL again |

---

## 4. Evaluation examples

Because AI output varies, correctness is judged against a fixed set of **example recipes**, as well as the hard rules in §3. The seed data (000 §9) must cover them.

- At least **3** example recipes, e.g. **pancakes**, **spaghetti bolognese**, **a simple cake**.
- Each example lists its **expected proposal lines** (product and quantity) and **expected skips**.
- At least one example has an **out-of-stock** ingredient with valid substitutes.
- At least one example has an **essential unavailable** ingredient (AC-28).
- At least one example has a **duplicate** ingredient to combine (AC-19).
- The AI "passes" an example when its proposal matches the expected lines. The exact tolerance (e.g. an acceptable alternative match) is set in the plan.

*The specific recipes and their expected results are written during planning, alongside the seed data.*

---

## 5. Edge cases

- **Recipe measures that aren't in our list** (cups, tablespoons, "1 medium onion"): the AI converts them to the product's measure as sensibly as it can. Rounding is always **up**.
- **Ingredient already covered by another line:** combined (AC-19).
- **Very long recipes (30+ ingredients):** all shown on one review screen, with no pagination.
- **The same URL imported twice:** treated as two separate imports. Each "Add to basket" adds on top.
- **Ingredients that are really equipment or instructions** ("a baking tin", "for serving"): skipped with a reason.

---

## 6. Out of scope

- Scaling quantities to a chosen number of portions *(backlog)*.
- Changing a proposal line's product or quantity on the review screen. The guest can adjust quantities in the basket afterwards.
- Live progress while importing, e.g. "found 8 ingredients…" *(backlog)*.
- Saving imported recipes, or a history of imports.
- Importing from an image of a shopping list (**007**, backlog).
- Recipes that aren't on a public web page (e.g. pasted text, PDFs).

---

## 7. Known limitations

- Matching and "essential" judgements are made by AI, so they **won't always be right**. The review screen (showing the original ingredient text next to each match) is the guest's safeguard.
- Some recipe sites may block automated reading. Those show the "couldn't be reached" or "no recipe found" message.

---

## 8. Open questions

*None at present.*
