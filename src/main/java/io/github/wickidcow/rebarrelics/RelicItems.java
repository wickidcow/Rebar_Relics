package io.github.wickidcow.rebarrelics;

import io.github.pylonmc.pylon.PylonPages;
import io.github.pylonmc.rebar.item.RebarItem;
import io.github.pylonmc.rebar.item.builder.ItemStackBuilder;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.EnumMap;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * Rebar item identity is authoritative. A renamed vanilla item never becomes a relic.
 * Legacy duplicate item IDs are registered but deliberately hidden and inert so existing
 * development snapshots are not converted into unknown items.
 */
public final class RelicItems {
    public enum Type {
        ARCHERY, STRENGTH, EXPERIENCE, ANGEL, TRAVELLER, FIREFIGHTER,
        WATER_BREATHER, LAVA_WALKER, ANVIL,
        ATTACK_SPEED, BOSS, POSEIDON, RESISTANCE, IMMUNITY, NECROSIS,
        MATRIX, CORE, MINER, CAVEMAN, WARRIOR, KNIGHT, WHIRLWIND, BAG
    }

    private static final Map<Type, Map<Integer, NamespacedKey>> KEYS = new EnumMap<>(Type.class);

    // Do not use ordinary clickable, throwable, placeable or damageable base items
    // for new relics. Inert display materials cannot accidentally consume a relic.
    private static final Set<Material> UNSAFE_ACTIVE_ICONS = EnumSet.of(
            Material.BOW, Material.CROSSBOW, Material.TRIDENT,
            Material.FIRE_CHARGE, Material.WIND_CHARGE, Material.EXPERIENCE_BOTTLE,
            Material.IRON_SWORD, Material.DIAMOND_SWORD, Material.IRON_PICKAXE,
            Material.GOLDEN_PICKAXE, Material.GOLDEN_APPLE, Material.SHIELD,
            Material.WITHER_SKELETON_SKULL, Material.WITHER_ROSE
    );

    private RelicItems() {}

    public static void initialize(RebarRelics plugin) {
        // Progression materials are genuine Rebar items, not renamed vanilla ingredients.
        register(plugin, Type.MATRIX, 0, "relic_matrix", Material.AMETHYST_SHARD, true);
        register(plugin, Type.CORE, 1, "talisman_core_1", Material.ECHO_SHARD, true);
        register(plugin, Type.CORE, 2, "talisman_core_2", Material.HEART_OF_THE_SEA, true);
        register(plugin, Type.CORE, 3, "talisman_core_3", Material.NETHER_STAR, true);
        // Personal access keys; the actual bag storage resides on the server,
        // keyed by player UUID, so copied bags do not contain duplicated items.
        register(plugin, Type.BAG, 1, "talisman_bag_1", Material.LEATHER, true, TalismanBag.class);
        register(plugin, Type.BAG, 2, "talisman_bag_2", Material.RABBIT_HIDE, true, TalismanBag.class);
        register(plugin, Type.BAG, 3, "talisman_bag_3", Material.PHANTOM_MEMBRANE, true, TalismanBag.class);


        // Built-in Pylon already provides Experience and Water Breathing talismans.
        // Keep their old Rebar_Relics IDs registered as compatibility-only items.
        register(plugin, Type.EXPERIENCE, 1, "experience_talisman_1", Material.EXPERIENCE_BOTTLE, false);
        register(plugin, Type.WATER_BREATHER, 1, "water_breather_talisman_1", Material.NAUTILUS_SHELL, false);

        register(plugin, Type.ARCHERY, 1, "archery_talisman_1", Material.FLINT, true);
        register(plugin, Type.ARCHERY, 2, "archery_talisman_2", Material.FLINT, true);
        register(plugin, Type.ARCHERY, 3, "archery_talisman_3", Material.FLINT, true);
        register(plugin, Type.STRENGTH, 1, "strength_talisman_1", Material.BLAZE_POWDER, true);
        register(plugin, Type.STRENGTH, 2, "strength_talisman_2", Material.BLAZE_POWDER, true);
        register(plugin, Type.STRENGTH, 3, "strength_talisman_3", Material.BLAZE_POWDER, true);

        register(plugin, Type.ANGEL, 1, "angel_talisman_1", Material.FEATHER, true);
        register(plugin, Type.TRAVELLER, 1, "traveller_talisman_1", Material.SUGAR, true);
        register(plugin, Type.FIREFIGHTER, 1, "firefighter_talisman_1", Material.MAGMA_CREAM, true);
        register(plugin, Type.LAVA_WALKER, 1, "lava_walker_talisman_1", Material.MAGMA_CREAM, true);
        register(plugin, Type.ANVIL, 1, "anvil_talisman_1", Material.IRON_INGOT, true);
        // Classic Slimefun-inspired distinct mechanics absent from Pylon.
        register(plugin, Type.MINER, 1, "miner_talisman_1", Material.IRON_NUGGET, true);
        register(plugin, Type.CAVEMAN, 1, "caveman_talisman_1", Material.RAW_GOLD, true);
        register(plugin, Type.WARRIOR, 1, "warrior_talisman_1", Material.BLAZE_ROD, true);
        register(plugin, Type.KNIGHT, 1, "knight_talisman_1", Material.PRISMARINE_SHARD, true);

        for (int tier = 1; tier <= 3; tier++) {
            register(plugin, Type.ATTACK_SPEED, tier, "attack_speed_talisman_" + tier,
                    Material.QUARTZ, true, AttackSpeedRelic.class);
            register(plugin, Type.BOSS, tier, "boss_talisman_" + tier,
                    Material.NETHER_STAR, true);
            register(plugin, Type.POSEIDON, tier, "poseidon_talisman_" + tier,
                    Material.HEART_OF_THE_SEA, true);
            register(plugin, Type.RESISTANCE, tier, "resistance_talisman_" + tier,
                    Material.PRISMARINE_CRYSTALS, true);
            register(plugin, Type.WHIRLWIND, tier, "whirlwind_talisman_" + tier,
                    Material.AMETHYST_SHARD, true);
        }

        register(plugin, Type.IMMUNITY, 1, "immunity_ring", Material.GOLD_INGOT, true);
        register(plugin, Type.NECROSIS, 1, "necrosis_ring", Material.ECHO_SHARD, true);
    }

