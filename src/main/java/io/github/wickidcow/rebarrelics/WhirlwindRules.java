package io.github.wickidcow.rebarrelics;

/** Pure, testable Whirlwind restrictions: no entity spawning or projectile ownership changes. */
public final class WhirlwindRules {
    private WhirlwindRules() {}

    public static boolean mayDeflect(boolean isArrow, boolean isTrident,
                                    boolean shooterIsVictim, double finalDamage) {
        return isArrow && !isTrident && !shooterIsVictim
                && Double.isFinite(finalDamage) && finalDamage > 0.0;
    }

    public static boolean shouldDeflect(boolean isArrow, boolean isTrident,
                                        boolean shooterIsVictim, double finalDamage,
                                        double chance, double roll) {
        return mayDeflect(isArrow, isTrident, shooterIsVictim, finalDamage)
                && ChanceRoll.success(chance, roll);
    }
}
