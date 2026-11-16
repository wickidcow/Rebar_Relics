package io.github.wickidcow.rebarrelics;

import org.junit.jupiter.api.Test;

import static io.github.wickidcow.rebarrelics.BagTransferRules.Action.*;
import static org.junit.jupiter.api.Assertions.assertEquals;

class BagTransferRulesTest {
    @Test void exactOneWayMovementsOnly() {
        assertEquals(DEPOSIT, BagTransferRules.choose(1, 0, false, true, true, 1, 64));
        assertEquals(DEPOSIT, BagTransferRules.choose(3, 26, false, true, true, 64, 64));
        assertEquals(WITHDRAW, BagTransferRules.choose(1, 8, true, false, true, 0, 0));
        assertEquals(NONE, BagTransferRules.choose(1, 8, true, true, true, 0, 0));
        assertEquals(NONE, BagTransferRules.choose(1, 8, false, false, true, 1, 64));
    }

    @Test void allLockedAndInvalidSlotsRefuseTransfers() {
        for (int tier = 1; tier <= 3; tier++) {
            for (int slot = 9 * tier; slot < 27; slot++) {
                assertEquals(NONE, BagTransferRules.choose(tier, slot, false, true, true, 1, 64));
                assertEquals(NONE, BagTransferRules.choose(tier, slot, true, false, true, 0, 0));
            }
        }
        for (int tier : new int[] {0, 4}) {
            assertEquals(NONE, BagTransferRules.choose(tier, 0, false, true, true, 1, 64));
        }
        assertEquals(NONE, BagTransferRules.choose(3, -1, false, true, true, 1, 64));
        assertEquals(NONE, BagTransferRules.choose(3, 27, false, true, true, 1, 64));
    }

    @Test void noUnknownItemsOrStackOverflows() {
        assertEquals(NONE, BagTransferRules.choose(2, 0, false, true, false, 1, 64));
        assertEquals(NONE, BagTransferRules.choose(2, 0, false, true, true, 0, 64));
        assertEquals(NONE, BagTransferRules.choose(2, 0, false, true, true, -1, 64));
        assertEquals(NONE, BagTransferRules.choose(2, 0, false, true, true, 65, 64));
        assertEquals(NONE, BagTransferRules.choose(2, 0, false, true, true, 1, 0));
    }

    @Test void simulateConservedWholeStackTransfers() {
        for (int amount = 1; amount <= 64; amount++) {
            int cursor = amount;
            int bag = 0;
            assertEquals(DEPOSIT, BagTransferRules.choose(1, 0, false, true, true, cursor, 64));
            bag = cursor;
            cursor = 0;
            assertEquals(amount, bag + cursor);
            assertEquals(WITHDRAW, BagTransferRules.choose(1, 0, true, false, true, 0, 0));
            cursor = bag;
            bag = 0;
            assertEquals(amount, bag + cursor);
        }
    }
}
