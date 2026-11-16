package io.github.wickidcow.rebarrelics;

import io.github.pylonmc.rebar.item.builder.ItemStackBuilder;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemStack;

import java.nio.file.Files;
import java.nio.file.Path;
import java.io.IOException;
import java.util.Comparator;
import java.util.UUID;
import java.util.stream.Stream;

/**
 * A non-player test run by disposable Paper CI only.
 * Uses real, registered Rebar item identity, Paper item byte serialization,
 * disk round trips, the backup file path and invalid-write protection.
 */
final class BagStoreAudit {
    private BagStoreAudit() {}

    static void run(RebarRelics plugin) {
        Path tmp = null;
        try {
            tmp = Files.createTempDirectory(plugin.getDataFolder().toPath(), "bag-ci-");
            BagStore store = new BagStore(tmp);
            UUID user = UUID.randomUUID();
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
            require(!RelicItems.isStorableTalisman(bag),
                    "Nested Talisman Bag must be rejected");
            ItemStack core = ItemStackBuilder.rebar(
                    Material.ECHO_SHARD, new NamespacedKey(plugin, "talisman_core_1")).build();
            require(!RelicItems.isStorableTalisman(core),
                    "Progression core must not enter bag storage");

            ItemStack[] items = store.load(user);
            require(items.length == 27, "Blank bag must expose 27 backing slots");
            ItemStack[] anotherPlayer = store.load(UUID.randomUUID());
            for (ItemStack item : anotherPlayer) {
                require(item == null, "Player UUID separation failed");
            }
            items[0] = first;
            items[26] = last;
            store.save(user, items);

            ItemStack[] roundtrip = store.load(user);
            require(roundtrip[0].isSimilar(first) && roundtrip[0].getAmount() == 2,
                    "Rebar item PDC or amount changed after serialization");
            require(roundtrip[26].isSimilar(last),
                    "Upper-tier slot not preserved on first disk round trip");
            require(!BagRules.accessible(1, 26) && BagRules.accessible(3, 26),
                    "Tier downgrade policy leaked higher-tier slots");

            // Remove slot 0, then prove tier III's existing slot is not erased.
            roundtrip[0] = null;
            store.save(user, roundtrip);
            require(Files.isRegularFile(tmp.resolve("talisman-bags")
                            .resolve(user + ".bak")),
                    "Previous storage snapshot is missing");
            ItemStack[] persisted = store.load(user);
            require(persisted[0] == null && persisted[26] != null,
                    "Removal or hidden-slot persistence mismatch");

            // The failed transaction must leave the canonical file untouched.
            persisted[0] = new ItemStack(Material.SHULKER_BOX);
            boolean rejected = false;
            try {
                store.save(user, persisted);
            } catch (IOException expected) {
                rejected = true;
            }
            require(rejected, "Invalid bag write was accepted");
            ItemStack[] afterRejected = store.load(user);
            require(afterRejected[0] == null && afterRejected[26].isSimilar(last),
                    "Invalid write corrupted canonical storage");

            plugin.getLogger().info("Bag storage audit: PASS (Rebar identity, 27-slot round-trip, atomic save and invalid-write guard)");
        } catch (Exception failure) {
            plugin.getLogger().severe("Bag storage audit: FAIL - " + failure);
        } finally {
            if (tmp != null) {
                try (Stream<Path> walk = Files.walk(tmp)) {
                    walk.sorted(Comparator.reverseOrder()).forEach(p -> {
                        try {
                            Files.deleteIfExists(p);
                        } catch (IOException ignored) { }
                    });
                } catch (IOException failure) {
                    plugin.getLogger().warning("Could not remove CI bag audit directory: " + failure);
                }
            }
        }
    }

    private static void require(boolean value, String message) {
        if (!value) throw new IllegalStateException(message);
    }
}
