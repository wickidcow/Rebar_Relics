package io.github.wickidcow.rebarrelics;

/**
 * Pure sizing/transfer contract for personal talisman bags.
 * Backing storage is always 27 slots; smaller keys reveal a prefix only.
 */
public final class BagRules {
    public static final int BACKING_SLOTS = 27;

    private BagRules() {}

    public static int capacity(int tier) {
        if (tier < 1 || tier > 3) throw new IllegalArgumentException("Bag tier must be 1..3");
        return 9 * tier;
    }

    public static boolean accessible(int tier, int slot) {
        return slot >= 0 && slot < capacity(tier);
    }

    public static boolean canDeposit(boolean isTalisman, boolean slotOccupied,
                                     int amount, int maximum, int tier, int slot) {
        return accessible(tier, slot) && isTalisman && !slotOccupied
                && amount > 0 && amount <= maximum && maximum > 0;
    }
}
