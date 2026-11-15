package io.github.wickidcow.rebarrelics;

import io.github.wickidcow.rebarrelics.RelicItems.Type;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.player.PlayerExpChangeEvent;
import org.bukkit.persistence.PersistentDataType;

public final class RelicListener implements Listener {
    private final RebarRelics plugin;
    private final NamespacedKey xpCarryKey;

    public RelicListener(RebarRelics plugin) {
        this.plugin = plugin;
        this.xpCarryKey = new NamespacedKey(plugin, "experience_bonus_remainder");
    }

    private boolean enabled(String type) {
        return plugin.getConfig().getBoolean("talismans." + type + ".enabled", true);
    }

    private double multiplier(String type) {
        double value = plugin.getConfig().getDouble("talismans." + type + ".multiplier", 1.0);
        return Double.isFinite(value) ? Math.clamp(value, 1.0, 5.0) : 1.0;
    }

    private boolean has(Player player, Type type) {
        return RelicItems.has(player, type, plugin.getConfig().getBoolean("include-offhand", true));
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void damage(EntityDamageByEntityEvent event) {
        // Only a normal direct melee hit or an arrow whose shooter is a player;
        // tridents and non-player shooters are not silently treated as bow attacks.
        if (event.getDamager() instanceof Player player) {
            if (enabled("strength") && has(player, Type.STRENGTH)) {
                event.setDamage(event.getDamage() * multiplier("strength"));
            }
            return;
        }
        if (event.getDamager() instanceof Arrow arrow
                && arrow.getShooter() instanceof Player player) {
            double factor = 1.0;
            if (enabled("archery") && has(player, Type.ARCHERY)) {
                factor *= multiplier("archery");
            }
            if (enabled("strength") && has(player, Type.STRENGTH)) {
                factor *= multiplier("strength");
            }
            event.setDamage(event.getDamage() * factor);
        }
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void experience(PlayerExpChangeEvent event) {
        Player player = event.getPlayer();
        if (!enabled("experience") || !has(player, Type.EXPERIENCE)) return;

        double carry = player.getPersistentDataContainer()
                .getOrDefault(xpCarryKey, PersistentDataType.DOUBLE, 0.0);
        BonusMath.ExtraExperience result = BonusMath.extraExperience(
                event.getAmount(), multiplier("experience"), carry);
        player.getPersistentDataContainer()
                .set(xpCarryKey, PersistentDataType.DOUBLE, result.remainder());
        event.setAmount(event.getAmount() + result.whole());
    }
}
