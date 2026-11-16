package io.github.wickidcow.rebarrelics;

import io.github.pylonmc.rebar.item.builder.ItemStackBuilder;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.inventory.ItemStack;

import java.nio.file.Files;
import java.nio.file.Path;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Comparator;
import java.util.UUID;
import java.util.stream.Stream;

/**
 * Runs only when REBAR_RELICS_BAG_AUDIT=true in disposable Paper CI.
 * Real Paper item serialization and actual Rebar PDC IDs are exercised.
 */
final class BagStoreAudit {
    private BagStoreAudit() {}

    static void run(RebarRelics plugin) {
        Path tmp = null;
        try {
            tmp = Files.createTempDirectory(plugin.getDataFolder().toPath(), "bag-ci-");
            BagStore store = new BagStore(tmp);
            UUID owner = UUID.randomUUID();
            ItemStack first = ItemStackBuilder.rebar(
                    Material.FLINT, new NamespacedKey(plugin, "archery_talisman_1")).build();
            ItemStack last = ItemStackBuilder.rebar(
                    Material.AMETHYST_SHARD, new NamespacedKey(plugin, "whirlwind_talisman_3")).build();
            first.setAmount(2);

            require(RelicItems.isStorableTalisman(first), "Original Rebar talisman was not accepted");
            require(RelicItems.isStorableTalisman(last), "Highest-tier Rebar talisman was not accepted");
            require(!RelicItems.isStorableTalisman(new ItemStack(Material.SHULKER_BOX)),
                    "Shulker box must be rejected");
            ItemStack bag = ItemStackBuilder.rebar(
                    Material.LEATHER, new NamespacedKey(plugin, "talisman_bag_1")).build();
            require(!RelicItems.isStorableTalisman(bag), "Nested bag must be rejected");
            ItemStack core = ItemStackBuilder.rebar(
                    Material.ECHO_SHARD, new NamespacedKey(plugin, "talisman_core_1")).build();
            require(!RelicItems.isStorableTalisman(core), "Crafting core must not enter bag storage");

            ItemStack[] contents = store.load(owner);
            require(contents.length == 27, "Blank bag must expose 27 backing slots");
            for (ItemStack value : store.load(UUID.randomUUID())) {
                require(value == null, "Player UUID separation failed");
            }
            contents[0] = first;
            contents[26] = last;
            store.save(owner, contents);

            Path file = tmp.resolve("talisman-bags").resolve(owner + ".yml");
            YamlConfiguration fresh = YamlConfiguration.loadConfiguration(file.toFile());
            require(fresh.getInt("version") == 2, "New writes must be version 2");
            require(fresh.getString("sha256") != null, "Missing storage checksum");
            ItemStack[] roundtrip = store.load(owner);
            require(roundtrip[0].isSimilar(first) && roundtrip[0].getAmount() == 2,
                    "Rebar item PDC or amount changed in serialized storage");
            require(roundtrip[26].isSimilar(last), "High-tier item missing from disk");
            require(!BagRules.accessible(1, 26) && BagRules.accessible(3, 26),
                    "Tier downgrade unexpectedly exposes high-tier slot");

            roundtrip[0] = null;
            store.save(owner, roundtrip);
            require(Files.isRegularFile(tmp.resolve("talisman-bags").resolve(owner + ".bak")),
                    "Previous storage backup missing");
            ItemStack[] persisted = store.load(owner);
            require(persisted[0] == null && persisted[26] != null,
                    "Withdrawal state or high-tier slot lost");

            // A rejected write must leave the canonical file unchanged.
            persisted[0] = new ItemStack(Material.SHULKER_BOX);
            boolean rejected = false;
            try {
                store.save(owner, persisted);
            } catch (IOException expected) {
                rejected = true;
            }
            require(rejected, "Invalid item write was accepted");
            require(store.load(owner)[0] == null && store.load(owner)[26].isSimilar(last),
                    "Invalid save corrupted the canonical file");
            // A stale GUI snapshot may not overwrite newer on-disk contents.
            ItemStack[] stale = store.load(owner);
            ItemStack[] concurrent = store.load(owner);
            concurrent[0] = first.clone();
            store.save(owner, concurrent);
            boolean staleRejected = false;
            try {
                store.saveIfUnchanged(owner, stale, stale);
            } catch (IOException expected) {
                staleRejected = true;
            }
            require(staleRejected, "Stale GUI snapshot overwrote newer storage");
            require(store.load(owner)[0].isSimilar(first),
                    "Stale GUI rejection damaged newer canonical data");

            // Restore the expected post-withdrawal state before corruption tests.
            concurrent[0] = null;
            store.save(owner, concurrent);


            // Tamper with valid YAML by removing an existing item slot. Version 1
            // lacked this protection; version 2 must refuse silently lost data.
            String original = Files.readString(file, StandardCharsets.UTF_8);
            YamlConfiguration tampered = YamlConfiguration.loadConfiguration(file.toFile());
            tampered.set("slots.26", null);
            tampered.save(file.toFile());
            expectReadFailure(store, owner, "Tampered slot was silently accepted");
            Files.writeString(file, original, StandardCharsets.UTF_8);
            require(store.load(owner)[26].isSimilar(last), "Canonical restore failed");

            // Empty/malformed slot maps cannot be taken as an empty valid file.
            YamlConfiguration invalidType = YamlConfiguration.loadConfiguration(file.toFile());
            invalidType.set("slots", "not-a-slot-map");
            invalidType.save(file.toFile());
            expectReadFailure(store, owner, "Wrong slot YAML type was accepted");
            Files.writeString(file, original, StandardCharsets.UTF_8);

            // A valid .bak is never automatically applied when a canonical
            // file is bad, because an older backup could duplicate prior withdrawals.
            YamlConfiguration corrupt = YamlConfiguration.loadConfiguration(file.toFile());
            corrupt.set("sha256", "0".repeat(64));
            corrupt.save(file.toFile());
            expectReadFailure(store, owner, "Corrupt canonical file was auto-restored");
            Files.writeString(file, original, StandardCharsets.UTF_8);

            // Version 1 snapshots must remain readable and upgrade on next write.
            UUID legacyOwner = UUID.randomUUID();
            Path legacyFile = tmp.resolve("talisman-bags").resolve(legacyOwner + ".yml");
            YamlConfiguration legacy = new YamlConfiguration();
            legacy.set("version", 1);
            legacy.set("owner", legacyOwner.toString());
            legacy.set("slots.0", Base64.getEncoder().encodeToString(first.serializeAsBytes()));
            legacy.save(legacyFile.toFile());
            ItemStack[] oldItems = store.load(legacyOwner);
            require(oldItems[0].isSimilar(first) && oldItems[0].getAmount() == 2,
                    "Legacy storage migration lost item identity or quantity");
            store.save(legacyOwner, oldItems);
            require(YamlConfiguration.loadConfiguration(legacyFile.toFile()).getInt("version") == 2,
                    "Legacy snapshot did not migrate to checksum storage");
            require(store.load(legacyOwner)[0].isSimilar(first),
                    "Migrated item no longer passes checksum verification");

            plugin.getLogger().info(
                    "Bag storage audit: PASS (real Rebar identity, 27-slot round-trip, checksum corruption rejection, v1 migration, UUID isolation, stale-GUI guard)");
        } catch (Exception failure) {
            plugin.getLogger().severe("Bag storage audit: FAIL - " + failure);
        } finally {
            if (tmp != null) {
                try (Stream<Path> walk = Files.walk(tmp)) {
                    walk.sorted(Comparator.reverseOrder()).forEach(p -> {
                        try { Files.deleteIfExists(p); }
                        catch (IOException ignored) { }
                    });
                } catch (IOException failure) {
                    plugin.getLogger().warning("Could not clean CI bag directory: " + failure);
                }
            }
        }
    }

    private static void expectReadFailure(BagStore store, UUID owner, String message) {
        try {
            store.load(owner);
            throw new IllegalStateException(message);
        } catch (IOException expected) {
            // Correct fail-closed behavior; do not restore an older backup.
        }
    }

    private static void require(boolean condition, String message) {
        if (!condition) throw new IllegalStateException(message);
    }
}
