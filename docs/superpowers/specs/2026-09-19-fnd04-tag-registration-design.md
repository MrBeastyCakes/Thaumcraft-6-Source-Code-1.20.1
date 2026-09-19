# FND-04 deferred item-tag registration design

## Intent and scope

Restore intended BETA26 assignment for items registered through shared categories, using Minecraft 1.20.1 Forge tags. Current source expands tags during common setup only when bound; tags load later. Runtime reproduction is mandatory before production edits. This slice covers ordinary tag assignment and load/reload lifecycle. Full TC6 parity remains the goal.

The owner authorized continuous builder/critic execution and commits only after approval. This architectural change within the existing attribution subsystem has a written design and plan for review; no repeated permission is requested for this reversible repair.

## Alternatives

1. Re-run ConfigAspects.init after tag load: simple, but clearing/replaying seeds can erase API/entity registrations or alter order; replay without removal leaves stale members.
2. Resolve all rules on each lookup: current memberships, but repeated scans walk every declaration and still require precedence and mutable-value handling.
3. Retain ordered declarations and rebuild tag-derived mappings on tag updates: selected. Constant-time lookups, removal of stale membership, direct registrations preserved, explicit declaration ordering.

## Contract

- Ordinary tag rules registered before binding remain pending; after binding they apply immediately.
- Last registration wins for overlapping item/tag declarations, in actual API call order, in both directions.
- Reload removes stale members and adds new ones. Removed tag assignments reveal an older direct assignment if present; otherwise raw lookup is absent. Generated fallback is not persisted.
- Tag declarations snapshot their AspectList; each resolved member owns an independent list. Direct registrations retain existing raw-list semantics.
- Entity registration is unchanged. clearTags clears item declarations, pending rules, resolved maps and entities. Refresh preserves direct/entity registrations.
- Object-tag count is the union of registered item IDs.
- Build and publish a complete replacement resolved map; do not clear/refill the live map during reload.
- Use pinned Forge TagsUpdatedEvent.shouldUpdateStaticData(): server data loads and remote-client packets refresh; duplicate integrated-client packet events do not. No new network messages or client authority.
- Preserve public/raw lookup, culling, container precedence and existing complex-item controls. No NBT identity, bonus, recipe or entity changes.

## Structure

A package-private AspectRegistrationStore owns direct registrations, retained tag rules, sequence precedence and the resolved map. String IDs and a membership resolver permit isolated-instance tests without replacing globals. Keep this domain-specific. AspectHelper owns one store and adapts real Forge tag membership; ThaumcraftApi's ordinary String overload delegates. A dedicated Forge subscriber refreshes on applicable tag events.

Store sequence with each direct assignment/rule; lookup selects the newest direct or resolved-tag entry. Refresh builds a local map from current memberships with per-member copies, then publishes it. Retain unbound/empty rules. Null lists or invalid names in tag registration remain no-ops; ItemStack overload null-list behavior is unchanged.

## Evidence and acceptance

First prove real iron/diamond tags are bound in GameTest runtime while expected base assignments are missing, with a direct seed control. Isolated-store tests cover delayed resolution, both precedence directions, overlapping tags, additions/removals, empty tags, copy isolation, clearing and union counts.

Production GameTests prove iron ingot METAL15, diamond CRYSTAL15/DESIRE15 and a direct control through raw/computed paths. A real asynchronous datapack reload round trip uses uniquely owned items/tag: initial A/C, replacement B/C, then A/C. A has an older direct fallback; C a newer direct override. Verify each stage and preserve unrelated registrations. Actual Forge events must drive the refresh, not a direct helper invocation.

Corroborate original category expansion and selected seeds through the shipped jar, retaining only paraphrase. Rank-1 survival, complete tables, downstream consumers and multiplayer remain open. Reload behavior is a modern adaptation, not a claim that 1.12.2 had the same lifecycle.

## Limits and decisions

The unused complex String overload still has generated/merge semantics gaps; repair it with complex registration and recipe attribution. Do not claim it is fixed. Prior direct-complex controls remain intact.

Ruling: preserve declaration order when adapting static category expansion to reloadable tags. This preserves precedence and avoids stale memberships. Cost if wrong: a later tag can win where an addon expected direct assignments always to dominate; the order remains explicit and testable.

Self-review: startup/reload correctness, ordering, ownership, lifecycle side selection, prior controls, reference limits and test cleanup are explicit. No irreversible action or missing user preference is involved.
