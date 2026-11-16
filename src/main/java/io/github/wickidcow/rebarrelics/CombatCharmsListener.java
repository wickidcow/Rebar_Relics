package io.github.wickidcow.rebarrelics;

import io.github.wickidcow.rebarrelics.RelicItems.Type;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.concurrent.ThreadLocalRandom;

/**
 * One-use defensive triggers model classic Slimefun talismans:
 * Warrior -> Strength III for 3 minutes; Knight -> 30% chance of
 * Regeneration IV for five seconds. No duplicate-copy amplification.
 */
public final class CombatCharmsListener implements Listener {
    private final RebarRelics plugin;

    public CombatCharmsListener(RebarRelics plugin) {
        this.plugin = plugin;
    }

    private boolean active(Player player, Type type, String key) {
        return plugin.getConfig().getBoolean("talismans." + key + ".enabled", true)
                && RelicItems.has(player, type, includeOffhand());
    }

    private boolean includeOffhand() {
        return plugin.getConfig().getBoolean("include-offhand", true);
    }

    private boolean usefulBuff(Player player, PotionEffectType type, int amplifier, int duration) {
        PotionEffect existing = player.getPotionEffect(type);
        return existing == null
                || existing.getAmplifier() < amplifier
                || existing.getDuration() < duration;
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onDamage(EntityDamageEvent event) {
        if (!(event.getEntity() instanceof Player player) || event.getFinalDamage() <= 0.0) {
            return;
        }

        if (active(player, Type.WARRIOR, "warrior")
                && usefulBuff(player, PotionEffectType.STRENGTH, 2, 20 * 60 * 3)
                && RelicItems.consume(player, Type.WARRIOR, includeOffhand())) {
            player.addPotionEffect(new PotionEffect(
                    PotionEffectType.STRENGTH, 20 * 60 * 3, 2, false, true, true));
        }

        double chance = plugin.getConfig().getDouble("talismans.knight.chance", 0.30);
        if (active(player, Type.KNIGHT, "knight")
                && usefulBuff(player, PotionEffectType.REGENERATION, 3, 20 * 5)
                && ChanceRoll.success(chance, ThreadLocalRandom.current().nextDouble())
                && RelicItems.consume(player, Type.KNIGHT, includeOffhand())) {
            player.addPotionEffect(new PotionEffect(
                    PotionEffectType.REGENERATION, 20 * 5, 3, false, true, true));
        }
    }
}
