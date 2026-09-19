# FND-04 container capacities - design (seventh slice)

**Date:** 2026-09-19. **Branch:** codex/fnd-04-aspect-lookup, base `d3be879`. **Item:** FND-04 (ACTIVE, seventh slice).
**Companion plan:** docs/superpowers/plans/2026-09-19-fnd04-container-capacities.md. **Evidence:** docs/agent-pipeline/fnd-04-container-capacities-evidence.md.

## Problem

The workboard acceptance asks that container capacities resolve consistently for scanning and Essentia handling with unit coverage. The port carries capacities, clamps and acceptance gates across a scattered container family, but no consolidated census existed against BETA26 and several edges were unverified (`TileJar` vs `TileJarFillable`, void-jar overflow, alembic `MAX_AMOUNT`, centrifuge capacity, tube-buffer and smelter buffers, `ItemEssentiaContainer` subclasses and their registration capacities).

## Authority

The in-repository BETA26 tree, read directly and paraphrased only. Comparisons use:

- `src/main/java_old/common/tiles/essentia/TileJarFillable.java` - jar capacity 250, short-typed `Amount`, clamp `min(am, 250 - amount)` with leftover return, drain guard with empty clearing, filter-gated acceptance query, suction constants.
- `src/main/java_old/common/tiles/essentia/TileJarFillableVoid.java` - same clamp, overflow destroyed (leftover 0), no filter gate inside `addToContainer`, inherited `addEssentia` leftover arithmetic.
- `src/main/java_old/common/tiles/essentia/TileAlembic.java` - `maxAmount` 128, filter gate inside `addToContainer`, short-typed amount, unconditional `doesContainerAccept`.
- `src/main/java_old/common/tiles/essentia/TileCentrifuge.java` - no capacity constant; a single-unit output slot (accepts one unit, `doesContainerContainAmount` only for amount 1, `containerContains` 1).
- `src/main/java_old/common/tiles/essentia/TileTubeBuffer.java` - `MAXAMOUNT` 10, single-unit additions only, take capped at the stored amount.
- `src/main/java_old/common/tiles/essentia/TileSmelter.java` - `maxVis` 256, smelt gate on `itemVis > maxVis - vis`, take guard that refreshes vis.
- `src/main/java_old/common/items/ItemTCEssentiaContainer.java` plus its subclasses - capacity supplied at construction (phial 10, crystal essence 1, label 1) with raw NBT aspect storage in `ItemGenericEssentiaContainer`.

The remaining implementers (crucible, thaumatorium, infusion matrix, potion sprayer, recharge pedestal, essentia mirror) were compared against their own BETA26 counterparts and are reported, not re-specified here.

## Census method

Every port type implementing `IAspectContainer`, `IAspectSource` or `IEssentiaContainerItem` was enumerated by search (plus the container-like smelter buffer, which implements neither interface). For each one the BETA26 counterpart was located, and six properties were compared: capacity constant, `addToContainer` clamp and leftover return, `takeFromContainer` guard and empty clearing, `doesContainerAccept`/filter semantics, `doesContainerContainAmount`, and the persisted amount type. Findings are recorded per row in the evidence audit table with verdict match / differ / port-only.

## Repairs (RED-first)

Four genuine mismatches were repaired; each had a failing witness captured before the production edit (`build/gametest/fnd04-cap-red-2-runGameTestServer.log`).

**F1 - Void jar filter gate on storage.** The port refused any aspect other than its filter inside `addToContainer`; BETA26 void jars have no such gate, so an empty void jar voids any aspect handed to it and the filter only shapes the pull and query surface. The gate was removed; the refusal path for a mismatched aspect into a non-empty jar is unchanged.

**F2 - Void jar transport accounts.** The port reported the full offered amount as added even when `addToContainer` refused it, while BETA26 inherits the jar's `amount - leftover` arithmetic. `addEssentia` now reports only consumed units (accepted units, including voided overflow, are still reported, because the void jar destroys the overflow and returns zero leftover for them).

