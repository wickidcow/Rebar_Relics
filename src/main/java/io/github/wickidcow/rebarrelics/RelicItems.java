package io.github.wickidcow.rebarrelics;

import io.github.pylonmc.pylon.PylonPages;
import io.github.pylonmc.rebar.item.RebarItem;
import io.github.pylonmc.rebar.item.builder.ItemStackBuilder;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.EnumMap;
import java.util.Map;

public final class RelicItems {
    public enum Type {
        ARCHERY, STRENGTH, EXPERIENCE,
        ANGEL, TRAVELLER, FIREFIGHTER, WATER_BREATHER, LAVA_WALKER, ANVIL
    }

    private static final Map<Type, NamespacedKey> KEYS = new EnumMap<>(Type.class);

    private RelicItems() {}

    public static void initialize(RebarRelics plugin) {
        register(plugin, Type.ARCHERY, "archery_talisman_1", Material.BOW);
        register(plugin, Type.STRENGTH, "strength_talisman_1", Material.BLAZE_POWDER);
        register(plugin, Type.EXPERIENCE, "experience_talisman_1", Material.EXPERIENCE_BOTTLE);
        register(plugin, Type.ANGEL, "angel_talisman_1", Material.FEATHER);
        register(plugin, Type.TRAVELLER, "traveller_talisman_1", Material.SUGAR);
        register(plugin, Type.FIREFIGHTER, "firefighter_talisman_1", Material.FIRE_CHARGE);
        register(plugin, Type.WATER_BREATHER, "water_breather_talisman_1", Material.NAUTILUS_SHELL);
        register(plugin, Type.LAVA_WALKER, "lava_walker_talisman_1", Material.MAGMA_CREAM);
        register(plugin, Type.ANVIL, "anvil_talisman_1", Material.IRON_INGOT);
    }

    private static void register(RebarRelics plugin, Type type, String name, Material material) {
        NamespacedKey key = new NamespacedKey(plugin, name);
        ItemStack item = ItemStackBuilder.rebar(material, key).build();
        RebarItem.register(RebarItem.class, item);
        PylonPages.TALISMANS.addItem(item);
        KEYS.put(type, key);
    }

    private static NamespacedKey key(Type type) {
        NamespacedKey key = KEYS.get(type);
        if (key == null) throw new IllegalStateException("Relic item not registered: " + type);
        return key;
    }

    /** Inventory contents only; no shulkers, ender chests or other nested storage. */
    public static boolean has(Player player, Type type, boolean includeOffhand) {
        NamespacedKey key = key(type);
        for (ItemStack stack : player.getInventory().getStorageContents()) {
            if (RebarItem.isRebarItem(stack, key)) return true;
        }
        return includeOffhand && RebarItem.isRebarItem(
                player.getInventory().getItemInOffHand(), key);
    }

    /** Consume exactly one matching original item, without affecting other talismans. */
    public static boolean consume(Player player, Type type, boolean includeOffhand) {
        NamespacedKey key = key(type);
        for (int slot = 0; slot < player.getInventory().getStorageContents().length; slot++) {
            ItemStack stack = player.getInventory().getItem(slot);
            if (!RebarItem.isRebarItem(stack, key)) continue;
            if (stack.getAmount() == 1) {
                player.getInventory().setItem(slot, null);
            } else {
                ItemStack remaining = stack.clone();
                remaining.setAmount(stack.getAmount() - 1);
                player.getInventory().setItem(slot, remaining);
            }
            return true;
        }
        if (!includeOffhand) return false;
        ItemStack stack = player.getInventory().getItemInOffHand();
        if (!RebarItem.isRebarItem(stack, key)) return false;
        if (stack.getAmount() == 1) {
            player.getInventory().setItemInOffHand(null);
        } else {
            ItemStack remaining = stack.clone();
            remaining.setAmount(stack.getAmount() - 1);
            player.getInventory().setItemInOffHand(remaining);
        }
        return true;
    }
}
