# FND-04 recipe-derived aspect attribution reference evidence

## Scope and evidence status

This note defines the BETA26 recipe-derived aspect contract needed by FND-04. It is evidence for implementation and tests, not an implementation plan. It covers `ThaumcraftCraftingManager.generateTags` and its recipe helpers, plus the public crucible and infusion output selectors.

- **Shipped-confirmed (rank 2):** `REF-0015` is a read-only `javap -p -c` inspection of the released BETA26 jar. It confirms the control flow and arithmetic described below, including the short-remainder behavior and null-to-empty registration.
- **Source-correlated (rank 3):** the readable names and source locations are from `ThaumcraftCraftingManager.java:398-605` and `ThaumcraftApi.java:201-210,227-234` in `REF-0001`. The checked bytecode agrees at the inspected sites.
- **Live-unverified:** no BETA26 game session in this evidence pass measured a derived item, ambiguous recipe choice, recursion cycle, reload, or late recipe registration. Display, smelting, multiplayer, and lifecycle consequences remain rank-1 work.

No original source, class file, disassembly, or binary is retained in this repository.

## Released contract

### Entry, recursion, and cache

1. Generation copies the requested stack and normalizes its count to one. Damageable items and items without subtypes are temporarily normalized to wildcard damage; wildcard damage becomes zero immediately before recipe lookup.
2. The history key is the normalized stack's serialized NBT text. A key already in the caller-supplied history returns `null`. The current key is appended before recipe traversal. Traversal proceeds only while the resulting history size is less than 100, so the one-hundredth distinct append returns `null` without looking up recipes.
3. The same mutable history list is passed through every recursive ingredient lookup. Entries are never removed when a branch returns. It is therefore a traversal-wide visited set, not a current recursion stack. Earlier ingredients and earlier crafting candidates can suppress later recursion into the same normalized stack.
4. Recipe categories have strict priority: the first matching crucible recipe wins; only if none exists is the first matching infusion recipe used; only if none exists are ordinary and arcane crafting recipes considered. An empty non-null result from an earlier category still wins and prevents fallback.
5. After a recipe traversal returns normally, its result is capped at 500 and registered against the original requested stack. A top-level no-recipe result is `null` at that point, and the released registration path converts it to an empty `AspectList`, so that ordinary no-recipe lookup is cached as empty attribution. A direct history revisit or the one-hundredth history append returns `null` earlier, before this method's cap and registration instructions, so that recursively requested stack is not directly cached by that call. Its caller's `getObjectTags` path turns the missing result into an empty computed list, and an enclosing derivation may later cache its own result. A later lookup can therefore differ from a fresh derivation if recipes or ingredient tags are registered after the first query.
6. `ThaumcraftApi.exists` is broken in the released jar: it queries the integer-keyed attribution map with serialized-NBT strings. Its early-cache test cannot succeed. The ordinary `getObjectTags` lookup still checks the integer-keyed map before calling generation, so a generated entry is reused on later public lookups despite the broken `exists` helper.

### Recipe output selection

- Crucible and infusion selectors iterate `CommonInternals.craftingRecipeCatalog.values()` and return the first recipe of the requested kind whose output passes `ItemStack.isItemEqual` against the requested stack. The invocation is shipped-confirmed; the 1.12.2 library contract that this covers item and metadata, not stack count or NBT, is source/library-correlated. The backing catalog is a `HashMap`; the released selection does not define a stable semantic tie-break among multiple matching recipes.
- Ordinary and arcane crafting scans the crafting registry and compares output item plus metadata, treating wildcard metadata as zero on either side. It does not compare output count or NBT.
- Every matching crafting candidate is evaluated. After non-positive aspects are removed, only a candidate with positive total aspect amount is eligible. The candidate with the strictly smallest total wins. Equal totals do not replace the current winner, so the first minimum encountered wins.

### Ingredient contribution and remainders

1. Each ingredient contributes the aspects of `Ingredient.getMatchingStacks()[0]`; alternative matching stacks are not compared or minimized. An ingredient with no matching stacks contributes nothing.
2. For ordinary or arcane crafting, a synthetic 3x3 inventory is populated with those same first alternatives in ingredient-list order and passed to `getRemainingItems`. Non-empty remainder stacks have their aspects subtracted before normalization. Subtraction uses `AspectList.reduce`: when the remainder amount exceeds the accumulated amount, the reduction is refused and the accumulated amount stays unchanged.
3. Infusion uses the central input followed by all components, but supplies no crafting recipe object to the ingredient helper, so it performs no remainder calculation.
4. For each accumulated aspect, ingredient contribution is computed as `v = accumulated * 0.75f / outputCount`. If `0.75 < v < 1.0`, `v` is promoted to `1.0`; otherwise the float is truncated toward zero. Zero and negative results are removed. The lower bound is strict: exactly `0.75` becomes zero.

### Per-category additions