**F3 - Alembic acceptance query.** The port gated `doesContainerAccept` on the aspect filter; BETA26 answers unconditionally and applies the filter in `addToContainer` and the pull path instead. The query now returns true; storage gating is untouched and still filter-bound.

**F4 - Phial transfer atomicity (found by this slice's RED).** Emptying a filled phial into a jar with less than the phial's capacity free absorbed the partial amount and then declined to consume the phial, which both mutated the jar and duplicated essentia. BETA26 guards the transfer with the jar's remaining capacity and adds nothing when the whole phial does not fit. The port now refuses that case up front for jar-family targets and, for every other `IAspectSource`, restores any partially absorbed amount so a refusal is atomic.

## Reported differences (not changed)

- **Void-jar suction constants.** The port pulls with 96/64 (and 48 when full and unfiltered) where BETA26 uses 48/32. Suction is transport-flow territory and is deferred to the ALC lane; recorded, not changed.
- **Centrifuge buffer.** BETA26 has no capacity constant and holds one unit in a single output slot; the port keeps a 10-unit input buffer as part of its own processing model. The slice witnesses the port's clamp and leftover arithmetic as a labelled characterization control and records the BETA26 fact; redesigning the device belongs to the centrifuge-processing lane.
- **Crucible query surface.** The crucible's aspect cap matches (500), but BETA26 answers `doesContainerContainAmount` false, `doesContainerContain` false and `containerContains` 0, while the port answers live values. No port consumer reads those queries on a crucible and the read surface is not one of this slice's five owned categories; recorded as a deviation.
- **Thaumatorium acceptance and buffer shape.** BETA26 accepts unconditionally and mirrors the selected recipe's needs; the port accepts while a per-aspect 64-unit store has room. Same category: recorded, not changed.
- **Essentia reservoir is port-only.** `TileEssentiaReservoir` (capacity 500, jar-like semantics) has no BETA26 counterpart in this tree; reported, never changed.
- **Item label.** BETA26's label is an essentia container item with capacity 1; the port's label is a plain item that stores its chosen aspect under its own NBT key and does not implement the container interface. Recorded; label interaction belongs to a later lane.
- **NBT key naming.** The port persists `Aspect`/`Amount`/`Facing` (jar, alembic) where BETA26 uses lowercase keys, and reads back symmetrically. No cross-version load path exists, so this is naming only.
- **Block-entity factories for alembics and tubes.** `BlockAlembic.newBlockEntity` and `BlockTube.newBlockEntity` are still null stubs and are already tracked as deferred issue ALC-02. The alembic and tube-buffer witnesses therefore attach the registered tile to the placed block directly; tickers, valve/filter interaction and routing remain with the ALC lane.

## Witness plan

Seventeen new GameTests in `src/gametest/java/thaumcraft/gametest/ContainerCapacityGameTests.java`, in the house style (one placed tile per test, assertions on production types only):

- Jar: fill to 250 with overflow leftover; drain to empty clearing aspect and amount; filter acceptance plus suction constants; save/load amount exact and short-typed.
- Void jar: overflow destroyed with capacity clamp; unfiltered aspect accepted into an empty filtered jar (F1 RED); transport reporting (F2 RED).
- Alembic: 128 clamp with leftover; drain clearing; unconditional acceptance query (F3 RED); filter still gates storage.
- Centrifuge: clamp/leftover characterization control labelled as differing from BETA26.
- Tube buffer: ten single units, mixed aspects, refusal beyond capacity, single-unit-only rule, take capped at stored amount.
- Smelter: 256-unit buffer tracking and GUI scaling, take guard refreshing vis.
- Items: phial fills exactly 10, refuses below capacity, empty-into-jar atomicity (F4 RED) and acceptance when 10 units fit, NBT roundtrip, registration capacities (phial 10, crystal essence 1, vis crystal 1).

## Boundaries

Owned by this slice: capacity numbers, clamp/leftover arithmetic, acceptance gating, empty clearing and amount persistence for the enumerated containers. Not touched: device flow algorithms (smelter cadence, tube routing/suction, centrifuge processing), GUI/rendering, object-tag/resolver/entity stores, new devices, and any ALC-lane repair such as the deferred block-entity factories.
