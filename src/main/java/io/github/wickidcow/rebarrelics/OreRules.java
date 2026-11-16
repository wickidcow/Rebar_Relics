package io.github.wickidcow.rebarrelics;

import java.util.Set;

/**
 * Pure eligibility rules. Never duplicate Pylon ores, custom item stacks,
 * enchanted silk-touch blocks or items that would overflow their max stack.
 */
public final class OreRules {
    private OreRules() {}

    private static final Set<String> ORES = Set.of(
            "COAL_ORE", "DEEPSLATE_COAL_ORE",
            "IRON_ORE", "DEEPSLATE_IRON_ORE",
            "COPPER_ORE", "DEEPSLATE_COPPER_ORE",
            "GOLD_ORE", "DEEPSLATE_GOLD_ORE",
            "LAPIS_ORE", "DEEPSLATE_LAPIS_ORE",
            "REDSTONE_ORE", "DEEPSLATE_REDSTONE_ORE",
            "DIAMOND_ORE", "DEEPSLATE_DIAMOND_ORE",
            "EMERALD_ORE", "DEEPSLATE_EMERALD_ORE",
            "NETHER_QUARTZ_ORE", "NETHER_GOLD_ORE",
            "ANCIENT_DEBRIS"
    );

    private static final Set<String> DROPS = Set.of(
            "COAL", "RAW_IRON", "RAW_COPPER", "RAW_GOLD",
            "LAPIS_LAZULI", "REDSTONE", "DIAMOND", "EMERALD",
            "QUARTZ", "GOLD_NUGGET", "ANCIENT_DEBRIS"
    );

    public static boolean isOre(String materialName) {
        return ORES.contains(materialName);
    }

    public static boolean canSafelyDouble(String materialName, boolean hasCustomMeta,
                                          int currentAmount, int maximumAmount) {
        return DROPS.contains(materialName)
                && !hasCustomMeta
                && currentAmount >= 1
                && maximumAmount > 0
                && currentAmount <= maximumAmount / 2;
    }
}