- **Crucible:** copy all aspects of the catalyst's first matching stack without the 0.75 factor or output-count division. Add each required Essentia aspect as `floor(sqrt(requiredAmount) / outputCount)`. Remove non-positive results.
- **Infusion:** normalize the central item plus components with the 0.75 ingredient rule. Add each required Essentia aspect as `floor(sqrt(requiredAmount) / outputCount)`. Remove non-positive results.
- **Ordinary crafting:** use only the normalized ingredient contribution after remainder subtraction.
- **Arcane crafting:** apply the ordinary crafting calculation, then, when Vis cost is positive, add MAGIC as `floor(sqrt(1 + vis / 2) / outputCount)`. `vis / 2` is integer division before the square root. The result participates in the candidate's total used for minimum-positive selection.

## Independently calculated examples for future tests

These are arithmetic fixtures derived from the shipped-confirmed formula, not live observations. Other aspects are omitted unless stated.

| # | Inputs | Expected derived amount |
|---|---|---|
| 1 | Crafting ingredient has EARTH 4; output count 1 | `4 * .75 / 1 = 3.0`, so EARTH 3 |
| 2 | Crafting ingredient has EARTH 1; output count 1 | `0.75` is not greater than `0.75`; truncates to 0 and is removed |
| 3 | Ingredients total EARTH 2; output count 2 | `0.75` again truncates to 0 and is removed |
| 4 | Ingredients total EARTH 5; output count 4 | `0.9375` lies strictly between `.75` and `1`; promoted to EARTH 1 |
| 5 | Ingredients total METAL 8, remainder has METAL 3, output count 2 | net 5; `5 * .75 / 2 = 1.875`; truncates to METAL 1 |
| 6 | Ingredients total METAL 2, remainder has METAL 3, output count 1 | `reduce(3)` refuses the short subtraction; total remains 2; `1.5` truncates to METAL 1 |
| 7 | Crucible catalyst has MAGIC 3; required MAGIC 9; output count 3 | catalyst 3 plus `floor(sqrt(9)/3) = 1`, so MAGIC 4 |
| 8 | Crucible requires FIRE 8; output count 3; catalyst has no FIRE | `floor(sqrt(8)/3) = floor(0.9428...) = 0`; FIRE removed |
| 9 | Infusion inputs total MAGIC 8; required MAGIC 16; output count 2 | ingredient `8 * .75 / 2 = 3`; Essentia `floor(4/2) = 2`; MAGIC 5 |
| 10 | Arcane ingredients yield total 3; Vis 30; output count 2 | `floor(sqrt(1 + 30/2)/2) = floor(4/2) = 2` MAGIC; candidate total becomes 5 |

Selection fixture: if registry-order crafting candidates yield positive totals 5, 3, 3, and 0, the second candidate wins. The equal third candidate cannot replace it, and the zero candidate is ineligible. Cap fixture: any final aspect amount 700 is cached and returned as 500.

## Released hazards versus parity intent

The evidence establishes what shipped, but FND parity also requires a stable playthrough. The following are released behaviors that should be treated as compatibility hazards. This evidence does not authorize rebalancing, alternative minimization, or a change from the cumulative-history rule; any adaptation belongs in the implementation design and must be explicit and tested:

- First-match selection through unordered `HashMap.values()` and first-alternative ingredient choice can make attribution depend on registration or collection order. Deterministic ordering is a candidate modern adaptation requiring coordinator review; the shipped requirements established here are category priority, first-alternative choice, and the minimum-positive crafting rule.
- The monotonic shared history can affect later candidates, while the 100-entry cutoff and cycle return can turn a valid branch into `null`. This is an observed released rule. Whether the modern representation can preserve it exactly, and any deliberate correction if it cannot, must be decided and recorded outside this reference note.
- Empty catalyst alternatives, zero output counts, null aspect results for remainder stacks, and non-`ItemStack` infusion outputs can throw in released helper paths. Outer exception swallowing is inconsistent across categories and can turn these into missing attribution. These are crash/data-quality defects, not recommendations to reproduce the crashes.
- Caching an empty list after a failed or premature lookup freezes absence until the registry is cleared. Cache invalidation must follow the modern recipe and aspect-registration lifecycle; live reload behavior is not established by this inspection.
- The released broken `exists` key type is already independently identified in FND-04 evidence and is confirmed here only because it changes the generator's cache guard. It should not be copied as intended behavior.

## Evidence limits and test implications

Future deterministic tests can use the numeric fixtures above to cover strict rounding boundaries, remainders, category additions, cap behavior, and minimum-positive ties. Subject to coordinator rulings, additional tests can pin any approved ordering or cycle-handling adaptation and verify cache invalidation after recipe/aspect reload. Rank-1 acceptance still needs at least one original BETA26 derived item observed end-to-end and compared with the port through scanning and an Essentia consumer.

## Modern integration handoff (port observation, not BETA26 evidence)

The coordinator's current-source inspection found that active craftable crucible, infusion, and arcane recipes come from the live datapack `RecipeManager`; the legacy `ThaumcraftApi` catalog is separate and has no production add calls. `RecipeManager` is level-bound on both server and client, while the existing aspect lookup surface has no `Level` parameter. The design therefore needs an explicit level/context bridge and reload-aware cache ownership. Candidate sourcing must use the live datapack recipes while preserving the reference category priority, first-alternative ingredient rule, and minimum-positive crafting selection unless an explicit reviewed adaptation says otherwise. A process-global assumption based on the original registry shape would not describe the modern runtime.

Reference: `REF-0015` in [reference-catalog.md](reference-catalog.md).
