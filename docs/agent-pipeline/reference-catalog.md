# BETA26 Reference Catalog

This catalog stores the observation metadata behind every BETA26 parity claim: which environment was observed, where the artifact lives, and how the capture was made. It lets an agent confirm how a claim was produced without the original game files being present in this repository.

**Entries record metadata only, and they NEVER store original binaries, assets, decompiled files, or recordings in the repository.** An entry is a pointer to a capture that stays on the capturing machine; it is never a copy of the captured content.

**Capture status: no BETA26 reference has been captured for this port yet.** The catalog is an empty template awaiting its first entry. Capture is pending before gameplay parity claims: until an entry exists here for the environment described in `parity-evidence-index.md`, no gameplay behavior may be claimed as verified against BETA26.

## Required fields

Every entry uses exactly these fields, in this order:

```text
Reference identifier: <stable catalog ID, for example REF-0001>
Minecraft version: <exact observed version string>
Forge version: <exact observed loader version string>
Thaumcraft version: <exact observed BETA26 build string>
Artifact location: <filesystem path of the artifact on the capturing machine, outside this repository>
SHA-256: <64-character lowercase hex digest of the observed artifact file>
Launcher or profile: <launcher, instance, or profile name used for the capture>
World seed: <seed of the world used, or n/a for a capture that uses no world>
Player setup: <game mode, inventory, research, and permission state needed to reproduce the observation>
Capture scenario: <exact steps or command sequence performed to observe the behavior>
Recorded by: <capture operator or capturing agent identity>
Date recorded: <ISO 8601 date of the capture>
```

## How to record an entry

1. Capture the behavior in an unmodified BETA26 environment and rank it against the evidence hierarchy in `parity-evidence-index.md`.
2. Compute the artifact digest on the capturing machine: `sha256sum <artifact>` in git-bash, or `certutil -hashfile <artifact> SHA256` in cmd.
3. Add a new entry under `## Captured entries` using the next free `REF` identifier, with every field from `## Required fields` in the same order.
4. Keep the artifact, the world directory, any decompiled source, and any recording outside the repository; the entry stores only the path and the digest.
5. Reference the identifier from the work item's scenario notes at `docs/agent-pipeline/<work-item>-evidence.md` and from the `Parity reference` field of the item's handoff.

## Captured entries

None recorded yet. Add each real capture here as a new entry in the required field order, anchored by the reference identifier that the work-item evidence cites.

## Illustrative example (not a real capture)

The block below shows the shape of one entry. It is an example only, not a real capture, and must not be cited as evidence.

```text
EXAMPLE ONLY — illustrative entry shape, not a real capture.

Reference identifier: REF-EXAMPLE-0001
Minecraft version: 1.12.2
Forge version: 14.23.5.2860
Thaumcraft version: 6.1.BETA26
Artifact location: D:/game-instances/TC6-Reference/mods/Thaumcraft-1.12.2-6.1.BETA26.jar
SHA-256: 0a1b2c3d4e5f60718293a4b5c6d7e8f90a1b2c3d4e5f60718293a4b5c6d7e8f9
Launcher or profile: CurseForge instance "TC6 Reference" (Minecraft 1.12.2, Forge 14.23.5.2860)
World seed: 1234567890
Player setup: creative mode, full Thaumonomicon unlocked, no Warp, no other mods
Capture scenario: create a fresh world, scan one aura node, and record the displayed aspect values before quitting
Recorded by: example author (not a real capture)
Date recorded: 2026-01-01
```
