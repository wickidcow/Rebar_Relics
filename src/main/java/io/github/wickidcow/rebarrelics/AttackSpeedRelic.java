package io.github.wickidcow.rebarrelics;

import io.github.pylonmc.pylon.content.talismans.Talisman;
import io.github.pylonmc.rebar.config.adapter.ConfigAdapter;
import org.bukkit.NamespacedKey;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.attribute.AttributeModifier;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

/**
 * Use Pylon's standard three-tier inventory effect lifecycle instead of a
 * hand-written ticker. ADD_SCALAR makes each level a +5/+10/+25% multiplier.
 */
public final class AttackSpeedRelic extends Talisman {
    public AttackSpeedRelic(@NotNull ItemStack stack) {
        super(stack);
    }

    @Override
    public NamespacedKey getTalismanKey() {
        return new NamespacedKey(RebarRelics.getPlugin(RebarRelics.class), "attack_speed_bonus");
    }

    @Override
    public void applyEffect(@NotNull Player player) {
        super.applyEffect(player);
        if (!RebarRelics.getPlugin(RebarRelics.class).getConfig()
                .getBoolean("talismans.attack-speed.enabled", true)) return;

        AttributeInstance attribute = player.getAttribute(Attribute.ATTACK_SPEED);
        if (attribute == null) return;
        attribute.removeModifier(getTalismanKey());
        double bonus = getSettingOrThrow("attr-bonus", ConfigAdapter.DOUBLE);
        attribute.addModifier(new AttributeModifier(
                getTalismanKey(), bonus, AttributeModifier.Operation.ADD_SCALAR));
    }

    @Override
    public void removeEffect(@NotNull Player player) {
        super.removeEffect(player);
        AttributeInstance attribute = player.getAttribute(Attribute.ATTACK_SPEED);
        if (attribute != null) attribute.removeModifier(getTalismanKey());
    }
}
