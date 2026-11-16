package io.github.wickidcow.rebarrelics;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class RelicBalanceTest {
    @Test void chanceBounds() {
        assertFalse(ChanceRoll.success(0, 0));
        assertTrue(ChanceRoll.success(0.6, 0.599));
        assertFalse(ChanceRoll.success(0.6, 0.60));
        assertTrue(ChanceRoll.success(1, 0.999));
        assertFalse(ChanceRoll.success(Double.POSITIVE_INFINITY, 0.5));
    }
}
