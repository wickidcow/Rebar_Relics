package io.github.wickidcow.rebarrelics;

/**
 * Platform-independent transfer planner. The GUI never writes a bag file
 * unless exactly one safe whole-stack movement is possible. Keeping this
 * decision separate from Paper events permits exhaustive unit tests.
 */
public final class BagTransferRules {
    public enum Action { NONE, DEPOSIT, WITHDRAW }

    private BagTransferRules() {}

    public static Action choose(int tier, int slot, boolean cursorEmpty,
                                boolean storedEmpty, boolean cursorIsTalisman,
                                int cursorAmount, int cursorMaximum) {
        if (tier < 1 || tier > 3 || slot < 0 || slot >= BagRules.capacity(tier)) {
            return Action.NONE;
        }
        if (cursorEmpty) {
            // Empty cursor may retrieve only an existing stack.
            return storedEmpty ? Action.NONE : Action.WITHDRAW;
        }
        // Never overwrite, split, swap, or merge two occupied stacks.
        if (!storedEmpty || !cursorIsTalisman
                || cursorAmount <= 0 || cursorMaximum <= 0
                || cursorAmount > cursorMaximum) return Action.NONE;
        return Action.DEPOSIT;
    }
}
