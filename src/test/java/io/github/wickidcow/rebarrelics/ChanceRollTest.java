package io.github.wickidcow.rebarrelics;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class ChanceRollTest {
    @Test void probabilityBoundaries() {
        assertFalse(ChanceRoll.success(0.0, 0.0));
        assertTrue(ChanceRoll.success(1.0, 0.99999));
        assertTrue(ChanceRoll.success(0.75, 0.749));
        assertFalse(ChanceRoll.success(0.75, 0.75));
        assertFalse(ChanceRoll.success(Double.NaN, 0.4));
        assertFalse(ChanceRoll.success(0.8, Double.NaN));
        assertFalse(ChanceRoll.success(0.8, -0.1));
        assertFalse(ChanceRoll.success(0.8, 1.0));
    }
}
