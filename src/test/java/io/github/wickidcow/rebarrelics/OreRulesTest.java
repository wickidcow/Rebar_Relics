package io.github.wickidcow.rebarrelics;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class OreRulesTest {
    @Test void onlyVanillaOreBlocksQualify() {
        assertTrue(OreRules.isOre("DEEPSLATE_DIAMOND_ORE"));
        assertTrue(OreRules.isOre("NETHER_GOLD_ORE"));
        assertFalse(OreRules.isOre("STONE"));
        assertFalse(OreRules.isOre("DIAMOND_BLOCK"));
        assertFalse(OreRules.isOre("CUSTOM_ORE"));
    }

    @Test void neverCloneCustomItemsOrInventNewEntities() {
        assertTrue(OreRules.canSafelyDouble("DIAMOND", false, 1, 64));
        assertTrue(OreRules.canSafelyDouble("RAW_IRON", false, 32, 64));
        assertFalse(OreRules.canSafelyDouble("RAW_IRON", false, 33, 64));
        assertFalse(OreRules.canSafelyDouble("DIAMOND", true, 1, 64));
        assertFalse(OreRules.canSafelyDouble("DIAMOND_SWORD", false, 1, 1));
        assertFalse(OreRules.canSafelyDouble("CUSTOM_ITEM", false, 1, 64));
        assertFalse(OreRules.canSafelyDouble("DIAMOND", false, 0, 64));
    }
}
