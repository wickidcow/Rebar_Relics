package io.github.wickidcow.rebarrelics;

/** Conservative, side-effect-free event and balance predicates. */
public final class PotionRules {
    private PotionRules() {}

    /**
     * Never overwrite a stronger potion effect with a weaker one just because
     * its duration is shorter. The max amplifier is more important than duration.
     */
    public static boolean shouldUpgrade(int currentAmplifier, int currentDuration,
                                        int proposedAmplifier, int proposedDuration) {
        return currentAmplifier < 0
                || currentAmplifier < proposedAmplifier
                || (currentAmplifier == proposedAmplifier && currentDuration < proposedDuration);
    }
}
