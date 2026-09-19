Implementation resumed on 2026-09-19 after the project owner explicitly instructed the team to lift the hold.

# Budget Hold Policy

This policy records the lifted hold, the owner's continuing release authority, and the implementation order after reactivation.

## Project owner authority

The project owner is the release authority for this port. Only the project owner may clear the budget hold, authorize implementation spending, accept the Minecraft EULA, or approve any distribution. No agent or coordinator may clear the hold on its own.

## Current development state

Implementation work may resume in dependency order. `FND-01` is `READY` and unclaimed; all other unfinished items retain their workboard dependencies. A coordinator must record a claim in `active-claims.md` before implementation begins.

The hold lift does not authorize Minecraft EULA acceptance or public distribution. Those actions remain reserved to the project owner, and public distribution additionally remains blocked pending written rights-holder permission.

Original BETA26 binaries, assets, decompiled files, and recordings must not be copied into the repository.

## Reactivation condition

The hold required an explicit project-owner instruction to lift. That condition was met on 2026-09-19. Any future pause or reactivation likewise requires an explicit project-owner instruction; agents and coordinators cannot change that authority on their own.

## Owner instructions

- **2026-09-18, reference-evidence pass authorized.** Implementation remains paused. The owner authorized the BETA26 reference-evidence pass only: reference-material discovery and cataloging, setup of an unmodified Minecraft 1.12.2 + Forge + Thaumcraft 6.1.BETA26 reference environment, and observation runs of that environment to capture behavior evidence for identified workboard items. EULA acceptance remains the owner's action alone. The port's own Gradle and game runs remain prohibited until the hold is cleared.
- **2026-09-19, implementation hold lifted.** The owner explicitly instructed the team to lift the hold. `FND-01` becomes the first `READY` item; dependent work remains blocked until its recorded prerequisites are satisfied. This instruction does not authorize EULA acceptance or public distribution.

## Spending order after reactivation

1. `FND-01`
2. `FND-04`
3. `RSR-01/RSR-02`
4. `ALC-00/ALC-03`
5. `CAS-01/CAS-02`
6. The remaining workboard order.

(Within each tier, items are worked in workboard dependency order.)

The tiers follow the workboard dependency spine and the deferred register's resume guidance.
