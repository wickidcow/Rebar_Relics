# Rebar Relics

Original talismans, charms and magical artifacts for [Rebar](https://github.com/pylonmc/rebar) and [Pylon](https://github.com/pylonmc/pylon), developed by wickidcow.

## Current status

**v0.1.0-SNAPSHOT — development only; not released or validated on a live server.**

This development implementation adds nine native talismans:

| Item ID | Effect while carried | Default bonus |
| --- | --- | --- |
| `rebar_relics:archery_talisman_1` | Arrow damage | +10% |
| `rebar_relics:strength_talisman_1` | Melee and arrow damage | +5% |
| `rebar_relics:experience_talisman_1` | Experience pickups | +10% |
| `rebar_relics:angel_talisman_1` | Chance to prevent fall damage | 75% |
| `rebar_relics:traveller_talisman_1` | Sprint activation grants Speed I | 60%; 3 minutes |
| `rebar_relics:firefighter_talisman_1` | Prevent fire damage and grant Fire Resistance | 3 minutes; consumed |
| `rebar_relics:water_breather_talisman_1` | Prevent drowning and grant Water Breathing | 3 minutes; consumed |
| `rebar_relics:lava_walker_talisman_1` | Prevent lava damage and grant Fire Resistance | 3 minutes; consumed |
| `rebar_relics:anvil_talisman_1` | Repair a tool before it breaks | Consumed |

Native Rebar item identity is required: renaming a vanilla item cannot grant powers.
Effects apply to items in top-level player inventory and (optionally) offhand, do not
multiply per copy, and do not scan shulker/ender chest contents.

Nine new crafting recipes live in `src/main/resources/recipes/minecraft/crafting_shaped.yml`.
Talisman bonuses can be configured in `plugins/Rebar_Relics/config.yml`.
The Pylon talismans guide category includes the items. Use Pylon's `/py give` command
for controlled testing.

## Target environment

- Java **25**; Paper **26.3** (also test Purpur separately)
- Rebar **0.45.0-26.3**, Pylon **0.43.0-26.3**
- Folia is **not claimed/supported** until upstream compatibility and native integration tests pass.

## Build

`gradle clean build` using Gradle 9.2.1 and JDK 25. GitHub Actions also builds and
runs JUnit tests; a passing build alone is not proof of in-game functionality.
Do not put snapshot builds on worlds you cannot restore.

## Roadmap

See [docs/ROADMAP.md](docs/ROADMAP.md) for classic Slimefun-inspired talismans,
advanced tiers, relic bags, rings and artifacts, Rebar recipes and future integrations.

## Attribution and code provenance

The existing Talismans 2026.45 Eco plugin and classic Slimefun talismans are
feature references only. This is a new original Rebar/Pylon implementation and
does not bundle, decompile for reuse, or redistribute their implementation,
textures or private assets.