    private static void register(RebarRelics plugin, Type type, int tier,
                                 String name, Material material, boolean showInGuide) {
        register(plugin, type, tier, name, material, showInGuide, RebarItem.class);
    }

    private static void register(RebarRelics plugin, Type type, int tier,
                                 String name, Material material, boolean showInGuide,
                                 Class<? extends RebarItem> itemClass) {
        if (showInGuide && UNSAFE_ACTIVE_ICONS.contains(material)) {
            throw new IllegalArgumentException("Relic " + name
                    + " uses a vanilla-interactive base material: " + material);
        }
        NamespacedKey key = new NamespacedKey(plugin, name);
        ItemStack stack = ItemStackBuilder.rebar(material, key).build();
        RebarItem.register(itemClass, stack);
        if (showInGuide) PylonPages.TALISMANS.addItem(stack);
        KEYS.computeIfAbsent(type, ignored -> new HashMap<>()).put(tier, key);
    }

    public static int bestTier(Player player, Type type, boolean includeOffhand) {
        Map<Integer, NamespacedKey> tiers = KEYS.get(type);
        if (tiers == null) return 0;
        ItemStack[] storage = player.getInventory().getStorageContents();
        ItemStack offhand = includeOffhand ? player.getInventory().getItemInOffHand() : null;
        for (int level = 3; level >= 1; level--) {
            NamespacedKey key = tiers.get(level);
            if (key == null) continue;
            for (ItemStack stack : storage) {
                if (RebarItem.isRebarItem(stack, key)) return level;
            }
            if (RebarItem.isRebarItem(offhand, key)) return level;
        }
        return 0;
    }

    /**
     * Explicit whitelist. No Shulker Boxes, other containers, progression cores,
     * bags, or deprecated duplicate IDs. Native Pylon Talismans are allowed.
     * Items inside this personal store never activate inventory effects.
     */
    public static boolean isStorableTalisman(ItemStack stack) {
        if (stack == null || stack.isEmpty() || stack.getAmount() < 1
                || stack.getAmount() > stack.getMaxStackSize()) return false;
        if (RebarItem.isRebarItem(stack, io.github.pylonmc.pylon.content.talismans.Talisman.class)) {
            return true;
        }
        for (var entry : KEYS.entrySet()) {
            Type type = entry.getKey();
            if (type == Type.CORE || type == Type.MATRIX || type == Type.BAG
                    || type == Type.EXPERIENCE || type == Type.WATER_BREATHER) continue;
            for (NamespacedKey key : entry.getValue().values()) {
                if (RebarItem.isRebarItem(stack, key)) return true;
            }
        }
        return false;
    }

    public static boolean isBag(ItemStack stack) {
        if (stack == null || stack.isEmpty()) return false;
        Map<Integer, NamespacedKey> ids = KEYS.get(Type.BAG);
        if (ids == null) return false;
        return ids.values().stream().anyMatch(key -> RebarItem.isRebarItem(stack, key));
    }

    public static boolean has(Player player, Type type, boolean includeOffhand) {
        return bestTier(player, type, includeOffhand) != 0;
    }

    /**
     * Consumes one Tier I item. Consumable classics don't have tiers yet. The event
     * handler only calls this if an effect is actually going to be applied.
     */
    public static boolean consume(Player player, Type type, boolean includeOffhand) {
        NamespacedKey key = KEYS.getOrDefault(type, Map.of()).get(1);
        if (key == null) return false;
        for (int slot = 0; slot < player.getInventory().getStorageContents().length; slot++) {
            ItemStack stack = player.getInventory().getItem(slot);
            if (!RebarItem.isRebarItem(stack, key)) continue;
            if (stack.getAmount() == 1) {
                player.getInventory().setItem(slot, null);
            } else {
                ItemStack copy = stack.clone();
                copy.setAmount(stack.getAmount() - 1);
                player.getInventory().setItem(slot, copy);
            }
            return true;
        }
        if (!includeOffhand) return false;
        ItemStack stack = player.getInventory().getItemInOffHand();
        if (!RebarItem.isRebarItem(stack, key)) return false;
        if (stack.getAmount() == 1) {
            player.getInventory().setItemInOffHand(null);
        } else {
            ItemStack copy = stack.clone();
            copy.setAmount(stack.getAmount() - 1);
            player.getInventory().setItemInOffHand(copy);
        }
        return true;
    }
}
