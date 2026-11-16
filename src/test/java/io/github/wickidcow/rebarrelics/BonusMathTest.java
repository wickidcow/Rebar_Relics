package io.github.wickidcow.rebarrelics;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class BonusMathTest {
    @Test
    void tenOnePointPickupsEarnExactlyOneBonusPoint() {
        double carry = 0.0;
        int earned = 0;
        for (int i = 0; i < 10; i++) {
            var result = BonusMath.extraExperience(1, 1.10, carry);
            earned += result.whole();
            carry = result.remainder();
        }
        assertEquals(1, earned);
        assertEquals(0.0, carry, 1.0e-7);
    }

    @Test
    void noBonusWithInvalidMultiplier() {
        assertEquals(0, BonusMath.extraExperience(10, Double.NaN, 0).whole());
        assertEquals(0, BonusMath.extraExperience(10, 0.1, 0).whole());
    }

    @Test
    void carriesUnclaimedFractionForward() {
        var first = BonusMath.extraExperience(4, 1.10, 0);
        assertEquals(0, first.whole());
        assertEquals(0.4, first.remainder(), 1.0e-8);
        var second = BonusMath.extraExperience(6, 1.10, first.remainder());
        assertEquals(1, second.whole());
        assertEquals(0, second.remainder(), 1.0e-8);
    }
}
