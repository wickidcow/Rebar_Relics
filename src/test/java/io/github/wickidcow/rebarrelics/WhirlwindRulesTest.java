package io.github.wickidcow.rebarrelics;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class WhirlwindRulesTest {
    @Test void onlyForeignVanillaArrowDamageCanBeDeflected() {
        assertTrue(WhirlwindRules.mayDeflect(true, false, false, 2.0));
        assertFalse(WhirlwindRules.mayDeflect(true, true, false, 2.0));
        assertFalse(WhirlwindRules.mayDeflect(true, false, true, 2.0));
        assertFalse(WhirlwindRules.mayDeflect(false, false, false, 2.0));
        assertFalse(WhirlwindRules.mayDeflect(true, false, false, 0.0));
        assertFalse(WhirlwindRules.mayDeflect(true, false, false, Double.NaN));
        assertFalse(WhirlwindRules.mayDeflect(true, false, false, Double.POSITIVE_INFINITY));
    }

    @Test void chancesDoNotExceedConfiguredTierBounds() {
        assertTrue(WhirlwindRules.shouldDeflect(true, false, false, 3, .12, .119));
        assertFalse(WhirlwindRules.shouldDeflect(true, false, false, 3, .12, .12));
        assertFalse(WhirlwindRules.shouldDeflect(true, false, false, 3, .22, .24));
        assertFalse(WhirlwindRules.shouldDeflect(true, true, false, 3, 1, 0));
    }

    @Test void strongerBuffsArePreserved() {
        assertTrue(PotionRules.shouldUpgrade(-1, 0, 2, 3600));
        assertTrue(PotionRules.shouldUpgrade(1, 5000, 2, 3600));
        assertTrue(PotionRules.shouldUpgrade(2, 2000, 2, 3600));
        assertFalse(PotionRules.shouldUpgrade(3, 30, 2, 3600));
        assertFalse(PotionRules.shouldUpgrade(2, 4000, 2, 3600));
        assertFalse(PotionRules.shouldUpgrade(2, 3600, 2, 3600));
    }
}
