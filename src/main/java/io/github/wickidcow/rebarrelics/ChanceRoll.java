package io.github.wickidcow.rebarrelics;

/** Pure probability rule: all chances are expressed as decimals from 0 to 1. */
public final class ChanceRoll {
    private ChanceRoll() {}

    public static boolean success(double chance, double roll) {
        if (!Double.isFinite(chance) || !Double.isFinite(roll)) return false;
        if (roll < 0.0 || roll >= 1.0) return false;
        return roll < Math.clamp(chance, 0.0, 1.0);
    }
}
