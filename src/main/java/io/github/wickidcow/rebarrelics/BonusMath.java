package io.github.wickidcow.rebarrelics;

/** Pure functions for preserving fractional experience bonuses across events. */
public final class BonusMath {
    private BonusMath() {}

    public record ExtraExperience(int whole, double remainder) {}

    public static ExtraExperience extraExperience(int base, double multiplier, double remainder) {
        if (base <= 0) return new ExtraExperience(0, Math.max(0.0, remainder));
        double clamped = Double.isFinite(multiplier) ? Math.clamp(multiplier, 1.0, 5.0) : 1.0;
        double carried = Double.isFinite(remainder) ? Math.clamp(remainder, 0.0, 0.999999999) : 0.0;
        double bonus = base * (clamped - 1.0) + carried;
        int whole = Math.max(0, (int) Math.floor(bonus + 1.0e-9));
        return new ExtraExperience(whole, Math.max(0.0, bonus - whole));
    }
}
