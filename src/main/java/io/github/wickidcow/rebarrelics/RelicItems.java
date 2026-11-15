package io.github.wickidcow.rebarrelics;

import io.github.pylonmc.pylon.PylonPages;
import io.github.pylonmc.rebar.item.RebarItem;
import io.github.pylonmc.rebar.item.builder.ItemStackBuilder;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemStack;
import org.bukkit.entity.Player;

import java.util.Objects;

public final class RelicItems {
    private static NamespacedKey archeryKey;
    private static NamespacedKey strengthKey;
    private static NamespacedKey experienceKey;

    private RelicItems() {}

    public static void initialize(RebarRelics plugin) {
        archeryKey = new NamespacedKey(plugin, "archery_talisman_1");
        strengthKey = new NamespacedKey(plugin, "strength_talisman_1");
        experienceKey = new NamespacedKey(plugin, "experience_talisman_1");

        register(Material.BOW, archeryKey);
        register(Material.BLAZE_POWDER, strengthKey);
        register(Material.EXPERIENCE_BOTTLE, experienceKey);
    }

    private static void register(Material material, NamespacedKey key) {
        ItemStack item = ItemStackBuilder.rebar(material, key).build();
        RebarItem.register(RebarItem.class, item);
        PylonPages.TALISMANS.addItem(item);
    }

    /**
     * Inventory and offhand only; never recursively open containers or read ender chests.
     * Presence is boolean so duplicate copies do not multiply an effect.
     */
    public static boolean has(Player player, Type type, boolean offhand) {
        NamespacedKey key = Objects.requireNonNull(switch (type) {
            case ARCHERY -> archeryKey;
            case STRENGTH -> strengthKey;
            case EXPERIENCE -> experienceKey;
        }, "Talismans have not been registered");

        for (ItemStack stack : player.getInventory().getStorageContents()) {
            if (RebarItem.isRebarItem(stack, key)) return true;
        }
        return offhand && RebarItem.isRebarItem(player.getInventory().getItemInOffHand(), key);
    }

    public enum Type {
        ARCHERY,
        STRENGTH,
        EXPERIENCE
    }
}
