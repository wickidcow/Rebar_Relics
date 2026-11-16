# Rebar Relics — Talisman Bag Acceptance Plan

**Status:** Not accepted for production. Intended for a disposable Paper 26.3 test server only, with matching Rebar and Pylon dependencies.

## Before starting

1. Back up the complete disposable world, player data and the plugin directory. Keep a copy of the original inventory **outside** the server.
2. Confirm [the Paper CI startup test](https://github.com/wickidcow/Rebar_Relics/actions) reports **39/39 recipes** and **Bag storage audit: PASS** for the exact JAR commit.
3. Keep `bags.enabled: false` on production. On the throwaway server only, set `bags.enabled: true` in `plugins/Rebar_Relics/config.yml` and restart.
4. Use real crafted/registered Rebar items, not renamed vanilla impostors. Make a separate paper/count table of every talisman and its amount **before** testing.

**Conservation rule:** At every step, the sum of a player's stored inventory, cursor stack, visible drops and bag contents must equal the starting total plus deliberate crafting gains or consumption. A missing or additional item is a release blocker.

## Test cases

| Case | Action | Expected result |
|---|---|---|
| Disabled by default | Right-click a valid bag with `bags.enabled: false` | Access refused and inventory unchanged |
| First deposit | Open Bag I and left-click to deposit one eligible, authentic talisman stack | Stack moves to one of slots 0–8, disappears from the cursor, survives reopen |
| First withdrawal | Click that stack with an empty cursor | Exact item identity and amount return to the cursor; saved slot is empty |
| Right-click transfer | Repeat with right-click and a stack of multiple talismans | Whole stack moves once; no split, loss, or second copy |
| Tier I lock | Inspect bag slots 9–26 | Locked and unmodifiable, including by shift-click, number key, drag, Q or double click |
| Tier upgrade | Put items in slot 17 with Bag II and slot 26 with Bag III, then open Bag I | Higher slots invisible/inaccessible but retained on disk; reopen Bag III to retrieve them |
| Multiple copies | Give player a second bag and open either copy | Same personal storage; no separate inventory is created |
| Two players | Have A and B each open their bag | A sees only A's UUID storage; exchanging bag items changes neither storage owner |
| Unknown items | Attempt to deposit Shulker Box, any bag, progression core, normal diamond and unregistered custom item | Every deposit refused; unchanged cursor and backing slot |
| Pylon talisman | Store a native Pylon talisman | Stored/retrieved with original PDC; **does not activate** while only in the bag |
| Other actions | Try shift-click, double click, drag, hotbar key, offhand swap, creative/spectator access, cursor collect, click outside | None may move a bag-stored item without an explicit saved transfer |
| External cancellation | In a testing region with an inventory-interaction restriction, perform a denied action | Cancelled events must never execute a bag transfer |
| Leave and rejoin | Close GUI, logout, restart, login, reopen | Same UUID data and quantities; any higher-tier hidden slot remains |
| Fail-closed checksum | **On disposable server only**, change or remove a stored slot from a version-2 bag YAML while server is stopped | On startup, opening fails with a checksum error; it must not silently erase or autorestore items |
| Legacy data | Back up a version-1 dev snapshot, open and save in a controlled test | Reads old data, saves version-2 checksum, no item identity/amount changes |
| Crash window | On a throwaway instance, deliberately terminate the server around one deposit/withdrawal | Record player-file vs bag-file quantities, detect any mismatch; do not enable production unless proven safe |

## Where storage lives

`plugins/Rebar_Relics/talisman-bags/<player-uuid>.yml` is the canonical record. A `.bak` holds the preceding version of that record. The v2 `sha256` field detects accidental corruption; it is **not** a cryptographic authentication mechanism against malicious administrators.

**Never automatically replace a damaged canonical file with its `.bak`.** The backup may contain items already withdrawn, resulting in duplication. Quarantine both files, compare player data, inventory records and timestamps, and perform a carefully documented manual reconciliation. Do not delete an unrecognized item or reset the bag to blank.

## Remaining acceptance gates

- [ ] Every click/drag/hotbar/creative interaction above passes with real player clients, including Geyser if supported.
- [ ] Cancellation and protection-plugin interactions are respected.
- [ ] Full survival inventory + bag totals survive clean restart and forced restart; no duplication or loss.
- [ ] Attack Speed, Whirlwind, poison/wither, Miner and all consumable relics work without cross-plugin effects.
- [ ] Separate Purpur 26.3 regression check. Folia support must be assessed separately, not inferred.
- [ ] Owner approves the final item economy, recipes and release.

Record failures with **exact JAR commit**, server type/build, `latest.log`, before/after player inventory counts, relevant UUID bag YAML (redact UUID if sharing publicly), and a reproducible click sequence. Do not use production player files.
