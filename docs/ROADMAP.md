# Rebar Relics roadmap

## Phase 1 - Native starter talismans (this PR)
- [x] First-class Rebar item registration, Pylon guide placement, translated item names
- [x] Nine initial talismans: Archery, Strength, Experience, Angel, Traveller, Firefighter, Water Breather, Lava Walker, Anvil
- [x] Inventory/offhand detection that cannot activate plain renamed vanilla items
- [x] Configurable bonuses, no duplicate-copy stacking
- [x] Pure unit tests for fractional XP accounting
- [ ] GitHub Actions build and first live test (must pass before release)

## Phase 2 - Classic Slimefun-inspired talismans
Build remaining classic-inspired Miner, Hunter, Farmer, Caveman, Magician, Wise,
Knight, Warrior, Whirlwind, Wizard talismans and Ender variants. Validate item
consumption, durability preservation, and interactions with protection plugins. Establish explicit names, charge counts
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

## New expansion phase

- [x] Inspect the provided Talismans JAR's configuration/recipe YAML (25 definitions); no copied implementation
- [x] Compare against Pylon's existing talisman families; skip XP, Water Breathing, Farming, Hunting, etc.
- [x] Tiered unique effects: Attack Speed, Boss, Poseidon, Resistance; Tier II/III for Archery and Strength
- [x] Poison/Wither immunity rings and expensive crafting cores
- [x] Eight-of-previous-tier upgrade recipes and Pylon materials
- [ ] Native server startup, recipe recognition, item modifier switching and item conservation tests
- [ ] Add unique classic Slimefun effects not already in Pylon (future isolated PR)
- [ ] Optional protected bag with tests before any Ender/shulker activation

## Classic-inspired expansion #2

- [x] Miner (20% eligible vanilla ore drops only) and Caveman (50% Haste)
- [x] Warrior and Knight consumable protection with duplicate-copy safeguards
- [x] Expensive Pylon material recipes for the new four talismans
- [ ] Validate WorldGuard protection, modified drops and 32+ stack overflow behavior in Paper 26.3
- [x] Whirlwind arrow-deflection (no projectile spawning/ownership changes) with three expensive tiers and pure unit tests
- [ ] Magician and Wizard intentionally deferred until native enchantment compatibility is proven

- [x] Align Gradle runner with upstream Pylon/Rebar run-paper plugin requirement (9.7.1)
- [ ] Complete disposable Paper 26.3 boot-and-recipe smoke run
