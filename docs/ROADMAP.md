# Rebar Relics roadmap

## Phase 1 - Native starter talismans (this PR)
- [x] First-class Rebar item registration, Pylon guide placement, translated item names
- [x] Three original entry talismans: Archery, Strength, Experience
- [x] Inventory/offhand detection that cannot activate plain renamed vanilla items
- [x] Configurable bonuses, no duplicate-copy stacking
- [x] Pure unit tests for fractional XP accounting
- [ ] GitHub Actions build and first live test (must pass before release)

## Phase 2 - Classic Slimefun-inspired talismans
Design original, configurable implementations of classic concepts such as Miner, Hunter,
Anvil, Water Breathing, Lava Walker, and others. Establish explicit names, charge counts
and trigger semantics in tests before adding each recipe. Do not assume item migration
from Slimefun or the eco Talismans plugin.

## Phase 3 - Tier system and relic bag
Three-tier talisman families; highest-tier-only option; persistent talisman bag with
permissions, slot limits, storage-safety tests, and protection against duplicate effects.
Never scan nested shulker or ender chest inventory by default.

## Phase 4 - Broader relic expansion
Rings, amulets, unique artifacts, Rebar/Pylon materials, progression & research,
relic abilities, optional integration bridges, translations and custom textures.

## Compatibility and release gates
- Initial target: Paper 26.3 + Java 25, Rebar 0.45.0-26.3 and Pylon 0.43.0-26.3.
- Purpur may work as a Paper fork but requires a separate smoke test.
- **Do not claim Folia compatibility yet**: upstream Rebar/Pylon does not guarantee it.
- CI passing does not replace full in-game compatibility verification.
- Never distribute proprietary code or artwork from the reference Eco Talismans JAR.
- Version bump, new release, and production server deployment only after integration checks.
