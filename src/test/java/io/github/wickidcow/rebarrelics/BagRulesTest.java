package io.github.wickidcow.rebarrelics;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class BagRulesTest {
    @Test void tiersNeverTruncateBackingStore() {
        assertEquals(27, BagRules.BACKING_SLOTS);
        assertEquals(9, BagRules.capacity(1));
        assertEquals(18, BagRules.capacity(2));
        assertEquals(27, BagRules.capacity(3));
        assertThrows(IllegalArgumentException.class, () -> BagRules.capacity(0));
        assertThrows(IllegalArgumentException.class, () -> BagRules.capacity(4));
    }

    @Test void lowerTierCannotOverwriteHigherTierSlots() {
        assertTrue(BagRules.accessible(1, 8));
        assertFalse(BagRules.accessible(1, 9));
        assertTrue(BagRules.accessible(2, 17));
        assertFalse(BagRules.accessible(2, 18));
        assertTrue(BagRules.accessible(3, 26));
        assertFalse(BagRules.accessible(3, 27));
        assertFalse(BagRules.accessible(3, -1));
    }

    @Test void storageRejectsNonTalismansNestedItemsAndStackOverflows() {
        assertTrue(BagRules.canDeposit(true, false, 1, 64, 1, 0));
        assertTrue(BagRules.canDeposit(true, false, 64, 64, 3, 26));
        assertFalse(BagRules.canDeposit(false, false, 1, 64, 3, 0));
        assertFalse(BagRules.canDeposit(true, true, 1, 64, 3, 0));
        assertFalse(BagRules.canDeposit(true, false, 65, 64, 3, 0));
        assertFalse(BagRules.canDeposit(true, false, 0, 64, 3, 0));
        assertFalse(BagRules.canDeposit(true, false, 1, 64, 1, 26));
    }
}
