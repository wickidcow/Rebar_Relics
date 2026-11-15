# Rebar Relics

An original expansion for [Rebar](https://github.com/pylonmc/rebar) and [Pylon](https://github.com/pylonmc/pylon).

**Experimental development branch. No release has been approved or tested in game.**

## What it adds

- Tier I/II/III **Archery, Strength, Attack Speed, Boss, Poseidon (trident) and Resistance** talismans.
- Permanent **Immunity Ring** (poison) and **Necrosis Ring** (wither).
- Classic-inspired **Angel, Traveller, Firefighter, Lava Walker, and Anvil** charms.
- Four custom progression materials: **Relic Matrix**, **Talisman Cores I/II/III**.
- Original passive/effect listeners, configurable strengths, Pylon's talisman guide and Rebar item IDs.
- Exactly one highest tier applies per family; carrying duplicates does not increase potency.

## No Pylon duplicates

Pylon already has Experience, Water Breathing, Health, Farming, Hunting, Breeding, Bartering, Enchanting and Luck talismans. This addon does not add new replacements for these. The prior dev-snapshot `rebar_relics:experience_talisman_1` and `rebar_relics:water_breather_talisman_1` remain **registered but inert and hidden**, to avoid unknown-item problems for testers. Their recipes have been removed. No new Sharpness talisman: Strength covers its melee effect.

## Crafting difficulty

See [docs/RECIPES.md](docs/RECIPES.md). Every permanent Tier I talisman uses a costly Tier I core surrounded by themed ingredients, inspired by the included Talismans v2026.45 JAR. Tier II requires **eight of Tier I** and a Tier II core; Tier III requires **eight of Tier II** and a Tier III core. The cores require Pylon materials and rare vanilla resources. Consumable charms use a slightly cheaper but still meaningful Relic Matrix. The recipe data lives under `src/main/resources/recipes/minecraft/crafting_shaped.yml`.

## Compatibility target

- Java 25, Paper 26.3, Rebar 0.45.0-26.3, Pylon 0.43.0-26.3
- Do not assume Folia or Purpur compatibility without actual smoke testing.
- Keep regular backups. The test jar is not production ready.
- Ender chest and shulker activation is intentionally not implemented.
- Native Rebar item metadata identity is required (renaming vanilla items has no effect).

## Build

`gradle clean build` using JDK 25 and Gradle 9.2.1. A GitHub Actions workflow compiles the JAR and runs unit tests. Recipe-file consistency tests do not replace testing against a running server.

## Attribution

The uploaded Eco Talismans JAR and classic Slimefun talismans are **mechanics/recipe references only**. Rebar Relics contains fresh code and no copied class files, plugin resources, skins or proprietary artwork from the example.
